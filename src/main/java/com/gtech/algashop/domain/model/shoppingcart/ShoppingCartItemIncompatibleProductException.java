package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.BusinessException;
import com.gtech.algashop.domain.model.product.ProductId;

import static com.gtech.algashop.domain.model.ErrorMessages.ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_PRODUCT;

public class ShoppingCartItemIncompatibleProductException extends BusinessException {
    public ShoppingCartItemIncompatibleProductException(ShoppingCartItemId id, ProductId productId) {
        super(String.format(ERROR_SHOPPING_CART_DOES_NOT_CONTAIN_PRODUCT, id, productId));
    }
}
