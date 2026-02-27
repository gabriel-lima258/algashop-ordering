package com.gtech.algashop.domain.model.product;

import com.gtech.algashop.domain.model.BusinessException;

import static com.gtech.algashop.domain.model.ErrorMessages.ERROR_PRODUCT_IS_OUT_OF_STOCK;

public class ProductOutOfStockException extends BusinessException {
    public ProductOutOfStockException(ProductId productId) {
        super(String.format(ERROR_PRODUCT_IS_OUT_OF_STOCK, productId));
    }
}
