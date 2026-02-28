package com.gtech.algashop.application.order.notifications;

import java.util.UUID;

public interface OrderNotificationApplicationService {
    // evento a ser disparado
    void notificateOrderPlaced(NotifyOrderStatusInput input);
    void notificateOrderPaid(NotifyOrderStatusInput input);
    void notificateOrderReady(NotifyOrderStatusInput input);
    void notificateOrderCanceled(NotifyOrderStatusInput input);

    record NotifyOrderStatusInput(String orderId, UUID customerId){}
}
