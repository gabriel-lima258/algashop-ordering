package com.gtech.algashop.core.domain.model.product;

import com.gtech.algashop.core.domain.model.DomainException;
import com.gtech.algashop.core.domain.model.ErrorMessages;
import com.gtech.algashop.core.domain.model.commons.Quantity;

public class InsufficientQuantityException extends DomainException {
    public InsufficientQuantityException(ProductName name, Quantity quantity) {
        super(String.format(ErrorMessages.ERROR_PRODUCT_INSUFFICIENT_STOCK, name, quantity));
    }
}
