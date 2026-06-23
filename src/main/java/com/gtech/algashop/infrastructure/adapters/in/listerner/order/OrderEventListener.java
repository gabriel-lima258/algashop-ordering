package com.gtech.algashop.infrastructure.adapters.in.listerner.order;

import com.gtech.algashop.core.domain.model.order.OrderCanceledEvent;
import com.gtech.algashop.core.domain.model.order.OrderPaidEvent;
import com.gtech.algashop.core.domain.model.order.OrderPlacedEvent;
import com.gtech.algashop.core.domain.model.order.OrderReadyEvent;
import com.gtech.algashop.core.ports.out.order.ForNotifyingOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// classe de escuta dos eventos publicos em customer
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

    // efeito colateral do evento
    private final ForNotifyingOrder forNotifyingOrder;

    @EventListener
    public void listen(OrderPlacedEvent event) {
        log.info("OrderPlacedEvent listen 1");
        ForNotifyingOrder.NotifyOrderStatusInput input = new ForNotifyingOrder.NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        forNotifyingOrder.notificateOrderPlaced(input);
    }

    @EventListener
    public void listen(OrderPaidEvent event) {
        log.info("OrderPaidEvent listen 1");
        ForNotifyingOrder.NotifyOrderStatusInput input = new ForNotifyingOrder.NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        forNotifyingOrder.notificateOrderPaid(input);
    }

    @EventListener
    public void listen(OrderReadyEvent event) {
        log.info("OrderReadyEvent listen 1");
        ForNotifyingOrder.NotifyOrderStatusInput input = new ForNotifyingOrder.NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        forNotifyingOrder.notificateOrderReady(input);
    }

    @EventListener
    public void listen(OrderCanceledEvent event) {
        log.info("OrderCanceledEvent listen 1");
        ForNotifyingOrder.NotifyOrderStatusInput input = new ForNotifyingOrder.NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        forNotifyingOrder.notificateOrderCanceled(input);
    }
}
