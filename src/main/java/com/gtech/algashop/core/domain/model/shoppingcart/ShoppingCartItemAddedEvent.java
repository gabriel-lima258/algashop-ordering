package com.gtech.algashop.core.domain.model.shoppingcart;

import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.product.ProductId;

import java.time.OffsetDateTime;

public record ShoppingCartItemAddedEvent(
        ShoppingCartId shoppingCartId,
        CustomerId customerId,
        ProductId productId,
        OffsetDateTime addedAt
) {
}
