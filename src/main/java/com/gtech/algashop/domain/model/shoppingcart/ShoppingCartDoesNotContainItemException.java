package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.BusinessException;

import static com.gtech.algashop.domain.model.ErrorMessages.*;

public class ShoppingCartDoesNotContainItemException extends BusinessException {
    public ShoppingCartDoesNotContainItemException(ShoppingCartId id, ShoppingCartItemId shoppingCartItemId) {
        super(String.format(ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_ITEM, id, shoppingCartItemId));
    }
}
