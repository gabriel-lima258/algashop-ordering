package com.gtech.algashop.core.domain.model.commons;

import com.gtech.algashop.core.domain.model.FieldValidations;
import lombok.Builder;

import java.util.Objects;

@Builder(toBuilder = true)
public record Address(
        String street,
        String complement,
        String neighborhood,
        String number,
        String city,
        String state,
        ZipCode zipCode
) {

    // cria um address ja populado
    public Address {
        FieldValidations.requiresNonBlank(street);
        FieldValidations.requiresNonBlank(neighborhood);
        FieldValidations.requiresNonBlank(number);
        FieldValidations.requiresNonBlank(city);
        FieldValidations.requiresNonBlank(state);
        Objects.requireNonNull(zipCode);
    }
}
