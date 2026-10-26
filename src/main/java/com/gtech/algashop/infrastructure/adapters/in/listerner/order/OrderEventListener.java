package com.gtech.algashop.infrastructure.adapters.in.listerner.order;

import com.gtech.algashop.core.application.order.event.OrderPlacedIntegrationEvent;
import com.gtech.algashop.core.application.order.event.OrderPlacedIntegrationEventAssembler;
import com.gtech.algashop.core.application.util.Mapper;
import com.gtech.algashop.core.domain.model.order.*;
import com.gtech.algashop.core.ports.out.order.ForNotifyingOrder;
import com.gtech.algashop.core.ports.out.order.ForPublishingOrderIntegrationEvents;
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
    private final ForPublishingOrderIntegrationEvents forPublishingOrderIntegrationEvents;

    private final OrderPlacedIntegrationEventAssembler orderPlacedIntegrationEventAssembler;
    private final Orders orders;

    @EventListener
    public void listen(OrderPlacedEvent event) {
        log.info("OrderPlacedEvent listen 1");
        ForNotifyingOrder.NotifyOrderStatusInput input = new ForNotifyingOrder.NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        forNotifyingOrder.notificateOrderPlaced(input);

        // valida o order
        Order order = orders.ofId(event.orderId()).orElseThrow(OrderNotFoundException::new);
        // converte o valor através de um assembler o evento recebido
        OrderPlacedIntegrationEvent integrationEvent = orderPlacedIntegrationEventAssembler.toIntegrationEvent(order);
        // publica evento
        forPublishingOrderIntegrationEvents.send(integrationEvent);
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
