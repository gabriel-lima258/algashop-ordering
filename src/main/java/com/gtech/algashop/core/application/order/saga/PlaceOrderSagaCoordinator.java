package com.gtech.algashop.core.application.order.saga;

import com.gtech.algashop.core.application.invoice.command.IssueInvoiceIntegrationCommand;
import com.gtech.algashop.core.application.order.snapshot.OrderSnapshot;
import com.gtech.algashop.core.application.order.snapshot.OrderSnapshotAssembler;
import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderId;
import com.gtech.algashop.core.domain.model.order.OrderNotFoundExceptionDomain;
import com.gtech.algashop.core.domain.model.order.Orders;
import com.gtech.algashop.core.ports.in.order.saga.ForCoordinatingPlaceOrderSaga;
import com.gtech.algashop.core.ports.out.order.saga.ForPublishingPlaceOrderSagaIntegrationCommands;
import com.gtech.algashop.core.ports.out.order.saga.ForStoringPlaceOrderSagas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaceOrderSagaCoordinator implements ForCoordinatingPlaceOrderSaga {

    private final ForStoringPlaceOrderSagas sagas;

    private final Orders orders;
    private final OrderSnapshotAssembler assembler;

    private final ForPublishingPlaceOrderSagaIntegrationCommands commands;

    // Chamado pelo OrderEventListener dentro da transacao do checkout: pedido, saga e
    // comando commitam juntos, ou nenhum. A ordem importa: monta o comando, inicia a saga
    // e ja a move para INVOICING, grava, e so entao registra o comando na outbox
    @Override
    @Transactional
    public void start(String rawOrderId) {
        // carrega e converte order
        OrderId orderId = new OrderId(rawOrderId);
        Order order = findOrder(orderId);
        OrderSnapshot snapshot = assembler.toSnapshot(order);

        // emite commando
        IssueInvoiceIntegrationCommand command = IssueInvoiceIntegrationCommand.from(snapshot);

        // inicia uma saga e move orquestração
        PlaceOrderSaga saga = PlaceOrderSaga.start(orderId);
        saga.moveToInvoicing();
        sagas.add(saga);

        // publica comando
        commands.send(saga.sagaId().toString(), command);

        log.info("Place order saga started: saga={} order={}", saga.sagaId(), saga.orderId());
    }

    private Order findOrder(OrderId orderId) {
        return orders.ofId(orderId).orElseThrow(OrderNotFoundExceptionDomain::new);
    }
}
