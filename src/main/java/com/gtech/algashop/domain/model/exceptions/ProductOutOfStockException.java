package com.gtech.algashop.domain.model.exceptions;

import com.gtech.algashop.domain.model.entity.VO.id.ProductId;

import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.ERROR_PRODUCT_IS_OUT_OF_STOCK;

public class ProductOutOfStockException extends BusinessException {
    public ProductOutOfStockException(ProductId productId) {
        super(String.format(ERROR_PRODUCT_IS_OUT_OF_STOCK, productId));
    }
}
