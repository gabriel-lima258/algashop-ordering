package com.gtech.algashop.infrastructure.adapters.in.listerner.order;

import com.gtech.algashop.core.application.order.event.*;
import com.gtech.algashop.core.application.util.Mapper;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundExceptionDomain;
import com.gtech.algashop.core.domain.model.costumer.Customers;
import com.gtech.algashop.core.domain.model.order.*;
import com.gtech.algashop.core.ports.in.order.saga.ForCoordinatingPlaceOrderSaga;
import com.gtech.algashop.core.ports.out.notification.ForPublishingNotificationCommands;
import com.gtech.algashop.core.ports.out.order.ForNotifyingOrder;
import com.gtech.algashop.core.ports.out.order.ForPublishingOrderIntegrationEvents;
import com.gtech.algashop.infrastructure.config.email.NotificationsProperties;
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
    private final OrderPaidIntegrationEventAssembler orderPaidIntegrationEventAssembler;
    private final Mapper mapper;

    private final Orders orders;

    // saga
    private final ForCoordinatingPlaceOrderSaga forCoordinatingPlaceOrderSaga;

    // notification
    private final Customers customers;
    private final NotificationsProperties notificationsProperties;
    private final OrderPlacedEmailCommandAssembler orderPlacedEmailCommandAssembler;
    private final ForPublishingNotificationCommands forPublishingNotificationCommands;

    // notifica o cliente e publica o OrderPlacedIntegrationEvent no broker (broadcast, sem esperar resposta)
    @EventListener
    public void onOrderPlacedPublishIntegrationEvent(OrderPlacedEvent event) {
        log.info("OrderPlacedEvent listen 1");
        ForNotifyingOrder.NotifyOrderStatusInput input = new ForNotifyingOrder.NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        forNotifyingOrder.notificateOrderPlaced(input);

        // valida o order
        Order order = orders.ofId(event.orderId()).orElseThrow(OrderNotFoundExceptionDomain::new);
        // converte o valor através de um assembler o evento recebido
        OrderPlacedIntegrationEvent integrationEvent = orderPlacedIntegrationEventAssembler.toIntegrationEvent(order);
        // publica evento
        forPublishingOrderIntegrationEvents.send(integrationEvent);
    }

    // inicia a saga orquestrada do pedido (o orquestrador coordena os próximos passos e as compensações)
    @EventListener
    public void onOrderPlacedStartSaga(OrderPlacedEvent event) {
        log.info("OrderPlaced Saga");
        forCoordinatingPlaceOrderSaga.start(event.orderId().toString());
    }

    // pede ao notifications o e-mail de confirmacao (dispare e esqueca). @EventListener comum:
    // roda dentro da transacao do checkout, entao o comando so e gravado se o pedido for.
    // Cliente inexistente lanca CustomerNotFoundExceptionDomain e desfaz o checkout inteiro.
    @EventListener
    public void onOrderPlacedSendConfirmationEmail(OrderPlacedEvent event) {
        Order order = orders.ofId(event.orderId()).orElseThrow(OrderNotFoundExceptionDomain::new);
        Customer customer = customers.ofId(order.customerId())
                .orElseThrow(() -> new CustomerNotFoundExceptionDomain(order.customerId()));
        forPublishingNotificationCommands.send(orderPlacedEmailCommandAssembler.toCommand(
                order, customer, notificationsProperties.getEmailTemplates().getOrderPlaced()));
    }

    @EventListener
    public void listen(OrderPaidEvent event) {
        log.info("OrderPaidEvent listen 1");
        ForNotifyingOrder.NotifyOrderStatusInput input = new ForNotifyingOrder.NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        forNotifyingOrder.notificateOrderPaid(input);

        Order order = orders.ofId(event.orderId()).orElseThrow(OrderNotFoundExceptionDomain::new);
        OrderPaidIntegrationEvent integrationEvent = orderPaidIntegrationEventAssembler.toIntegrationEvent(order);
        forPublishingOrderIntegrationEvents.send(integrationEvent);
    }

    @EventListener
    public void listen(OrderReadyEvent event) {
        log.info("OrderReadyEvent listen 1");
        ForNotifyingOrder.NotifyOrderStatusInput input = new ForNotifyingOrder.NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        forNotifyingOrder.notificateOrderReady(input);

        OrderReadyIntegrationEvent integrationEvent = mapper.convert(event, OrderReadyIntegrationEvent.class);
        forPublishingOrderIntegrationEvents.send(integrationEvent);

    }

    @EventListener
    public void listen(OrderCanceledEvent event) {
        log.info("OrderCanceledEvent listen 1");
        ForNotifyingOrder.NotifyOrderStatusInput input = new ForNotifyingOrder.NotifyOrderStatusInput(
                event.orderId().toString(),
                event.customerId().value()
        );
        forNotifyingOrder.notificateOrderCanceled(input);

        OrderCanceledIntegrationEvent integrationEvent = mapper.convert(event, OrderCanceledIntegrationEvent.class);
        forPublishingOrderIntegrationEvents.send(integrationEvent);
    }
}
