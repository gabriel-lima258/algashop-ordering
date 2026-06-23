package com.gtech.algashop.core.domain.model.costumer;

import com.gtech.algashop.core.domain.model.BusinessException;
import com.gtech.algashop.core.domain.model.ErrorMessages;

public class CustomerEmailInUseException extends BusinessException {
    public CustomerEmailInUseException(CustomerId customerId) {
        super(String.format(ErrorMessages.ERROR_CUSTOMER_EMAIL_IN_USE, customerId));
    }
}
