package com.gtech.algashop.domain.entity.VO;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldCreateMoneyWithBigDecimal() {
        Money money = new Money(new BigDecimal("10"));

        assertThat(money.money()).isEqualByComparingTo("10.00");
    }

    @Test
    void shouldCreateMoneyWithString() {
        Money money = new Money("10.5");

        assertThat(money.money()).isEqualByComparingTo("10.50");
    }

    @Test
    void shouldRoundUsingHalfEven() {
        Money m1 = new Money(new BigDecimal("2.345"));
        Money m2 = new Money(new BigDecimal("2.355"));

        assertThat(m1.money()).isEqualByComparingTo("2.34");
        assertThat(m2.money()).isEqualByComparingTo("2.36");
    }

    @Test
    void shouldNotAllowNullMoney() {
        assertThatThrownBy(() -> new Money((BigDecimal) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNegativeMoney() {
        assertThatThrownBy(() -> new Money(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void zeroShouldBeZero() {
        assertThat(Money.ZERO.money()).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldAddMoney() {
        Money m1 = new Money("10.00");
        Money m2 = new Money("5.50");

        Money result = m1.add(m2);

        assertThat(result.money()).isEqualByComparingTo("15.50");
    }

    @Test
    void shouldNotAllowNullInAdd() {
        Money m1 = new Money("10");

        assertThatThrownBy(() -> m1.add(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldMultiplyMoneyByQuantity() {
        Money money = new Money("10.00");
        Quantity quantity = new Quantity(3);

        Money result = money.multiply(quantity);

        assertThat(result.money()).isEqualByComparingTo("30.00");
    }

    @Test
    void shouldNotAllowNullQuantityInMultiply() {
        Money money = new Money("10");

        assertThatThrownBy(() -> money.multiply(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowQuantityLessThanOne() {
        Money money = new Money("10");
        Quantity quantity = new Quantity(0);

        assertThatThrownBy(() -> money.multiply(quantity))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldDivideMoney() {
        Money m1 = new Money("10.00");
        Money m2 = new Money("2.00");

        Money result = m1.divide(m2);

        assertThat(result.money()).isEqualByComparingTo("5.00");
    }

    @Test
    void shouldNotDivideByZero() {
        Money m1 = new Money("10.00");
        Money zero = Money.ZERO;

        assertThatThrownBy(() -> m1.divide(zero))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void shouldReturnZeroWhenEqual() {
        Money m1 = new Money("10.00");
        Money m2 = new Money("10.00");

        assertThat(m1.compareTo(m2)).isZero();
    }

    @Test
    void shouldReturnPositiveWhenGreater() {
        Money m1 = new Money("15.00");
        Money m2 = new Money("10.00");

        assertThat(m1.compareTo(m2)).isGreaterThan(0);
    }

    @Test
    void shouldReturnNegativeWhenLess() {
        Money m1 = new Money("5.00");
        Money m2 = new Money("10.00");

        assertThat(m1.compareTo(m2)).isLessThan(0);
    }

    @Test
    void shouldNotMutateOriginalWhenAdding() {
        Money m1 = new Money("10.00");
        Money m2 = new Money("5.00");

        Money result = m1.add(m2);

        assertThat(m1.money()).isEqualByComparingTo("10.00");
        assertThat(result.money()).isEqualByComparingTo("15.00");
    }

    @Test
    void shouldReturnStringRepresentation() {
        Money money = new Money("10.50");

        assertThat(money.toString()).hasToString("10.50");
    }

}