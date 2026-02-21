package com.gtech.algashop.domain.model.entity.VO;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.VALIDATION_ERROR_BIRTHDATE_MUST_IN_PAST;

public record BirthDate(LocalDate birthDate) {

    public BirthDate {
        Objects.requireNonNull(birthDate);

        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(VALIDATION_ERROR_BIRTHDATE_MUST_IN_PAST);
        }
    }

    public Integer age() {
        return Period.between(this.birthDate, LocalDate.now()).getYears();
    }


    @Override
    public String toString() {
        return birthDate.toString();
    }
}
