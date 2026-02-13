package com.gtech.algashop.domain.entity.VO;

import com.gtech.algashop.domain.util.FieldValidations;

import java.util.Objects;

import static com.gtech.algashop.domain.exceptions.ErrorMessages.VALIDATION_ERROR_EMAIL_IS_INVALID;

public record Email(String email) {

    public Email {
        Objects.requireNonNull(email);
        FieldValidations.requiresEmailValid(email, VALIDATION_ERROR_EMAIL_IS_INVALID);
    }

    @Override
    public String toString() {
        return email;
    }
}
