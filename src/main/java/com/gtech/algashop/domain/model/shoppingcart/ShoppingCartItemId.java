package com.gtech.algashop.domain.model.entity.VO.id;

import com.gtech.algashop.domain.model.IdGenerator;
import io.hypersistence.tsid.TSID;

import java.util.Objects;

public record ShoppingCartItemId(TSID id) {

    public ShoppingCartItemId {
        Objects.requireNonNull(id);
    }

    public ShoppingCartItemId() {
        this(IdGenerator.generateTSID());
    }

    public ShoppingCartItemId(Long value) {
        this(TSID.from(value));
    }

    public ShoppingCartItemId(String value) {
        this(TSID.from(value));
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
