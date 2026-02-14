package com.gtech.algashop.domain.entity.VO.id;

import com.gtech.algashop.domain.util.IdGenerator;
import io.hypersistence.tsid.TSID;

import java.util.Objects;

public record OrderId(TSID id) {

    public OrderId {
        Objects.requireNonNull(id);
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
        return id.toString();
    }
}
