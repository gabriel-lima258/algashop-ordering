package com.gtech.algashop.domain.model.entity.VO.id;

import com.gtech.algashop.domain.model.util.IdGenerator;
import io.hypersistence.tsid.TSID;

import java.util.Objects;

public record OrderId(TSID value) {

    public OrderId {
        Objects.requireNonNull(value);
    }

    public OrderId() {
        this(IdGenerator.generateTSID());
    }

    public OrderId(Long value) {
        this(TSID.from(value));
    }

    public OrderId(String value) {
        this(TSID.from(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
