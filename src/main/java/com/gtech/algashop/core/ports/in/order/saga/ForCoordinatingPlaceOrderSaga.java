package com.gtech.algashop.core.ports.in.order.saga;

public interface ForCoordinatingPlaceOrderSaga {
    void start(String rawOrderId);
}
