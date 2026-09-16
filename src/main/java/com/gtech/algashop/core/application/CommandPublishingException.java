package com.gtech.algashop.core.application;

import lombok.Getter;

@Getter
public class CommandPublishingException extends RuntimeException {

    private final IntegrationCommand command;

    // a causa vai junto: sem o super(..., e) o stack trace do broker sumiria do log
    public CommandPublishingException(String message, IntegrationCommand command, Throwable e) {
        super("%s Command=%s AggregateId=%s".formatted(message, command.getClass().getSimpleName(), command.getAggregateId()), e);
        this.command = command;
    }
}
