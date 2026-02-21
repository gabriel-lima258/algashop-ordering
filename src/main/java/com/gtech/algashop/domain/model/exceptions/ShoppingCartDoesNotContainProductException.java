package com.gtech.algashop.domain.model.exceptions;

import com.gtech.algashop.domain.model.entity.VO.id.ProductId;
import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartId;

import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_PRODUCT;

public class ShoppingCartDoesNotContainProductException extends BusinessException {
    public ShoppingCartDoesNotContainProductException(ShoppingCartId id, ProductId productId) {
        super(String.format(ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_PRODUCT, id, productId));
    }
}
