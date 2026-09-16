package com.gtech.algashop.core.application;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public interface IntegrationCommand {
    @JsonIgnore
    String getAggregateId();
    @JsonIgnore
    UUID getIdempotencyKey();
}
