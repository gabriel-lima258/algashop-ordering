package com.gtech.algashop.core.application.order.saga;

import com.gtech.algashop.core.application.invoice.command.IssueInvoiceIntegrationCommand;
import com.gtech.algashop.core.application.order.snapshot.OrderSnapshot;
import com.gtech.algashop.core.application.order.snapshot.OrderSnapshotAssembler;
import com.gtech.algashop.core.application.stock.command.ReserveStockIntegrationCommand;
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

import java.util.UUID;

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

    // Resposta "fatura paga": numa transacao so, o pedido vira PAID (e o OrderPaid vai para
    // a outbox), a saga passa a RESERVING_STOCK e o comando ReserveStock e gravado na outbox
    @Override
    @Transactional
    public void onInvoicePaid(UUID sagaId) {
        PlaceOrderSaga saga = findSaga(sagaId);
        Order order = findOrder(saga.orderId());

        // reply repetido (reentrega, at-least-once do billing): a saga ja avancou, ignora
        if (saga.isReservingStock() || saga.hasSucceeded()) {
            logIgnored(saga);
            return;
        }

        order.markAsPaid();
        orders.add(order);

        saga.moveToReservingStock();
        sagas.add(saga);

        commands.send(saga.sagaId().toString(), reserveStockIntegrationCommand(order));
    }

    // Resposta "reserva confirmada": o pivo deu certo, a saga nao volta mais. Numa transacao
    // so, o pedido vira READY (OrderReady na outbox) e a saga passa por APPROVING_ORDER - um
    // passo interno, sem comando - ate SUCCEEDED
    @Override
    @Transactional
    public void onStockReservationConfirmed(UUID sagaId) {
        PlaceOrderSaga saga = findSaga(sagaId);

        if (saga.hasSucceeded()) {
            logIgnored(saga);
            return;
        }

        Order order = findOrder(saga.orderId());
        order.markAsReady();
        orders.add(order);

        saga.moveToApprovingOrder();
        saga.endSucceeded();
        sagas.add(saga);
    }

    private void logIgnored(PlaceOrderSaga saga) {
        log.info("Saga reply was ignored: saga:{} order:{} status:{} step:{}",
                saga.sagaId(), saga.orderId(), saga.status(), saga.step()
        );
    }

    private ReserveStockIntegrationCommand reserveStockIntegrationCommand(Order order) {
        return new ReserveStockIntegrationCommand(
                order.id().toString(),
                order.items().stream().map(item -> new ReserveStockIntegrationCommand.Item(
                        item.productId().value(),
                        item.quantity().quantity()
                )).toList()
        );
    }

    private PlaceOrderSaga findSaga(UUID sagaId) {
        return sagas.ofId(sagaId).orElseThrow(() -> new PlaceOrderSagaNotFoundException(sagaId));
    }

    private Order findOrder(OrderId orderId) {
        return orders.ofId(orderId).orElseThrow(OrderNotFoundExceptionDomain::new);
    }
}
