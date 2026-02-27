package com.gtech.algashop.domain.model.exceptions;

public class BusinessException extends RuntimeException {

    public BusinessException() {
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

}
