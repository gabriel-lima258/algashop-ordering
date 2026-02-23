package com.gtech.algashop.domain.model.service;

import com.gtech.algashop.domain.model.entity.VO.Money;
import com.gtech.algashop.domain.model.entity.VO.id.ProductId;

public interface ShoppingCartProductAdjustmentService {
    // services para atualizaçoes em massa em do shopping em order
    void adjustPrice(ProductId productId, Money updatedPrice);
    void changeAvailability(ProductId productId, boolean available);
}
