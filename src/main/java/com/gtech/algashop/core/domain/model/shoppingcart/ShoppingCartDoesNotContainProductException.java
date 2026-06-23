package com.gtech.algashop.core.domain.model.shoppingcart;

import com.gtech.algashop.core.domain.model.BusinessException;
import com.gtech.algashop.core.domain.model.product.ProductId;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_PRODUCT;

public class ShoppingCartDoesNotContainProductException extends BusinessException {
    public ShoppingCartDoesNotContainProductException(ShoppingCartId id, ProductId productId) {
        super(String.format(ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_PRODUCT, id, productId));
    }
}
