package com.gtech.algashop.core.domain.model.costumer;

import com.gtech.algashop.core.domain.model.DomainEntityNotFoundException;
import com.gtech.algashop.core.domain.model.ErrorMessages;

public class CustomerNotFoundExceptionDomain extends DomainEntityNotFoundException {
    public CustomerNotFoundExceptionDomain() {}

    public CustomerNotFoundExceptionDomain(CustomerId customerId) {
        super(String.format(ErrorMessages.ERROR_CUSTOMER_NOT_FOUND, customerId));
    }
}
