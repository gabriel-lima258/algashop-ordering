package com.gtech.algashop.domain.model.commons;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal money) implements Comparable<Money> {

    // define o arredondamento:
    // 2.345 → 2.34 / 2.355 → 2.36
    private static final RoundingMode roundingMode = RoundingMode.HALF_EVEN;
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    public Money(String money) {
        this(new BigDecimal(money));
    }

    public Money(BigDecimal money) {
        Objects.requireNonNull(money);
        this.money = money.setScale(2, roundingMode);
        // signum retorna -1 = negativo, 0 = zero, 1 = positivo
        if (this.money.signum() == -1) {
            throw new IllegalArgumentException();
        }
    }

    public Money multiply(Quantity value) {
        Objects.requireNonNull(value);
        if (value.quantity() < 1) {
            throw new IllegalArgumentException();
        }
        BigDecimal multiplied = this.money.multiply(new BigDecimal(value.quantity()));
        return new Money(multiplied);
    }

    public Money add(Money value) {
        Objects.requireNonNull(value);
        return new Money(this.money.add(value.money));
    }

    public Money divide(Money value) {
        return new Money(this.money.divide(value.money, roundingMode));
    }

    @Override
    public int compareTo(Money value) {
        return this.money().compareTo(value.money);
    }

    @Override
    public String toString() {
        return money.toString();
    }
}
