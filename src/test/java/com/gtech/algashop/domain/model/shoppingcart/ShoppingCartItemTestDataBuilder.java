package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.commons.Quantity;

public class ShoppingCartItemTestDataBuilder {

    private ShoppingCartId shoppingCartId = new ShoppingCartId();
    private Product product = ProductTestDataBuilder.aProduct().build();
    private Quantity quantity = new Quantity(1);

    private ShoppingCartItemTestDataBuilder() {
    }

    /////////////////////////////////////
    ///  BUILDER
    ////////////////////////////////////

    public static ShoppingCartItemTestDataBuilder anItem() {
        return new ShoppingCartItemTestDataBuilder();
    }

    public ShoppingCartItem build() {
        return ShoppingCartItem.brandNew()
                .shoppingCartId(shoppingCartId)
                .productId(product.id())
                .productName(product.productName())
                .price(product.price())
                .quantity(quantity)
                .available(product.inStock())
                .build();
    }

    /////////////////////////////////////
    ///  SETTERS
    ////////////////////////////////////

    public ShoppingCartItemTestDataBuilder shoppingCartId(ShoppingCartId shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
        return this;
    }

    public ShoppingCartItemTestDataBuilder product(Product product) {
        this.product = product;
        return this;
    }

    public ShoppingCartItemTestDataBuilder quantity(Quantity quantity) {
        this.quantity = quantity;
        return this;
    }
}
