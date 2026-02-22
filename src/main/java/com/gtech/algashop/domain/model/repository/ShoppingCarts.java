package com.gtech.algashop.domain.model.repository;

import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.ShoppingCart;
import com.gtech.algashop.domain.model.entity.VO.Money;
import com.gtech.algashop.domain.model.entity.VO.id.CustomerId;
import com.gtech.algashop.domain.model.entity.VO.id.OrderId;
import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartId;

import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * REPOSITÓRIO DE DOMÍNIO para o Aggregate Root Shopping cart.
 */
public interface ShoppingCarts extends RemoveCapableRepository<ShoppingCart, ShoppingCartId> {
    Optional<ShoppingCart> ofCustomer(CustomerId customerId);
}
