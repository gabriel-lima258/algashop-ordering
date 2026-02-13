package com.gtech.algashop.domain.entity.VO;

import com.gtech.algashop.domain.exceptions.LoyaltyValueException;

import java.util.Objects;

public record LoyaltyPoints(Integer point) implements Comparable<LoyaltyPoints> {

    public static final LoyaltyPoints ZERO = new LoyaltyPoints(0);

    public LoyaltyPoints() {
        this(0);
    }

    // construtor validação, não nulo e não negativo
    public LoyaltyPoints(Integer point) {
        Objects.requireNonNull(point);

        if (point < 0) {
            throw new LoyaltyValueException();
        }

        this.point = point;
    }

    // sobrecarga de metodo points.add(10) na verdade chama points.add(new LoyaltyPoints())
    public LoyaltyPoints add(Integer point) {
        // chama o metodo abaixo
        return add(new LoyaltyPoints(point));
    }

    // sobrecarga points.add(new LoyaltyPoints(10))
    public LoyaltyPoints add(LoyaltyPoints loyaltyPoints) {
        Objects.requireNonNull(loyaltyPoints);

        if (loyaltyPoints.point() <= 0) {
            throw new LoyaltyValueException();
        }

        // cria um novo objeto com valor somado
        return new LoyaltyPoints(this.point + loyaltyPoints.point);
    }

    @Override
    public String toString() {
        return point.toString();
    }

    @Override
    public int compareTo(LoyaltyPoints o) {
        return this.point().compareTo(o.point());
    }
}
