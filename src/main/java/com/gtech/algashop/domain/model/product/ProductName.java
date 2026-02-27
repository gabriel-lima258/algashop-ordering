package com.gtech.algashop.domain.model.entity.VO;

import com.gtech.algashop.domain.model.FieldValidations;

public record ProductName(String name) {

    public ProductName {
        FieldValidations.requiresNonBlank(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
