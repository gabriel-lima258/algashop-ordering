package com.gtech.algashop.domain.entity;

import com.gtech.algashop.domain.entity.VO.Money;
import com.gtech.algashop.domain.entity.VO.Product;
import com.gtech.algashop.domain.entity.VO.ProductName;
import com.gtech.algashop.domain.entity.VO.Quantity;
import com.gtech.algashop.domain.entity.VO.id.ProductId;
import com.gtech.algashop.domain.entity.VO.id.ShoppingCartId;
import com.gtech.algashop.domain.entity.VO.id.ShoppingCartItemId;
import com.gtech.algashop.domain.entity.factory.ProductTestDataBuilder;
import com.gtech.algashop.domain.entity.factory.ShoppingCartItemTestDataBuilder;
import com.gtech.algashop.domain.exceptions.ShoppingCartItemIncompatibleProductException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ShoppingCartItemTest {

    @Test
    void shouldGenerateNewShoppingCartItem() {
        Product product = ProductTestDataBuilder.aProduct().build();
        ShoppingCartId shoppingCartId = new ShoppingCartId();

        ShoppingCartItem shoppingCartItem = ShoppingCartItemTestDataBuilder.anItem()
                .shoppingCartId(shoppingCartId)
                .product(product)
                .build();

        Assertions.assertWith(shoppingCartItem,
                o -> Assertions.assertThat(o.id()).isNotNull(),
                o -> Assertions.assertThat(o.productId()).isEqualTo(product.id()),
                o -> Assertions.assertThat(o.price()).isEqualTo(product.price()),
                o -> Assertions.assertThat(o.quantity()).isEqualTo(new Quantity(1)),
                o -> Assertions.assertThat(o.available()).isEqualTo(product.inStock()),
                o -> Assertions.assertThat(o.shoppingCartId()).isEqualTo(shoppingCartId)
        );
    }

    @Test
    void shouldThrowExceptionWhenAQuantityIsZero() {
        ShoppingCartItem shoppingCartItem = ShoppingCartItemTestDataBuilder.anItem().build();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> shoppingCartItem.changeQuantity(new Quantity(0)));
    }

    @Test
    void shouldCalculateTotalAmountAsPriceTimesQuantity() {
        Product product = ProductTestDataBuilder.aProduct().build();
        Quantity quantity = new Quantity(3);

        ShoppingCartItem shoppingCartItem = ShoppingCartItemTestDataBuilder.anItem()
                .product(product)
                .quantity(quantity)
                .build();

        Assertions.assertThat(shoppingCartItem.totalAmount())
                .isEqualTo(product.price().multiply(quantity));
    }

    @Test
    void shouldThrowExceptionWhenRefreshingWithIncompatibleProduct() {
        ShoppingCartItem shoppingCartItem = ShoppingCartItemTestDataBuilder.anItem().build();

        Product incompatibleProduct = ProductTestDataBuilder.aProductMousePad().build();

        Assertions.assertThatExceptionOfType(ShoppingCartItemIncompatibleProductException.class)
                .isThrownBy(() -> shoppingCartItem.refresh(incompatibleProduct));
    }

    @Test
    void shouldBeEqualWhenSameId() {
        ShoppingCartItemId sharedId = new ShoppingCartItemId();
        ShoppingCartId shoppingCartId = new ShoppingCartId();
        ProductId productId = new ProductId();

        ShoppingCartItem item1 = ShoppingCartItem.existing()
                .id(sharedId)
                .shoppingCartId(shoppingCartId)
                .productId(productId)
                .name(new ProductName("Product A"))
                .price(new Money("100.00"))
                .quantity(new Quantity(1))
                .totalAmount(new Money("100.00"))
                .available(true)
                .build();

        ShoppingCartItem item2 = ShoppingCartItem.existing()
                .id(sharedId)
                .shoppingCartId(shoppingCartId)
                .productId(productId)
                .name(new ProductName("Product B"))
                .price(new Money("200.00"))
                .quantity(new Quantity(5))
                .totalAmount(new Money("1000.00"))
                .available(false)
                .build();

        Assertions.assertThat(item1).isEqualTo(item2);
    }
}
