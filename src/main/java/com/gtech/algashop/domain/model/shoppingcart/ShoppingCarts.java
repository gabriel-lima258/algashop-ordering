package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.RemoveCapableRepository;
import com.gtech.algashop.domain.model.costumer.CustomerId;

import java.util.Optional;

/**
 * REPOSITÓRIO DE DOMÍNIO para o Aggregate Root Shopping cart.
 */
public interface ShoppingCarts extends RemoveCapableRepository<ShoppingCart, ShoppingCartId> {
    Optional<ShoppingCart> ofCustomer(CustomerId customerId);
}
