package com.gtech.algashop.domain.exceptions;

import static com.gtech.algashop.domain.exceptions.ErrorMessages.ERROR_CUSTOMER_ARCHIVED;

public class CustomerArchivedException extends BusinessException {

    public CustomerArchivedException() {
        super(ERROR_CUSTOMER_ARCHIVED);
    }

    public CustomerArchivedException(Throwable cause) {
        super(ERROR_CUSTOMER_ARCHIVED, cause);
    }
}
