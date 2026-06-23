package com.gtech.algashop.core.domain.model.costumer;

import com.gtech.algashop.core.domain.model.EntityNotFoundException;
import com.gtech.algashop.core.domain.model.ErrorMessages;

public class CustomerNotFoundException extends EntityNotFoundException {
    public CustomerNotFoundException() {}

    public CustomerNotFoundException(CustomerId customerId) {
        super(String.format(ErrorMessages.ERROR_CUSTOMER_NOT_FOUND, customerId));
    }
}
