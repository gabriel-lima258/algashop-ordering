package com.gtech.algashop.infrastructure.adapters.in.messaging.kafka.product;

import com.gtech.algashop.core.application.product.event.ProductDelistedIntegrationEvent;
import com.gtech.algashop.core.application.product.event.ProductListedIntegrationEvent;
import com.gtech.algashop.core.application.product.event.ProductPriceChangedV2IntegrationEvent;
import com.gtech.algashop.core.ports.in.shoppingcart.ForManagingShoppingCarts;
import com.gtech.algashop.infrastructure.config.cache.ProductCacheManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

// Adaptador de ENTRADA da mensageria: onde os eventos do product-catalog entram no
// ordering. O @KafkaListener fica na CLASSE e cada @KafkaHandler trata um tipo - o
// dispatch e feito pelo tipo ja desserializado, e quem decide esse tipo e o
// KafkaConsumerTypeIdIdentifier, lendo o header __TypeId__ contra o type.mapping do
// application.yml (nome LOGICO, nao classe Java - os pacotes dos servicos ficam
// desacoplados).
//
// O topico vem de ${algashop.messaging.kafka.product-event-topic-name} - placeholder
// de propriedade, nao SpEL com nome de bean: nome de bean dentro de SpEL e contrato
// implicito que um rename de classe quebra sem nenhum aviso do compilador (licao ja
// paga neste projeto com o @securityChecks do billing).
//
// O group-id "ordering" (application.yml) faz todas as instancias do servico agirem
// como UM grupo: cada uma das 3 particoes tem no maximo um consumidor do grupo, e o
// offset commitado pertence ao grupo, nao a instancia - escalar instancias divide
// particoes, nao duplica consumo.
//
// O @KafkaHandler(isDefault = true) e a rede de seguranca: evento sem type.mapping
// chega como ObjectNode e cai nele - so key + offset no log, em silencio. E decisao,
// nao acidente: consumidor antigo nao pode quebrar porque o produtor evoluiu.
// ProductAddedIntegrationEvent esta nesse caso DE PROPOSITO, como exemplo vivo.
//
// Desde a Fase 39 os handlers tem efeito de negocio: Listed/Delisted flipam a
// disponibilidade do item nos carrinhos que contem o produto (notification: so o fato
// e o id) e PriceChangedV2 atualiza o preco dos itens com o valor QUE VEIO NO EVENTO
// (ECST: nenhuma chamada de volta ao catalogo). O @Valid no payload do V2 e validado
// pelo validator plugado em KafkaBeanValidationConfigurer.
//
// O que AINDA nao existe: retry com backoff e DLQ. E atencao ao que o default faz de
// verdade: o DefaultErrorHandler do spring-kafka tenta 10 vezes SEM intervalo e depois
// DESCARTA a mensagem commitando o offset - nao e retry infinito, e perda silenciosa.
// Um evento de preco que falhe some, e o carrinho fica desatualizado para sempre, com
// um log de erro como unico vestigio.
@Component
@Slf4j
@KafkaListener(topics = "${algashop.messaging.kafka.product-event-topic-name}")
@RequiredArgsConstructor
public class KafkaProductIntegrationEventListener {

    private final ForManagingShoppingCarts shoppingCarts;
    private final ProductCacheManager productCacheManager;

    // notification: o evento so diz O QUE aconteceu e com QUEM - o efeito e decisao local
    @KafkaHandler
    public void handle(@Payload ProductListedIntegrationEvent event,
                       @Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey
                       ) {
        log.info("Event Received from: {}", event.getClass());
        log.info("Message key: {}", messageKey);
        shoppingCarts.changeProductAvailability(event.getProductId(), true);
        // o evict vem DEPOIS da atualizacao: invalidar antes abre janela para uma leitura
        // concorrente repovoar o cache com o dado velho enquanto o banco ainda muda
        productCacheManager.evict(event.getProductId());
    }

    @KafkaHandler
    public void handle(@Payload ProductDelistedIntegrationEvent event,
                       @Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey
    ) {
        log.info("Event Received from: {}", event.getClass());
        log.info("Message key: {}", messageKey);
        shoppingCarts.changeProductAvailability(event.getProductId(), false);
        productCacheManager.evict(event.getProductId());
    }

    // @Valid: o payload desserializado passa pelo Bean Validation ANTES do metodo rodar
    // (validator registrado em KafkaBeanValidationConfigurer) - evento com campo nulo
    // vira erro de listener aqui na porta, nao NullPointerException no meio do dominio
    @KafkaHandler
    public void handle(@Payload @Valid ProductPriceChangedV2IntegrationEvent event,
                       @Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey
    ) {
        log.info("Event Received from: {}", event.getClass());
        log.info("Message key: {}", messageKey);
        // ECST na pratica: o preco novo vem do proprio evento - nenhuma chamada ao catalogo
        shoppingCarts.refreshProductPrice(event.getProductId(), event.getNewSalePrice());
        productCacheManager.evict(event.getProductId());
    }

    // mapeia os listeners desconhecidos com handler generico para todos eventos desconhecidos
    @KafkaHandler(isDefault = true)
    public void handle(@Payload Object object,
                       @Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey,
                       @Header(value = KafkaHeaders.OFFSET) String messageOffset
    ) {
        log.info("Event ignored: key = {}, offset = {}", messageKey, messageOffset);
    }
}
