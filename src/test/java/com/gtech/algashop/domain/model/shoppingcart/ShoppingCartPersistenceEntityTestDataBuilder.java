package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.IdGenerator;
import com.gtech.algashop.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartItemPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;

public class ShoppingCartPersistenceEntityTestDataBuilder {

    private ShoppingCartPersistenceEntityTestDataBuilder() {}

    public static ShoppingCartPersistenceEntity.ShoppingCartPersistenceEntityBuilder existingCart() {
        return ShoppingCartPersistenceEntity.builder()
                .id(IdGenerator.generateTimeBasedUUID())
                .customer(CustomerPersistenceEntityTestDataBuilder.existingCustomer().build())
                .totalAmount(new BigDecimal("4620"))
                .totalItems(2)
                .createdAt(OffsetDateTime.now())
                .items(Set.of(
                        existingItem().build(),
                        existingItemAlt().build()
                ));
    }

    public static ShoppingCartItemPersistenceEntity.ShoppingCartItemPersistenceEntityBuilder existingItem() {
        return ShoppingCartItemPersistenceEntity.builder()
                .id(IdGenerator.generateTSID().toLong())
                .productId(IdGenerator.generateTimeBasedUUID())
                .productName("Notebook Max Pro")
                .price(new BigDecimal("4500"))
                .quantity(1)
                .totalAmount(new BigDecimal("4500"))
                .available(true);
    }

    public static ShoppingCartItemPersistenceEntity.ShoppingCartItemPersistenceEntityBuilder existingItemAlt() {
        return ShoppingCartItemPersistenceEntity.builder()
                .id(IdGenerator.generateTSID().toLong())
                .productId(IdGenerator.generateTimeBasedUUID())
                .productName("Mouse Pad Gamer")
                .price(new BigDecimal("120"))
                .quantity(1)
                .totalAmount(new BigDecimal("120"))
                .available(true);
    }
}
