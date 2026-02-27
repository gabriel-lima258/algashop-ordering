package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.product.ProductId;

public interface ShoppingCartProductAdjustmentService {
    // services para atualizaçoes em massa em do shopping em order
    void adjustPrice(ProductId productId, Money updatedPrice);
    void changeAvailability(ProductId productId, boolean available);
}
