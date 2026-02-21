package com.gtech.algashop.domain.model.exceptions;

import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartId;
import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartItemId;

import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.*;

public class ShoppingCartDoesNotContainItemException extends BusinessException {
    public ShoppingCartDoesNotContainItemException(ShoppingCartId id, ShoppingCartItemId shoppingCartItemId) {
        super(String.format(ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_ITEM, id, shoppingCartItemId));
    }
}
