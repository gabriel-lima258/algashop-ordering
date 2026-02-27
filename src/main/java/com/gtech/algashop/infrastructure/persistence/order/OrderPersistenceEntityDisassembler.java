package com.gtech.algashop.infrastructure.persistence.order;

import com.gtech.algashop.domain.model.commons.*;
import com.gtech.algashop.domain.model.order.*;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.OrderItemId;
import com.gtech.algashop.domain.model.product.ProductId;
import com.gtech.algashop.domain.model.product.ProductName;
import com.gtech.algashop.infrastructure.persistence.commons.AddressEmbeddable;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OrderPersistenceEntityDisassembler {

    public Order toDomainEntity(OrderPersistenceEntity persistenceEntity) {
        return Order.existing()
                .id(new OrderId(persistenceEntity.getId()))
                .customerId(new CustomerId(persistenceEntity.getCustomerId()))
                .totalAmount(new Money(persistenceEntity.getTotalAmount()))
                .totalQuantity(new Quantity(persistenceEntity.getTotalItems()))
                .status(OrderStatus.valueOf(persistenceEntity.getStatus()))
                .paymentMethod(PaymentMethod.valueOf(persistenceEntity.getPaymentMethod()))
                .placedAt(persistenceEntity.getPlacedAt())
                .paidAt(persistenceEntity.getPaidAt())
                .canceledAt(persistenceEntity.getCanceledAt())
                .readyAt(persistenceEntity.getReadyAt())
                .billing(toBillingValueObject(persistenceEntity.getBilling()))
                .shipping(toShippingValueObject(persistenceEntity.getShipping()))
                .items(new HashSet<>())
                .version(persistenceEntity.getVersion())
                .items(toDomainEntity(persistenceEntity.getItems()))
                .build();
    }

    private Set<OrderItem> toDomainEntity(Set<OrderItemPersistenceEntity> items) {
        return items.stream().map(this::toDomainEntity).collect(Collectors.toSet());
    }

    private OrderItem toDomainEntity(OrderItemPersistenceEntity item) {
        return OrderItem.existing()
                .id(new OrderItemId(item.getId()))
                .orderId(new OrderId(item.getOrderId()))
                .productId(new ProductId(item.getProductId()))
                .productName(new ProductName(item.getProductName()))
                .price(new Money(item.getPrice()))
                .quantity(new Quantity(item.getQuantity()))
                .totalAmount(new Money(item.getTotalAmount()))
                .build();
    }

    private Billing toBillingValueObject(BillingEmbeddable billing) {
        if (billing == null) return null;

        return Billing.builder()
                .fullName(new FullName(billing.getFirstName(), billing.getLastName()))
                .email(new Email(billing.getEmail()))
                .phone(new Phone(billing.getPhone()))
                .document(new Document(billing.getDocument()))
                .address(toAddressValueObject(billing.getAddress()))
                .build();
    }

    private Address toAddressValueObject(AddressEmbeddable address) {
        return Address.builder()
                .street(address.getStreet())
                .complement(address.getComplement())
                .neighborhood(address.getNeighborhood())
                .number(address.getNumber())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(new ZipCode(address.getZipCode()))
                .build();
    }

    private Shipping toShippingValueObject(ShippingEmbeddable shipping) {
        if (shipping == null) return null;

        return Shipping.builder()
                .cost(new Money(shipping.getCost()))
                .expectedDate(shipping.getExpectedDate())
                .recipient(toRecipientValueObject(shipping.getRecipient()))
                .address(toAddressValueObject(shipping.getAddress()))
                .build();
    }

    private Recipient toRecipientValueObject(RecipientEmbeddable recipient) {
        return Recipient.builder()
                .fullName(new FullName(recipient.getFirstName(), recipient.getLastName()))
                .phone(new Phone(recipient.getPhone()))
                .document(new Document(recipient.getDocument()))
                .build();
    }

}
