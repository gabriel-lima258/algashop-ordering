package com.gtech.algashop.infrastructure.listerner.order;

import com.gtech.algashop.application.order.notifications.OrderNotificationApplicationService;
import com.gtech.algashop.domain.model.order.OrderCanceledEvent;
import com.gtech.algashop.domain.model.order.OrderPaidEvent;
import com.gtech.algashop.domain.model.order.OrderPlacedEvent;
import com.gtech.algashop.domain.model.order.OrderReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.gtech.algashop.application.order.notifications.OrderNotificationApplicationService.*;

// classe de escuta dos eventos publicos em customer
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    // efeito colateral do evento
    private final OrderNotificationApplicationService orderNotificationService;

    @EventListener
    public void listen(OrderPlacedEvent event) {
        log.info("OrderPlacedEvent listen 1");
        NotifyOrderStatusInput input = new NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        orderNotificationService.notificateOrderPlaced(input);
    }

    @EventListener
    public void listen(OrderPaidEvent event) {
        log.info("OrderPaidEvent listen 1");
        NotifyOrderStatusInput input = new NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        orderNotificationService.notificateOrderPaid(input);
    }

    @EventListener
    public void listen(OrderReadyEvent event) {
        log.info("OrderReadyEvent listen 1");
        NotifyOrderStatusInput input = new NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        orderNotificationService.notificateOrderReady(input);
    }

    @EventListener
    public void listen(OrderCanceledEvent event) {
        log.info("OrderCanceledEvent listen 1");
        NotifyOrderStatusInput input = new NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        orderNotificationService.notificateOrderCanceled(input);
    }
}
