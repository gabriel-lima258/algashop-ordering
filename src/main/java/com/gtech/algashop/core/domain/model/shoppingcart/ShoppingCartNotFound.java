package com.gtech.algashop.core.domain.model.shoppingcart;


import com.gtech.algashop.core.domain.model.EntityNotFoundException;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_SHOPPING_CART_NOT_FOUND;

public class ShoppingCartNotFound extends EntityNotFoundException {
    public ShoppingCartNotFound() {
    }

    public ShoppingCartNotFound(ShoppingCartId shoppingCartId) {
        super(String.format(ERROR_SHOPPING_CART_NOT_FOUND, shoppingCartId));
    }
}
