package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.costumer.CustomerId;

import java.time.OffsetDateTime;

public record ShoppingCartCreatedEvent(
        ShoppingCartId shoppingCartId,
        CustomerId customerId,
        OffsetDateTime createdAt
) {
}
