package com.gtech.algashop.core.application.order.saga;

import java.util.UUID;

public class PlaceOrderSagaNotFoundException extends RuntimeException {

    public PlaceOrderSagaNotFoundException(UUID sagaId) {
        super("Place order saga %s not found".formatted(sagaId));
    }

}
