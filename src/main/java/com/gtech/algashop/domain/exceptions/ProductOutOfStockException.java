package com.gtech.algashop.domain.exceptions;

import com.gtech.algashop.domain.entity.VO.id.ProductId;

import static com.gtech.algashop.domain.exceptions.ErrorMessages.ERROR_PRODUCT_IS_OUT_OF_STOCK;

public class ProductOutOfStockException extends BusinessException {
    public ProductOutOfStockException(ProductId productId) {
        super(String.format(ERROR_PRODUCT_IS_OUT_OF_STOCK, productId));
    }
}
