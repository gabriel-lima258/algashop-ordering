package com.gtech.algashop.domain.model.commons;

import java.util.Objects;

public record ZipCode(String zipcode) {

    public ZipCode {
        Objects.requireNonNull(zipcode);

        if(zipcode.isBlank()) {
            throw new IllegalArgumentException();
        }
        if(zipcode.length() != 5) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString() {
        return zipcode;
    }
}
