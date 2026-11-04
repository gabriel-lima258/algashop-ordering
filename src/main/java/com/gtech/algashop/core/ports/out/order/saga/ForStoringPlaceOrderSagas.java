package com.gtech.algashop.core.ports.out.order.saga;

import com.gtech.algashop.core.application.order.saga.PlaceOrderSaga;

import java.util.Optional;
import java.util.UUID;

public interface ForStoringPlaceOrderSagas {
    Optional<PlaceOrderSaga> ofId(UUID sagaId);
    void add(PlaceOrderSaga saga);
}
