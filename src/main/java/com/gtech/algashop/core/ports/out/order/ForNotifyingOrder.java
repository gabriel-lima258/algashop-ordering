package com.gtech.algashop.core.ports.out.order;

import java.util.UUID;

public interface ForNotifyingOrder {
    // evento a ser disparado
    void notificateOrderPlaced(NotifyOrderStatusInput input);
    void notificateOrderPaid(NotifyOrderStatusInput input);
    void notificateOrderReady(NotifyOrderStatusInput input);
    void notificateOrderCanceled(NotifyOrderStatusInput input);

    record NotifyOrderStatusInput(String orderId, UUID customerId){}
}
