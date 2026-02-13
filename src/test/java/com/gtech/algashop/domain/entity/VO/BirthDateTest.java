package com.gtech.algashop.domain.entity.VO;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.gtech.algashop.domain.exceptions.ErrorMessages.VALIDATION_ERROR_BIRTHDATE_MUST_IN_PAST;

class BirthDateTest {

    @Test
    void shouldCreateAValidBirthDate() {
        LocalDate date = LocalDate.of(2001, 9, 1);
        BirthDate birthDate = new BirthDate(date);
        Assertions.assertThat(birthDate.birthDate()).isEqualTo(date);
    }

    @Test
    void shouldNotAllowNullBirthDate() {
        Assertions.assertThatThrownBy(() -> new BirthDate(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowFutureDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        Assertions.assertThatThrownBy(() -> new BirthDate(tomorrow))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(VALIDATION_ERROR_BIRTHDATE_MUST_IN_PAST);
    }

    @Test
    void shouldAllowTodayAsBirthDate() {
        LocalDate today = LocalDate.now();
        BirthDate birthDate = new BirthDate(today);
        Assertions.assertThat(birthDate.age()).isZero();
    }

    @Test
    void shouldCalculateAgeWhenBirthdayNotOccurredYetThisYear() {
        LocalDate birthDateValue = LocalDate.now()
                .minusYears(20)
                .plusDays(1);

        BirthDate birthDate = new BirthDate(birthDateValue);
        Assertions.assertThat(birthDate.age()).isEqualTo(19);
    }

    @Test
    void shouldCalculateAgeCorrectly() {
        LocalDate twentyYearsAgo = LocalDate.now().minusYears(20);
        BirthDate birthDate = new BirthDate(twentyYearsAgo);
        Assertions.assertThat(birthDate.age()).isEqualTo(20);
    }

    @Test
    void shouldHandleVeryOldDates() {
        LocalDate date = LocalDate.of(1900, 1, 1);
        BirthDate birthDate = new BirthDate(date);
        Assertions.assertThat(birthDate.age()).isGreaterThan(100);
    }

    @Test
    void shouldReturnStringRepresentation() {
        LocalDate date = LocalDate.of(1990, 5, 10);
        BirthDate birthDate = new BirthDate(date);
        Assertions.assertThat(birthDate.toString()).hasToString("1990-05-10");
    }

}