package com.gtech.algashop.domain.model.entity.VO;

import java.util.Objects;

import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.VALIDATION_ERROR_DOCUMENT_IS_NULL;

public record Document(String document) {

    public Document {
        Objects.requireNonNull(document);
        if (document.isBlank()) {
            throw new IllegalArgumentException(VALIDATION_ERROR_DOCUMENT_IS_NULL);
        }
    }

    @Override
    public String toString() {
        return document;
    }
}
