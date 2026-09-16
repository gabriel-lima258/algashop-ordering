package com.gtech.algashop.infrastructure.adapters.in.messaging.kafka.product;

import com.gtech.algashop.core.application.product.event.ProductDelistedIntegrationEvent;
import com.gtech.algashop.core.application.product.event.ProductListedIntegrationEvent;
import com.gtech.algashop.core.application.product.event.ProductPriceChangedV2IntegrationEvent;
import com.gtech.algashop.core.ports.in.shoppingcart.ForManagingShoppingCarts;
import com.gtech.algashop.core.ports.out.idempotency.ForGuardingIdempotency;
import com.gtech.algashop.infrastructure.config.cache.ProductCacheManager;
import com.gtech.algashop.infrastructure.config.kafka.KafkaConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// Adaptador de ENTRADA da mensageria: onde os eventos do product-catalog entram no
// ordering. O @KafkaListener fica na CLASSE e cada @KafkaHandler trata um tipo - o
// dispatch e feito pelo tipo ja desserializado, e quem decide esse tipo e o
// KafkaConsumerTypeIdIdentifier, lendo o header __TypeId__ contra o type.mapping do
// application.yml (nome LOGICO, nao classe Java - os pacotes dos servicos ficam
// desacoplados).
//
// O topico vem por SpEL (#{algaShopMessagingKafkaProperties...}) - o curso voltou a
// essa forma na Fase 41. Ela funciona (o nome do bean esta correto), mas fica o aviso
// de sempre: nome de bean em SpEL e contrato implicito que um rename quebra em runtime
// sem aviso do compilador (licao do @securityChecks do billing).
//
// Desde a Fase 41 o group.id vem do id do LISTENER ("ordering.product-events" - o
// idIsGroup default), nao mais do group-id global: cada listener desta aplicacao e um
// grupo proprio, com offsets proprios. E concurrency=3 cria 3 threads de consumo -
// uma por particao, o teto util de paralelismo do topico.
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
// A pendencia de retry/DLQ FECHOU na Fase 41: o KafkaConfig pluga DefaultErrorHandler
// com backoff exponencial (2s/4s/8s) e DeadLetterPublishingRecoverer - excecao que
// esgota as tentativas (ou e de negocio, sem retry) vai para
// ordering.dlt.product-catalog.product.events, mesma particao, e fica 30 dias. O
// descarte silencioso da Fase 39 acabou. E o handler do V2 agora roda sob a guarda de
// idempotencia (ForGuardingIdempotency): INSERT na PK de processed_message NA MESMA
// transacao do efeito - redelivery vira violacao de PK, rollback e skip.
@Component
@Slf4j
@KafkaListener(
        id = "ordering.product-events",
        concurrency = "3", // threads em paralelo para cada partição
        topics = "#{algaShopMessagingKafkaProperties.productEventTopicName}"
)
@RequiredArgsConstructor
public class KafkaProductIntegrationEventListener {

    // Publish Event
    private final ForManagingShoppingCarts forManagingShoppingCarts;

    // Cache
    private final ProductCacheManager productCacheManager;
    private final ForGuardingIdempotency forGuardingIdempotency;

    // notification: o evento so diz O QUE aconteceu e com QUEM - o efeito e decisao local
    @KafkaHandler
    public void handle(@Payload ProductListedIntegrationEvent event,
                       @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
                       @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
                       @Header(value = KafkaHeaders.OFFSET, required = false) Integer messageOffset
                       ) {
        logReceived(event, messageKey, partition, messageOffset);

        forManagingShoppingCarts.changeProductAvailability(event.getProductId(), true);
        // o evict vem DEPOIS da atualizacao: invalidar antes abre janela para uma leitura
        // concorrente repovoar o cache com o dado velho enquanto o banco ainda muda
        productCacheManager.evict(event.getProductId());
    }

    @KafkaHandler
    public void handle(@Payload ProductDelistedIntegrationEvent event,
                       @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
                       @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
                       @Header(value = KafkaHeaders.OFFSET, required = false) Integer messageOffset
    ) {
        logReceived(event, messageKey, partition, messageOffset);

        forManagingShoppingCarts.changeProductAvailability(event.getProductId(), false);
        productCacheManager.evict(event.getProductId());
    }

    // @Valid: o payload desserializado passa pelo Bean Validation ANTES do metodo rodar
    // (validator registrado em KafkaBeanValidationConfigurer) - evento com campo nulo
    // vira erro de listener aqui na porta, nao NullPointerException no meio do dominio
    @KafkaHandler
    public void handle(@Payload @Valid ProductPriceChangedV2IntegrationEvent event,
                       @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
                       @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
                       @Header(value = KafkaHeaders.OFFSET, required = false) Integer messageOffset,
                       @Header(value = KafkaConfig.IDEMPOTENCY_KEY_HEADER, required = false) byte[] rawIdempotencyKey
    ) {
        logReceived(event, messageKey, partition, messageOffset);

        // header ausente (mensagem legada, produtor sem a chave) nao pode virar
        // retries + DLT por erro de binding: processa sem a guarda, com aviso
        if (rawIdempotencyKey == null) {
            log.warn("Missing idempotency-key header, processing without dedupe guard");
            forManagingShoppingCarts.refreshProductPrice(event.getProductId(), event.getNewSalePrice());
            productCacheManager.evict(event.getProductId());
            return;
        }

        UUID idempotencyKey = UUID.fromString(new String(rawIdempotencyKey, StandardCharsets.UTF_8));

        // processa o evento com idempotencia
        boolean processed = forGuardingIdempotency.runOnce(idempotencyKey, () -> {
            productCacheManager.evict(event.getProductId()); // redis nao tem rollback
            forManagingShoppingCarts.refreshProductPrice(event.getProductId(), event.getNewSalePrice()); // rollback
        });

        // caso ruim: rollback
        if (!processed) {
            return;
        }

        // após a confimação de idempotencia pode processar outros eventos, email, etc
        log.warn("Mail send product price on shopping cart has been updated");

    }

    // mapeia os listeners desconhecidos com handler generico para todos eventos desconhecidos
    @KafkaHandler(isDefault = true)
    public void handle(@Payload Object object,
                       @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
                       @Header(value = KafkaHeaders.OFFSET, required = false) String messageOffset
    ) {
        log.info("Event ignored: key = {}, offset = {}", messageKey, messageOffset);
    }

    private void logReceived(Object event, String messageKey, Integer partition, Integer offset) {
        log.info("Received: {} | Key: {} | Partition: {} | Offset: {} | Thread: {}",
                event.getClass().getSimpleName(),
                messageKey,
                partition,
                offset,
                Thread.currentThread().getName()
        );
    }
}
