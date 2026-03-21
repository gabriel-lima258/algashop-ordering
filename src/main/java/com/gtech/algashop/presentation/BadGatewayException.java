package com.gtech.algashop.presentation;

public class BadGatewayException extends RuntimeException {
    public BadGatewayException() {
    }

    public BadGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
