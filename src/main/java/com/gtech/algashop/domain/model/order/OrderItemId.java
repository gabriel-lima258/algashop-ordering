package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.IdGenerator;
import io.hypersistence.tsid.TSID;

import java.util.Objects;

public record OrderItemId(TSID id) {

    public OrderItemId {
        Objects.requireNonNull(id);
    }

    public OrderItemId() {
        this(IdGenerator.generateTSID());
    }

    public OrderItemId(Long value) {
        this(TSID.from(value));
    }

    public OrderItemId(String value) {
        this(TSID.from(value));
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
