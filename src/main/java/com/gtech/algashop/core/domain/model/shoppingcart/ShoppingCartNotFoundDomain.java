package com.gtech.algashop.core.domain.model.shoppingcart;


import com.gtech.algashop.core.domain.model.DomainEntityNotFoundException;

import java.util.UUID;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_SHOPPING_CART_NOT_FOUND;

public class ShoppingCartNotFoundDomain extends DomainEntityNotFoundException {
    public ShoppingCartNotFoundDomain() {
    }

    public ShoppingCartNotFoundDomain(String message) {
        super(message);
    }

    public ShoppingCartNotFoundDomain(ShoppingCartId shoppingCartId) {
        super(String.format(ERROR_SHOPPING_CART_NOT_FOUND, shoppingCartId));
    }

    public static ShoppingCartNotFoundDomain ofCustomer(UUID customerId) {
        return new ShoppingCartNotFoundDomain("Shopping cart for customer ID " + customerId + " not found");
    }
}
