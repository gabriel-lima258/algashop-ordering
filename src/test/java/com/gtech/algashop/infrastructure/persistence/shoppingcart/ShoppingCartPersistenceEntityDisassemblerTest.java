package com.gtech.algashop.infrastructure.persistence.disassembler;

import com.gtech.algashop.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartItem;
import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartId;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartItemId;
import com.gtech.algashop.domain.model.product.ProductId;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartItemPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntityDisassembler;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

class ShoppingCartPersistenceEntityDisassemblerTest {

    private final ShoppingCartPersistenceEntityDisassembler disassembler =
            new ShoppingCartPersistenceEntityDisassembler();

    @Test
    void shouldMapScalarFields() {
        ShoppingCartPersistenceEntity persistenceEntity = ShoppingCartPersistenceEntityTestDataBuilder
                .existingCart().build();

        ShoppingCart cart = disassembler.toDomainEntity(persistenceEntity);

        assertThat(cart.id()).isEqualTo(new ShoppingCartId(persistenceEntity.getId()));
        assertThat(cart.customerId()).isEqualTo(new CustomerId(persistenceEntity.getCustomerId()));
        assertThat(cart.totalAmount()).isEqualTo(new Money(persistenceEntity.getTotalAmount()));
        assertThat(cart.totalItems()).isEqualTo(new Quantity(persistenceEntity.getTotalItems()));
        assertThat(cart.createdAt()).isEqualTo(persistenceEntity.getCreatedAt());
        assertThat(cart.version()).isEqualTo(persistenceEntity.getVersion());
    }

    @Test
    void shouldMapItems() {
        ShoppingCartPersistenceEntity persistenceEntity = ShoppingCartPersistenceEntityTestDataBuilder
                .existingCart().build();

        ShoppingCart cart = disassembler.toDomainEntity(persistenceEntity);

        assertThat(cart.items()).hasSize(persistenceEntity.getItems().size());
    }

    @Test
    void shouldMapItemScalarFields() {
        ShoppingCartItemPersistenceEntity itemEntity = ShoppingCartPersistenceEntityTestDataBuilder
                .existingItem().build();

        ShoppingCartPersistenceEntity persistenceEntity = ShoppingCartPersistenceEntityTestDataBuilder
                .existingCart()
                .items(java.util.Set.of(itemEntity))
                .build();

        ShoppingCart cart = disassembler.toDomainEntity(persistenceEntity);
        ShoppingCartItem item = cart.items().iterator().next();

        assertThat(item.id()).isEqualTo(new ShoppingCartItemId(itemEntity.getId()));
        assertThat(item.productId()).isEqualTo(new ProductId(itemEntity.getProductId()));
        assertThat(item.name().name()).isEqualTo(itemEntity.getProductName());
        assertThat(item.price()).isEqualTo(new Money(itemEntity.getPrice()));
        assertThat(item.quantity()).isEqualTo(new Quantity(itemEntity.getQuantity()));
        assertThat(item.totalAmount()).isEqualTo(new Money(itemEntity.getTotalAmount()));
        assertThat(item.available()).isEqualTo(itemEntity.getAvailable());
    }

    @Test
    void shouldMapItemShoppingCartIdFromParent() {
        ShoppingCartPersistenceEntity persistenceEntity = ShoppingCartPersistenceEntityTestDataBuilder
                .existingCart().build();

        ShoppingCart cart = disassembler.toDomainEntity(persistenceEntity);

        // Cada item deve ter o shoppingCartId igual ao id do carrinho pai
        cart.items().forEach(item ->
                assertThat(item.shoppingCartId()).isEqualTo(cart.id())
        );
    }

    @Test
    void shouldMapEmptyItemsSet() {
        ShoppingCartPersistenceEntity persistenceEntity = ShoppingCartPersistenceEntityTestDataBuilder
                .existingCart()
                .items(new HashSet<>())
                .build();

        ShoppingCart cart = disassembler.toDomainEntity(persistenceEntity);

        assertThat(cart.items()).isEmpty();
    }
}
