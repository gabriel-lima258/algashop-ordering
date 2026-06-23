package com.gtech.algashop.core.domain.model.costumer;

import com.gtech.algashop.core.domain.model.BusinessException;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_CUSTOMER_ARCHIVED;

public class CustomerArchivedException extends BusinessException {

    public CustomerArchivedException() {
        super(ERROR_CUSTOMER_ARCHIVED);
    }

    public CustomerArchivedException(Throwable cause) {
        super(ERROR_CUSTOMER_ARCHIVED, cause);
    }
}
