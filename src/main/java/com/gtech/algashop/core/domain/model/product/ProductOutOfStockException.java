package com.gtech.algashop.core.domain.model.product;

import com.gtech.algashop.core.domain.model.DomainException;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_PRODUCT_IS_OUT_OF_STOCK;

public class ProductOutOfStockException extends DomainException {
    public ProductOutOfStockException(ProductId productId) {
        super(String.format(ERROR_PRODUCT_IS_OUT_OF_STOCK, productId));
    }
}
