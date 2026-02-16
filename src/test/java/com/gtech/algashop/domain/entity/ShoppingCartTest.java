package com.gtech.algashop.domain.entity;

import com.gtech.algashop.domain.entity.VO.Money;
import com.gtech.algashop.domain.entity.VO.Product;
import com.gtech.algashop.domain.entity.VO.Quantity;
import com.gtech.algashop.domain.entity.VO.id.CustomerId;
import com.gtech.algashop.domain.entity.VO.id.ShoppingCartId;
import com.gtech.algashop.domain.entity.VO.id.ShoppingCartItemId;
import com.gtech.algashop.domain.entity.factory.ProductTestDataBuilder;
import com.gtech.algashop.domain.entity.factory.ShoppingCartTestDataBuilder;
import com.gtech.algashop.domain.exceptions.ProductOutOfStockException;
import com.gtech.algashop.domain.exceptions.ShoppingCartDoesNotContainItemException;
import com.gtech.algashop.domain.exceptions.ShoppingCartDoesNotContainProductException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashSet;

class ShoppingCartTest {

    @Test
    void shouldBeEmptyAfterCreation() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart()
                .withItems(false)
                .build();

        Assertions.assertWith(cart,
                c -> Assertions.assertThat(c.totalAmount()).isEqualTo(Money.ZERO),
                c -> Assertions.assertThat(c.totalItems()).isEqualTo(Quantity.ZERO),
                c -> Assertions.assertThat(c.isEmpty()).isTrue()
        );
    }

    @Test
    void shouldThrowExceptionWhenAddingOutOfStockProduct() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart()
                .withItems(false)
                .build();

        Product unavailableProduct = ProductTestDataBuilder.aProductUnavailable().build();

        Assertions.assertThatExceptionOfType(ProductOutOfStockException.class)
                .isThrownBy(() -> cart.addItem(unavailableProduct, new Quantity(1)));
    }

    @Test
    void shouldSumQuantityWhenAddingSameProductTwice() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart()
                .withItems(false)
                .build();

        Product notebook = ProductTestDataBuilder.aProduct().build();

        cart.addItem(notebook, new Quantity(1));
        cart.addItem(notebook, new Quantity(2));

        Assertions.assertWith(cart,
                c -> Assertions.assertThat(c.items()).hasSize(1),
                c -> Assertions.assertThat(c.totalItems()).isEqualTo(new Quantity(3)),
                c -> Assertions.assertThat(c.totalAmount()).isEqualTo(new Money("13500.00"))
        );

        ShoppingCartItem item = cart.items().iterator().next();
        Assertions.assertWith(item,
                i -> Assertions.assertThat(i.quantity()).isEqualTo(new Quantity(3)),
                i -> Assertions.assertThat(i.price()).isEqualTo(notebook.price()),
                i -> Assertions.assertThat(i.available()).isEqualTo(notebook.inStock())
        );
    }

    @Test
    void shouldAddTwoDifferentProducts() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart()
                .withItems(false)
                .build();

        Product notebook = ProductTestDataBuilder.aProduct().build();
        Product mousePad = ProductTestDataBuilder.aProductMousePad().build();

        cart.addItem(notebook, new Quantity(1));
        cart.addItem(mousePad, new Quantity(2));

        Assertions.assertWith(cart,
                c -> Assertions.assertThat(c.items()).hasSize(2),
                c -> Assertions.assertThat(c.totalItems()).isEqualTo(new Quantity(3)),
                c -> Assertions.assertThat(c.totalAmount()).isEqualTo(new Money("4740.00"))
        );
    }

    @Test
    void shouldThrowExceptionWhenRemovingNonExistentItem() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart()
                .withItems(false)
                .build();

        ShoppingCartItemId nonExistentId = new ShoppingCartItemId();

        Assertions.assertThatExceptionOfType(ShoppingCartDoesNotContainItemException.class)
                .isThrownBy(() -> cart.removeItem(nonExistentId));
    }

    @Test
    void shouldEmptyCartAndResetTotals() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().build();

        cart.empty();

        Assertions.assertWith(cart,
                c -> Assertions.assertThat(c.items()).isEmpty(),
                c -> Assertions.assertThat(c.totalAmount()).isEqualTo(Money.ZERO),
                c -> Assertions.assertThat(c.totalItems()).isEqualTo(Quantity.ZERO),
                c -> Assertions.assertThat(c.isEmpty()).isTrue()
        );
    }

    @Test
    void shouldThrowExceptionWhenRefreshingWithIncompatibleProduct() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().build();

        Product incompatibleProduct = ProductTestDataBuilder.aProductRamMemory().build();

        Assertions.assertThatExceptionOfType(ShoppingCartDoesNotContainProductException.class)
                .isThrownBy(() -> cart.refreshItem(incompatibleProduct));
    }

    @Test
    void shouldBeEqualWhenSameId() {
        ShoppingCartId sharedId = new ShoppingCartId();

        ShoppingCart cart1 = ShoppingCart.existing()
                .id(sharedId)
                .customerId(new CustomerId())
                .totalAmount(Money.ZERO)
                .totalItems(Quantity.ZERO)
                .createdAt(OffsetDateTime.now())
                .items(new HashSet<>())
                .build();

        ShoppingCart cart2 = ShoppingCart.existing()
                .id(sharedId)
                .customerId(new CustomerId())
                .totalAmount(new Money("100"))
                .totalItems(new Quantity(5))
                .createdAt(OffsetDateTime.now())
                .items(new HashSet<>())
                .build();

        Assertions.assertThat(cart1).isEqualTo(cart2);
    }
}
