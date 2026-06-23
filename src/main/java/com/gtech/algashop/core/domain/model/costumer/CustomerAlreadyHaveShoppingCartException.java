package com.gtech.algashop.core.domain.model.costumer;

import com.gtech.algashop.core.domain.model.BusinessException;
import com.gtech.algashop.core.domain.model.ErrorMessages;

public class CustomerAlreadyHaveShoppingCartException extends BusinessException {
    public CustomerAlreadyHaveShoppingCartException(CustomerId customerId) {
        super(String.format(ErrorMessages.ERROR_CUSTOMER_ALREADY_HAVE_SHOPPING_CART, customerId));
    }
}
