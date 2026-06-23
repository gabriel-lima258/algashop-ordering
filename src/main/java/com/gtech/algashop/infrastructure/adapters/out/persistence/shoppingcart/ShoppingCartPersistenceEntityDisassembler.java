package com.gtech.algashop.infrastructure.adapters.out.persistence.shoppingcart;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.commons.Quantity;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.product.ProductId;
import com.gtech.algashop.core.domain.model.product.ProductName;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartId;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartItem;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartItemId;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ShoppingCartPersistenceEntityDisassembler {

    public ShoppingCart toDomainEntity(ShoppingCartPersistenceEntity persistenceEntity) {
        return ShoppingCart.existing()
                .id(new ShoppingCartId(persistenceEntity.getId()))
                .customerId(new CustomerId(persistenceEntity.getCustomerId()))
                .totalAmount(new Money(persistenceEntity.getTotalAmount()))
                .totalItems(new Quantity(persistenceEntity.getTotalItems()))
                .createdAt(persistenceEntity.getCreatedAt())
                .items(new HashSet<>())
                .version(persistenceEntity.getVersion())
                .items(toDomainEntity(persistenceEntity.getItems()))
                .build();
    }

    private Set<ShoppingCartItem> toDomainEntity(Set<ShoppingCartItemPersistenceEntity> items) {
        return items.stream().map(this::toDomainEntity).collect(Collectors.toSet());
    }

    private ShoppingCartItem toDomainEntity(ShoppingCartItemPersistenceEntity item) {
        return ShoppingCartItem.existing()
                .id(new ShoppingCartItemId(item.getId()))
                .shoppingCartId(new ShoppingCartId(item.getShoppingCartId()))
                .productId(new ProductId(item.getProductId()))
                .name(new ProductName(item.getProductName()))
                .price(new Money(item.getPrice()))
                .quantity(new Quantity(item.getQuantity()))
                .totalAmount(new Money(item.getTotalAmount()))
                .available(item.getAvailable())
                .build();
    }

}
