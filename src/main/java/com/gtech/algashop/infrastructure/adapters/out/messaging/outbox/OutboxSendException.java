package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

public class OutboxSendException extends RuntimeException {
    public OutboxSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public OutboxSendException(String message) {
        super(message);
    }
}
