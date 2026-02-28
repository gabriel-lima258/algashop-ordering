package com.gtech.algashop.infrastructure.notification.order;

import com.gtech.algashop.application.order.notifications.OrderNotificationApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderNotificationApplicationServiceFakeImpl implements OrderNotificationApplicationService {

    @Override
    public void notificateOrderPlaced(NotifyOrderStatusInput input) {
        log.info("Order placed: {}", input.orderId());
    }

    @Override
    public void notificateOrderPaid(NotifyOrderStatusInput input) {
        log.info("Order paid: {}", input.orderId());
    }

    @Override
    public void notificateOrderReady(NotifyOrderStatusInput input) {
        log.info("Order ready: {}", input.orderId());
    }

    @Override
    public void notificateOrderCanceled(NotifyOrderStatusInput input) {
        log.info("Order canceled: {}", input.orderId());
    }
}
