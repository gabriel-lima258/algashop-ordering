package com.gtech.algashop.domain.exceptions;

import com.gtech.algashop.domain.entity.VO.id.*;

import static com.gtech.algashop.domain.exceptions.ErrorMessages.*;

public class ShoppingCartDoesNotContainItemException extends BusinessException {
    public ShoppingCartDoesNotContainItemException(ShoppingCartId id, ShoppingCartItemId shoppingCartItemId) {
        super(String.format(ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_ITEM, id, shoppingCartItemId));
    }
}
