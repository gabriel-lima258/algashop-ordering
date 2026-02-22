package com.gtech.algashop.infrastructure.persistence.disassembler;

import com.gtech.algashop.domain.model.entity.*;
import com.gtech.algashop.domain.model.entity.VO.*;
import com.gtech.algashop.domain.model.entity.VO.id.*;
import com.gtech.algashop.infrastructure.persistence.embeddable.AddressEmbeddable;
import com.gtech.algashop.infrastructure.persistence.embeddable.BillingEmbeddable;
import com.gtech.algashop.infrastructure.persistence.embeddable.RecipientEmbeddable;
import com.gtech.algashop.infrastructure.persistence.embeddable.ShippingEmbeddable;
import com.gtech.algashop.infrastructure.persistence.entity.OrderItemPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.entity.ShoppingCartItemPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.entity.ShoppingCartPersistenceEntity;
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
