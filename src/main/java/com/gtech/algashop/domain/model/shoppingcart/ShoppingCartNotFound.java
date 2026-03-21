package com.gtech.algashop.domain.model.shoppingcart;


import com.gtech.algashop.domain.model.EntityNotFoundException;

import static com.gtech.algashop.domain.model.ErrorMessages.ERROR_SHOPPING_CART_NOT_FOUND;

public class ShoppingCartNotFound extends EntityNotFoundException {
    public ShoppingCartNotFound() {
    }

    public ShoppingCartNotFound(ShoppingCartId shoppingCartId) {
        super(String.format(ERROR_SHOPPING_CART_NOT_FOUND, shoppingCartId));
    }
}
