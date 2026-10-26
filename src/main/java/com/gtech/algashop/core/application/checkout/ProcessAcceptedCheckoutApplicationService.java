package com.gtech.algashop.core.application.checkout;

import com.gtech.algashop.core.application.checkout.command.ProcessAcceptedCheckoutIntegrationCommand;
import com.gtech.algashop.core.application.order.event.OrderPlacedIntegrationEventAssembler;
import com.gtech.algashop.core.application.order.snapshot.OrderSnapshotAssembler;
import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderId;
import com.gtech.algashop.core.domain.model.order.Orders;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartId;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCarts;
import com.gtech.algashop.core.ports.in.checkout.ForProcessingCheckoutAccepted;
import com.gtech.algashop.core.ports.out.order.ForPublishingOrderIntegrationEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// O destino do comando de checkout - onde a Order NASCE de verdade. Roda na thread do
// consumidor Kafka, dentro da transacao aberta pelo runOnce da idempotencia:
//   1. orders.exists(orderId): segunda guarda de idempotencia (a primeira e a PK de
//      processed_message) - cobre o caso do header de dedupe faltar
//   2. reconstroi o Order do snapshot (Order.existing() - sem recalculo, sem HTTP)
//   3. persiste, esvazia o carrinho que originou (se houver)
//   4. publica OrderPlacedIntegrationEvent - de novo via outbox, mesma transacao
// ATENCAO (pendencia AGUDA): Order.existing() NAO emite eventos de dominio, entao o
// OrderEventListener (notificacao ao cliente) nunca dispara no caminho V2 - pedido
// assincrono hoje nao notifica. Ver outbox-e-checkout-assincrono.md.

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessAcceptedCheckoutApplicationService implements ForProcessingCheckoutAccepted {

    // Repositories
    private final Orders orders;
    private final ShoppingCarts shoppingCarts;

    // Assembler
    private final OrderSnapshotAssembler orderSnapshotAssembler;
    private final OrderPlacedIntegrationEventAssembler orderPlacedIntegrationEventAssembler;

    // Events
    private final ForPublishingOrderIntegrationEvents forPublishingOrderIntegrationEvents;

    @Override
    @Transactional
    public void process(ProcessAcceptedCheckoutIntegrationCommand integrationCommand) {
        OrderId orderId = new OrderId(integrationCommand.getAggregateId());

        if (orders.exists(orderId)) {
            log.warn("Checkout command ignored order {} already exists.", orderId);
            return;
        }

        Order order = orderSnapshotAssembler.toDomain(integrationCommand.getOrder());
        orders.add(order);

        // limpeza do carrinho
        UUID rawShoppingCartId = integrationCommand.getOrder().shoppingCartId();
        if (rawShoppingCartId != null) {
            shoppingCarts.ofId(new ShoppingCartId(rawShoppingCartId))
                    .ifPresent(shoppingCart -> {
                        shoppingCart.empty();
                        shoppingCarts.add(shoppingCart);
                    });
        }

        // publica o evento
        forPublishingOrderIntegrationEvents.send(
                orderPlacedIntegrationEventAssembler.toIntegrationEvent(order)
        );
    }
}
