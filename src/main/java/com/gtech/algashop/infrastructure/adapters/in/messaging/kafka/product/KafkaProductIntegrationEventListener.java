package com.gtech.algashop.infrastructure.adapters.in.messaging.kafka.product;

import com.gtech.algashop.core.application.product.event.ProductDelistedIntegrationEvent;
import com.gtech.algashop.core.application.product.event.ProductListedIntegrationEvent;
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
// O que AINDA nao existe aqui: efeito de negocio (os handlers so logam), retry com
// backoff e DLQ - uma excecao hoje vira retry infinito do container na mesma mensagem.
@Component
@Slf4j
@KafkaListener(topics = "${algashop.messaging.kafka.product-event-topic-name}")
public class KafkaProductIntegrationEventListener {


    // le o payload no kafka e adiciona no header
    @KafkaHandler
    public void handle(@Payload ProductListedIntegrationEvent event,
                       @Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey
                       ) {
        log.info("Event Received from: {}", event.getClass());
        log.info("Message key: {}", messageKey);
    }

    @KafkaHandler
    public void handle(@Payload ProductDelistedIntegrationEvent event,
                       @Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey
    ) {
        log.info("Event Received from: {}", event.getClass());
        log.info("Message key: {}", messageKey);
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
