package com.gtech.algashop.domain.model.entity.factory;

import com.gtech.algashop.domain.model.entity.ShoppingCart;
import com.gtech.algashop.domain.model.entity.VO.Quantity;
import com.gtech.algashop.domain.model.entity.VO.id.CustomerId;

public class ShoppingCartTestDataBuilder {

    private CustomerId customerId = new CustomerId();
    private boolean withItems = true;

    private ShoppingCartTestDataBuilder() {
    }

    /////////////////////////////////////
    ///  BUILDER
    ////////////////////////////////////

    public static ShoppingCartTestDataBuilder aShoppingCart() {
        return new ShoppingCartTestDataBuilder();
    }

    public ShoppingCart build() {
        ShoppingCart cart = ShoppingCart.startShopping(customerId);

        if (withItems) {
            cart.addItem(ProductTestDataBuilder.aProduct().build(), new Quantity(1));
            cart.addItem(ProductTestDataBuilder.aProductMousePad().build(), new Quantity(1));
        }

        return cart;
    }

    /////////////////////////////////////
    ///  SETTERS
    ////////////////////////////////////

    public ShoppingCartTestDataBuilder customerId(CustomerId customerId) {
        this.customerId = customerId;
        return this;
    }

    public ShoppingCartTestDataBuilder withItems(boolean withItems) {
        this.withItems = withItems;
        return this;
    }
}
