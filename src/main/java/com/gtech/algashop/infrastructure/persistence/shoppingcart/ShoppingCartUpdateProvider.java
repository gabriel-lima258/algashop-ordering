package com.gtech.algashop.infrastructure.persistence.shoppingcart;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.product.ProductId;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartProductAdjustmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ShoppingCartUpdateProvider implements ShoppingCartProductAdjustmentService {

    private final ShoppingCartJpaEntityRepository shoppingCartJpaEntityRepository;

    // ajuste de preço do shopping
    @Override
    @Transactional
    public void adjustPrice(ProductId productId, Money updatedPrice) {
        // atualiza o preço do carrinho
        shoppingCartJpaEntityRepository.updateItemPrice(productId.value(), updatedPrice.money());
        // recalcula o total avaliado
        shoppingCartJpaEntityRepository.recalculateTotalForCartWithProduct(productId.value());

    }

    @Override
    @Transactional
    public void changeAvailability(ProductId productId, boolean available) {
        // verifica a disposição do produto
        shoppingCartJpaEntityRepository.updateItemAvailability(productId.value(), available);
    }
}
