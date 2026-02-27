package com.gtech.algashop.domain.model.entity.VO;

import com.gtech.algashop.domain.model.FieldValidations;

import java.util.Objects;

import static com.gtech.algashop.domain.model.ErrorMessages.VALIDATION_ERROR_EMAIL_IS_INVALID;

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
