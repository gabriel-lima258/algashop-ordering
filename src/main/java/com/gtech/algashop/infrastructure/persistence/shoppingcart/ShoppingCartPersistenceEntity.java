package com.gtech.algashop.infrastructure.persistence.shoppingcart;

import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// implementação completa do modelo entity
@Entity
@NoArgsConstructor
@Getter
@Setter
@ToString(of = "id")
@Table(name = "shopping_cart")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class ShoppingCartPersistenceEntity {
    @Id
    @EqualsAndHashCode.Include
    private UUID id;

    @JoinColumn
    @ManyToOne(optional = false)
    private CustomerPersistenceEntity customer;
    private BigDecimal totalAmount;
    private Integer totalItems;

    @CreatedDate
    private OffsetDateTime createdAt;
    @CreatedBy
    private UUID createdByUserId;
    @LastModifiedDate
    private OffsetDateTime lastModifiedAt;
    @LastModifiedBy
    private UUID lastModifiedByUserId;

    // optimistic lock version, previne concorrencia
    @Version
    private Long version;

    @OneToMany(mappedBy = "shoppingCart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ShoppingCartItemPersistenceEntity> items = new HashSet<>();

    @Builder
    public ShoppingCartPersistenceEntity(UUID id, CustomerPersistenceEntity customer, BigDecimal totalAmount, Integer totalItems, OffsetDateTime createdAt, Set<ShoppingCartItemPersistenceEntity> items) {
        this.id = id;
        this.customer = customer;
        this.totalAmount = totalAmount;
        this.totalItems = totalItems;
        this.createdAt = createdAt;
        this.replaceItems(items);
    }

    // instancia a referencia do pai Order aos filhos Items
    public void replaceItems(Set<ShoppingCartItemPersistenceEntity> items) {
        if (items == null || items.isEmpty()) {
            this.setItems(new HashSet<>());
            return;
        }
        items.forEach(i -> i.setShoppingCart(this));
        this.setItems(items);
    }

    // replace singular
    public void addItem(ShoppingCartItemPersistenceEntity item) {
        if (item == null) {
            return;
        }
        if (this.getItems() == null) {
            this.setItems(new HashSet<>());
        }
        item.setShoppingCart(this);
        this.getItems().add(item);
    }

    public UUID getCustomerId() {
        if (this.customer == null) {
            return null;
        }
        return customer.getId();
    }
}
