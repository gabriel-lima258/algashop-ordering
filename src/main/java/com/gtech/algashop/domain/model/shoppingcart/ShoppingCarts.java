package com.gtech.algashop.domain.model.repository;

import com.gtech.algashop.domain.model.RemoveCapableRepository;
import com.gtech.algashop.domain.model.entity.ShoppingCart;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartId;

import java.util.Optional;

/**
 * REPOSITÓRIO DE DOMÍNIO para o Aggregate Root Shopping cart.
 */
public interface ShoppingCarts extends RemoveCapableRepository<ShoppingCart, ShoppingCartId> {
    Optional<ShoppingCart> ofCustomer(CustomerId customerId);
}
