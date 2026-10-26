package com.gtech.algashop.core.domain.model.product;

import com.gtech.algashop.core.domain.model.DomainEntityNotFoundException;
import com.gtech.algashop.core.domain.model.ErrorMessages;

public class ProductNotFoundExceptionDomain extends DomainEntityNotFoundException {
    public ProductNotFoundExceptionDomain() {}

    public ProductNotFoundExceptionDomain(ProductId productId) {
        super(String.format(ErrorMessages.ERROR_PRODUCT_NOT_FOUND, productId));
    }
}
