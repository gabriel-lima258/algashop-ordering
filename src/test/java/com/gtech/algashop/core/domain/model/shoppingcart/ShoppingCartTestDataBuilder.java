package com.gtech.algashop.core.domain.model.shoppingcart;

import com.gtech.algashop.core.domain.model.commons.Quantity;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.product.ProductTestDataBuilder;

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
