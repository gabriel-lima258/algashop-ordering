package com.gtech.algashop.domain.entity.VO;

import com.gtech.algashop.domain.util.FieldValidations;

public record ProductName(String name) {

    public ProductName {
        FieldValidations.requiresNonBlank(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
