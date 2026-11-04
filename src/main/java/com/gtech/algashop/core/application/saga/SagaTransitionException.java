package com.gtech.algashop.core.application.saga;

public class SagaTransitionException extends RuntimeException {

    private SagaTransitionException(String message) {
        super(message);
    }

    public static SagaTransitionException notAllowed(Object aggregateId, SagaStatus status, Enum<?> step) {
        return new SagaTransitionException(
                "Transition not allowed for saga of aggregate %s in status %s and step %s"
                        .formatted(aggregateId, status, step));
    }

    public static SagaTransitionException statusChange(Object aggregateId, SagaStatus from, SagaStatus to) {
        return new SagaTransitionException(
                "Saga of aggregate %s cannot change status from %s to %s".formatted(aggregateId, from, to));
    }

    public static SagaTransitionException stepChange(Object aggregateId, Enum<?> from, Enum<?> to) {
        return new SagaTransitionException(
                "Saga of aggregate %s cannot change step from %s to %s".formatted(aggregateId, from, to));
    }
}
