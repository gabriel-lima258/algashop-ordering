package com.gtech.algashop.core.application;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

// Contrato dos eventos de integracao que o ordering PUBLICA (pedido realizado, pago,
// cancelado, pronto). O getAggregateId() vira a KEY do record: todos os eventos de um
// mesmo pedido caem na mesma particao, em ordem. Os eventos que o ordering CONSOME
// implementam InboundIntegrationEvent, que nao precisa de key.
public interface OutboundIntegrationEvent {
    // @JsonIgnore espelha o produtor: aggregateId fora do JSON nos dois lados - o id
    // ja viaja como key do record e como campo productId do payload
    @JsonIgnore
    String getAggregateId();

    @JsonIgnore
    default UUID getIdempotencyKey() {
        return null;
    }
}
