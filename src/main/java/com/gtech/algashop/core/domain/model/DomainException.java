package com.gtech.algashop.core.domain.model;

public class DomainException extends RuntimeException {

    public DomainException() {
    }

    public DomainException(Throwable cause) {
        super(cause);
    }
    public DomainException(String message) {
        super(message);
    }

    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }

}
