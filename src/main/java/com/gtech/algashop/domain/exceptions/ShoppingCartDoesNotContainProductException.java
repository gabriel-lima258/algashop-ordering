package com.gtech.algashop.domain.exceptions;

import com.gtech.algashop.domain.entity.VO.id.ProductId;
import com.gtech.algashop.domain.entity.VO.id.ShoppingCartId;
import com.gtech.algashop.domain.entity.VO.id.ShoppingCartItemId;

import static com.gtech.algashop.domain.exceptions.ErrorMessages.ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_ITEM;
import static com.gtech.algashop.domain.exceptions.ErrorMessages.ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_PRODUCT;

public class ShoppingCartDoesNotContainProductException extends BusinessException {
    public ShoppingCartDoesNotContainProductException(ShoppingCartId id, ProductId productId) {
        super(String.format(ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_PRODUCT, id, productId));
    }
}
