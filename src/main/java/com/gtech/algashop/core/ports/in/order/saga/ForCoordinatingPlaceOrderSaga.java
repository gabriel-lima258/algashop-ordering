package com.gtech.algashop.core.ports.in.order.saga;

import java.util.UUID;

public interface ForCoordinatingPlaceOrderSaga {
    void start(String rawOrderId);
    void onInvoicePaid(UUID sagaId);
    void onStockReservationConfirmed(UUID sagaId);
}
