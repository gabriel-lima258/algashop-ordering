package com.gtech.algashop.core.application.order.snapshot;

import com.gtech.algashop.core.domain.model.commons.*;
import com.gtech.algashop.core.domain.model.order.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

// Conversor do agregado Order para o OrderSnapshot serializavel: desembrulha value objects
// para tipos simples. So existe o sentido de ida - a volta (toDomain) servia apenas ao
// consumidor do checkout assincrono, removido. Sem chamador por enquanto: volta quando a
// Saga montar o conteudo do comando de emissao de fatura.
@Component
public class OrderSnapshotAssembler {

    public OrderSnapshot toSnapshot(Order order) {
        return new OrderSnapshot(
                order.id().toString(),
                order.customerId().value(),
                order.placedAt(),
                order.status(),
                toSnapshot(order.billing()),
                toSnapshot(order.shipping()),
                order.paymentMethod().name(),
                order.creditCardId() == null ? null : order.creditCardId().id(),
                order.totalAmount().money(),
                order.totalQuantity().quantity(),
                order.items().stream().map(this::toSnapshot).collect(Collectors.toSet())
        );
    }

    private BillingSnapshot toSnapshot(Billing billing) {
        return new BillingSnapshot(
                billing.fullName().firstName(), billing.fullName().lastName(), billing.document().document(),
                billing.phone().phone(), billing.email().email(), toSnapshot(billing.address())
        );
    }

    private ShippingSnapshot toSnapshot(Shipping shipping) {
        var recipient = shipping.recipient();
        return new ShippingSnapshot(
                shipping.cost().money(), shipping.expectedDate(),
                new RecipientSnapshot(
                        recipient.fullName().firstName(), recipient.fullName().lastName(),
                        recipient.document().document(), recipient.phone().phone()),
                toSnapshot(shipping.address())
        );
    }

    private AddressSnapshot toSnapshot(Address address) {
        return new AddressSnapshot(address.street(), address.complement(), address.neighborhood(),
                address.number(), address.city(), address.state(), address.zipCode().zipcode());
    }

    private OrderItemSnapshot toSnapshot(OrderItem item) {
        return new OrderItemSnapshot(item.id().toString(),
                item.orderId().toString(),
                item.productId().value(),
                item.productName().name(),
                item.price().money(),
                item.quantity().quantity(),
                item.totalAmount().money());
    }
}
