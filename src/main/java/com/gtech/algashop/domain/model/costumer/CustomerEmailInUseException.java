package com.gtech.algashop.domain.model.costumer;

import com.gtech.algashop.domain.model.BusinessException;
import com.gtech.algashop.domain.model.ErrorMessages;

public class CustomerEmailInUseException extends BusinessException {
    public CustomerEmailInUseException(CustomerId customerId) {
        super(String.format(ErrorMessages.ERROR_CUSTOMER_EMAIL_IN_USE, customerId));
    }
}
