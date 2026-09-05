package com.gtech.algashop.core.ports.in.shoppingcart;

import java.math.BigDecimal;
import java.util.UUID;

public interface ForManagingShoppingCarts {
    void addItem(ShoppingCartItemInput input);
    UUID createNew(UUID customerId);
    void removeItem(UUID shoppingCartId, String shoppingCartItemId);
    void empty(UUID shoppingCartId);
    void delete(UUID shoppingCartId);

    // EVENTOS
    void changeProductAvailability(UUID productId, boolean available);
    void refreshProductPrice(UUID productId, BigDecimal salePrice);
}
