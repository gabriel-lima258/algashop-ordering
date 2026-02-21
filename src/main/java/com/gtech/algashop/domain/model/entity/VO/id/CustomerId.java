package com.gtech.algashop.domain.model.entity.VO.id;

import com.gtech.algashop.domain.model.util.IdGenerator;
import io.hypersistence.tsid.TSID;

import java.util.Objects;
import java.util.UUID;

public record CustomerId(UUID value) {

    public CustomerId() {
        this(IdGenerator.generateTimeBasedUUID());
    }

    public CustomerId(UUID value) {
        Objects.requireNonNull(value);
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
