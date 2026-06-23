package com.gtech.algashop.core.domain.model.shoppingcart;

import com.gtech.algashop.core.domain.model.costumer.CustomerId;

import java.time.OffsetDateTime;

public record ShoppingCartEmptiedEvent(
        ShoppingCartId shoppingCartId,
        CustomerId customerId,
        OffsetDateTime emptiedAt
) {
}
