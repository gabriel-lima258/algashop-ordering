package com.gtech.algashop.domain.model.entity.VO;

import com.gtech.algashop.domain.model.commons.*;
import lombok.Builder;

import java.util.Objects;

@Builder
public record Billing(
        FullName fullName,
        Document document,
        Phone phone,
        Email email,
        Address address
) {
    public Billing {
        Objects.requireNonNull(fullName);
        Objects.requireNonNull(document);
        Objects.requireNonNull(phone);
        Objects.requireNonNull(email);
        Objects.requireNonNull(address);
    }
}
