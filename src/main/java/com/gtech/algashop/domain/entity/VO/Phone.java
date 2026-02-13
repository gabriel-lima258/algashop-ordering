package com.gtech.algashop.domain.entity.VO;

import java.util.Objects;

import static com.gtech.algashop.domain.exceptions.ErrorMessages.VALIDATION_ERROR_PHONE_IS_BLANK;

public record Phone(String phone) {

    public Phone {
        Objects.requireNonNull(phone);
        if (phone.isBlank()) {
            throw new IllegalArgumentException(VALIDATION_ERROR_PHONE_IS_BLANK);
        }
    }

    @Override
    public String toString() {
        return phone;
    }
}
