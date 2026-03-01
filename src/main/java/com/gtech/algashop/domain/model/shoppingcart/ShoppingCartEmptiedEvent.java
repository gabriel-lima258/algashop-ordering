package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.product.ProductId;

import java.time.OffsetDateTime;

public record ShoppingCartEmptiedEvent(
        ShoppingCartId shoppingCartId,
        CustomerId customerId,
        OffsetDateTime emptiedAt
) {
}
