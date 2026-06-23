package com.gtech.algashop.core.domain.model.valueobject;

import com.gtech.algashop.core.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.core.domain.model.costumer.LoyaltyValueException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LoyaltyPointsTest {

    @Test
    void shouldGenerateWithPoint() {
        LoyaltyPoints loyaltyPoints = new LoyaltyPoints(10);
        Assertions.assertThat(loyaltyPoints.point()).isEqualTo(10);
    }

    @Test
    void shouldAddPoint() {
        LoyaltyPoints loyaltyPoints = new LoyaltyPoints(10);
        Assertions.assertThat(loyaltyPoints.add(5).point()).isEqualTo(15);
    }

    @Test
    void shouldNotAddPoint() {
        LoyaltyPoints loyaltyPoints = new LoyaltyPoints(10);

        Assertions.assertThatExceptionOfType(LoyaltyValueException.class)
                        .isThrownBy(() -> loyaltyPoints.add(-5));

        Assertions.assertThat(loyaltyPoints.point()).isEqualTo(10);
    }

    @Test
    void shouldInitiateZeroPoint() {
        LoyaltyPoints loyaltyPoints = new LoyaltyPoints();

        Assertions.assertThat(loyaltyPoints.point()).isZero();
    }
}