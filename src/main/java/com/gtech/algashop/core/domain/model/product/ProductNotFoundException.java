package com.gtech.algashop.core.domain.model.product;

import com.gtech.algashop.core.domain.model.EntityNotFoundException;
import com.gtech.algashop.core.domain.model.ErrorMessages;

public class ProductNotFoundException extends EntityNotFoundException {
    public ProductNotFoundException() {}

    public ProductNotFoundException(ProductId productId) {
        super(String.format(ErrorMessages.ERROR_PRODUCT_NOT_FOUND, productId));
    }
}
