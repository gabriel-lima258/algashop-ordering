package com.gtech.algashop.core.domain.model.shoppingcart;

import com.gtech.algashop.core.domain.model.RemoveCapableRepository;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.product.ProductId;

import java.util.List;
import java.util.Optional;

/**
 * REPOSITÓRIO DE DOMÍNIO para o Aggregate Root Shopping cart.
 */
public interface ShoppingCarts extends RemoveCapableRepository<ShoppingCart, ShoppingCartId> {
    Optional<ShoppingCart> ofCustomer(CustomerId customerId);
    List<ShoppingCart> findAllContainingItem(ProductId productId);
}
