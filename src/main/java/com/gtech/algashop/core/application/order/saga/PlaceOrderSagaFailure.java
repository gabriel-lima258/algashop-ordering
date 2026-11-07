package com.gtech.algashop.core.application.order.saga;

// Por que a saga foi compensada. Gravado na coluna failure da saga_instance (reservada
// desde a V010): o status diz QUE falhou, este enum diz POR QUE
public enum PlaceOrderSagaFailure {
    PAYMENT_REFUSED,
    OUT_OF_STOCK
}
