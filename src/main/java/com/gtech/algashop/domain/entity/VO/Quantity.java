package com.gtech.algashop.domain.entity.VO;

import java.util.Objects;

public record Quantity(Integer quantity) implements Comparable<Quantity> {

    public Quantity {
        Objects.requireNonNull(quantity);
        if (quantity < 0) {
            throw new IllegalArgumentException();
        }
    }

    public Quantity add(Quantity value) {
        Objects.requireNonNull(value);
        return new Quantity(this.quantity + value.quantity);
    }

    @Override
    public int compareTo(Quantity value) {
        return this.quantity().compareTo(value.quantity);
    }

    @Override
    public String toString() {
        return quantity.toString();
    }
}
