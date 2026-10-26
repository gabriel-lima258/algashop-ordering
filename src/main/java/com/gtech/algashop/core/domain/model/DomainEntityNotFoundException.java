package com.gtech.algashop.core.domain.model;

public class DomainEntityNotFoundException extends RuntimeException {

    public DomainEntityNotFoundException() {
    }

    public DomainEntityNotFoundException(Throwable cause) {
        super(cause);
    }
    public DomainEntityNotFoundException(String message) {
        super(message);
    }

    public DomainEntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
