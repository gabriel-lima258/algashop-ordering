package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.product.ProductId;

import java.time.OffsetDateTime;

public record ShoppingCartItemAddedEvent(
        ShoppingCartId shoppingCartId,
        CustomerId customerId,
        ProductId productId,
        OffsetDateTime addedAt
) {
}
