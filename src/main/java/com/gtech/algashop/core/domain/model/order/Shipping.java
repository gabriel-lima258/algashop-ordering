package com.gtech.algashop.core.domain.model.order;

import com.gtech.algashop.core.domain.model.commons.Address;
import com.gtech.algashop.core.domain.model.commons.Money;
import lombok.Builder;

import java.time.LocalDate;
import java.util.Objects;

@Builder(toBuilder = true)
public record Shipping(
        Money cost,
        LocalDate expectedDate,
        Recipient recipient,
        Address address
) {
    public Shipping {
        Objects.requireNonNull(cost);
        Objects.requireNonNull(expectedDate);
        Objects.requireNonNull(recipient);
        Objects.requireNonNull(address);
    }
}
