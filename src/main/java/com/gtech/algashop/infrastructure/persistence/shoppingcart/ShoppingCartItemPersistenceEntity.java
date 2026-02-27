package com.gtech.algashop.infrastructure.persistence.shoppingcart;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "shopping_cart_item")
@Data
@ToString(of = "id")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartItemPersistenceEntity {
    @Id
    @EqualsAndHashCode.Include
    private Long id;
    private UUID productId;
    private String productName;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private Boolean available;

    @JoinColumn
    @ManyToOne(optional = false)
    private ShoppingCartPersistenceEntity shoppingCart;

    public UUID getShoppingCartId() {
        if (getShoppingCart() == null) return null;

        return getShoppingCart().getId();
    }
}
