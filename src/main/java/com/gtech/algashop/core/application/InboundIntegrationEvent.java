package com.gtech.algashop.core.application;

// Marcador dos eventos de integracao que o ordering CONSOME de outros servicos (hoje os
// de produto). Nao tem getAggregateId: quem escolheu a key foi o produtor, e no consumo
// ela chega pronta no header KafkaHeaders.RECEIVED_KEY.
public interface InboundIntegrationEvent {
}
