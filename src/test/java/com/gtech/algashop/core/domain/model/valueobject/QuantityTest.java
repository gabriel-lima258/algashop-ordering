package com.gtech.algashop.core.domain.model.valueobject;

import com.gtech.algashop.core.domain.model.commons.Quantity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class QuantityTest {

    @Test
    void shouldCreateValidQuantity() {
        Quantity quantity = new Quantity(10);

        Assertions.assertThat(quantity.quantity()).isEqualTo(10);
    }

    @Test
    void shouldAllowZeroQuantity() {
        Quantity quantity = new Quantity(0);

        Assertions.assertThat(quantity.quantity()).isZero();
    }

    @Test
    void shouldNotAllowNullQuantity() {
        Assertions.assertThatThrownBy(() -> new Quantity(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNegativeQuantity() {
        Assertions.assertThatThrownBy(() -> new Quantity(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldAddQuantities() {
        Quantity q1 = new Quantity(5);
        Quantity q2 = new Quantity(3);

        Quantity result = q1.add(q2);

        Assertions.assertThat(result.quantity()).isEqualTo(8);
    }

    @Test
    void shouldNotMutateOriginalWhenAdding() {
        Quantity q1 = new Quantity(5);
        Quantity q2 = new Quantity(3);

        Quantity result = q1.add(q2);

        Assertions.assertThat(q1.quantity()).isEqualTo(5);
        Assertions.assertThat(result.quantity()).isEqualTo(8);
    }

    @Test
    void shouldNotAllowNullInAdd() {
        Quantity q1 = new Quantity(5);

        Assertions.assertThatThrownBy(() -> q1.add(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnZeroWhenEqual() {
        Quantity q1 = new Quantity(5);
        Quantity q2 = new Quantity(5);

        Assertions.assertThat(q1.compareTo(q2)).isZero();
    }

    @Test
    void shouldReturnNegativeWhenLessThan() {
        Quantity q1 = new Quantity(3);
        Quantity q2 = new Quantity(5);

        Assertions.assertThat(q1.compareTo(q2)).isLessThan(0);
    }

    @Test
    void shouldReturnPositiveWhenGreaterThan() {
        Quantity q1 = new Quantity(10);
        Quantity q2 = new Quantity(5);

        Assertions.assertThat(q1.compareTo(q2)).isGreaterThan(0);
    }

    @Test
    void shouldReturnStringRepresentation() {
        Quantity quantity = new Quantity(7);

        Assertions.assertThat(quantity.toString()).hasToString("7");
    }

}