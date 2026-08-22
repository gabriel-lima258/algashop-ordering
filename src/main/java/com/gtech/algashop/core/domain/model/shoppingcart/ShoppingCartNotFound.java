package com.gtech.algashop.core.domain.model.shoppingcart;


import com.gtech.algashop.core.domain.model.EntityNotFoundException;

import java.util.UUID;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_SHOPPING_CART_NOT_FOUND;

public class ShoppingCartNotFound extends EntityNotFoundException {
    public ShoppingCartNotFound() {
    }

    public ShoppingCartNotFound(String message) {
        super(message);
    }

    public ShoppingCartNotFound(ShoppingCartId shoppingCartId) {
        super(String.format(ERROR_SHOPPING_CART_NOT_FOUND, shoppingCartId));
    }

    public static ShoppingCartNotFound ofCustomer(UUID customerId) {
        return new ShoppingCartNotFound("Shopping cart for customer ID " + customerId + " not found");
    }
}
