package com.gtech.algashop.domain.model.factory;

import com.gtech.algashop.domain.model.util.IdGenerator;
import com.gtech.algashop.infrastructure.persistence.embeddable.AddressEmbeddable;
import com.gtech.algashop.infrastructure.persistence.embeddable.BillingEmbeddable;
import com.gtech.algashop.infrastructure.persistence.embeddable.RecipientEmbeddable;
import com.gtech.algashop.infrastructure.persistence.embeddable.ShippingEmbeddable;
import com.gtech.algashop.infrastructure.persistence.entity.OrderItemPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.entity.OrderPersistenceEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Set;

public class OrderPersistenceEntityTestDataBuilder {

    private OrderPersistenceEntityTestDataBuilder() {}

    public static OrderPersistenceEntity.OrderPersistenceEntityBuilder existingOrder() {
        return OrderPersistenceEntity.builder()
                .id(IdGenerator.generateTSID().toLong())
                .customer(CustomerPersistenceEntityTestDataBuilder.existingCustomer().build())
                .totalItems(3)
                .totalAmount(new BigDecimal(4300))
                .status("PLACED")
                .paymentMethod("CREDIT_CARD")
                .placedAt(OffsetDateTime.now())
                .items(Set.of(
                        existingItem().build(),
                        existingItemAlt().build()
                ))
                .billing(aBillingEmbeddable())
                .shipping(aShippingEmbeddable());
    }

    public static BillingEmbeddable aBillingEmbeddable() {
        return BillingEmbeddable.builder()
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@gmail.com")
                .phone("123-111-9911")
                .document("255-09-1992")
                .address(anAddressEmbeddable())
                .build();
    }

    public static ShippingEmbeddable aShippingEmbeddable() {
        return ShippingEmbeddable.builder()
                .cost(new BigDecimal("10"))
                .expectedDate(LocalDate.now().plusWeeks(1))
                .recipient(aRecipientEmbeddable())
                .address(anAddressEmbeddable())
                .build();
    }

    public static RecipientEmbeddable aRecipientEmbeddable() {
        return RecipientEmbeddable.builder()
                .firstName("John")
                .lastName("Doe")
                .phone("123-111-9911")
                .document("255-09-1992")
                .build();
    }

    public static AddressEmbeddable anAddressEmbeddable() {
        return AddressEmbeddable.builder()
                .street("Bourbon Street")
                .number("1134")
                .neighborhood("North Ville")
                .city("York")
                .state("South California")
                .zipCode("12345")
                .complement("Apt. 114")
                .build();
    }

    public static OrderItemPersistenceEntity.OrderItemPersistenceEntityBuilder existingItem() {
        return OrderItemPersistenceEntity.builder()
                .id(IdGenerator.generateTSID().toLong())
                .price(new BigDecimal(2000))
                .quantity(2)
                .totalAmount(new BigDecimal(4000))
                .productName("Notebook")
                .productId(IdGenerator.generateTimeBasedUUID());
    }


    public static OrderItemPersistenceEntity.OrderItemPersistenceEntityBuilder existingItemAlt() {
        return OrderItemPersistenceEntity.builder()
                .id(IdGenerator.generateTSID().toLong())
                .price(new BigDecimal(300))
                .quantity(1)
                .totalAmount(new BigDecimal(300))
                .productName("Mouse pad")
                .productId(IdGenerator.generateTimeBasedUUID());
    }
}
