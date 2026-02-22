package com.gtech.algashop.domain.model.entity;

import com.gtech.algashop.domain.model.entity.VO.Money;
import com.gtech.algashop.domain.model.entity.VO.Product;
import com.gtech.algashop.domain.model.entity.VO.Quantity;
import com.gtech.algashop.domain.model.entity.VO.id.CustomerId;
import com.gtech.algashop.domain.model.entity.VO.id.ProductId;
import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartId;
import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartItemId;
import com.gtech.algashop.domain.model.exceptions.ShoppingCartDoesNotContainItemException;
import com.gtech.algashop.domain.model.exceptions.ShoppingCartDoesNotContainProductException;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

public class ShoppingCart implements AggregateRoot<ShoppingCartId> {

    private ShoppingCartId id;
    private CustomerId customerId;
    private Money totalAmount;
    private Quantity totalItems;
    private OffsetDateTime createdAt;

    private Set<ShoppingCartItem> items;

    // controle de concorrencia no banco
    private Long version;

    @Builder(builderClassName = "ExistingShoppingCartBuilder", builderMethodName = "existing")
    public ShoppingCart(ShoppingCartId id, Long version, CustomerId customerId, Money totalAmount,
                        Quantity totalItems, OffsetDateTime createdAt, Set<ShoppingCartItem> items) {
        this.setId(id);
        this.setVersion(version);
        this.setCustomerId(customerId);
        this.setTotalAmount(totalAmount);
        this.setTotalItems(totalItems);
        this.setCreatedAt(createdAt);
        this.setItems(items);
    }

    public static ShoppingCart startShopping(CustomerId customerId) {
        return new ShoppingCart(
                new ShoppingCartId(),
                null,
                customerId,
                Money.ZERO,
                Quantity.ZERO,
                OffsetDateTime.now(),
                new HashSet<>()
        );
    }

    /////////////////////////////////////
    ///  METHODS
    ////////////////////////////////////

    public void empty() {
        this.items.clear();
        this.setTotalAmount(Money.ZERO);
        this.setTotalItems(Quantity.ZERO);
    }

    public void addItem(Product product, Quantity quantity) {
        Objects.requireNonNull(product);
        Objects.requireNonNull(quantity);

        product.checkOutOfStock();

        ShoppingCartItem shoppingCartItem = ShoppingCartItem.brandNew()
                .shoppingCartId(this.id())
                .productId(product.id())
                .productName(product.productName())
                .price(product.price())
                .quantity(quantity)
                .available(product.inStock())
                .build();

        searchItemByProduct(product.id())
                .ifPresentOrElse(i -> updatedItem(i, product, quantity),
                        () -> insertItem(shoppingCartItem));

        this.recalculateTotals();
    }

    public void removeItem(ShoppingCartItemId shoppingCartItemId) {
        ShoppingCartItem itemToRemove = this.findItem(shoppingCartItemId);
        this.items.remove(itemToRemove);
        this.recalculateTotals();
    }

    public void refreshItem(Product productItem) {
        ShoppingCartItem shoppingCartItem = this.findItem(productItem.id());
        shoppingCartItem.refresh(productItem);
        this.recalculateTotals();
    }

    public void changeQuantity(ShoppingCartItemId shoppingCartItemId, Quantity quantity) {
        ShoppingCartItem shoppingCartItem = this.findItem(shoppingCartItemId);
        shoppingCartItem.changeQuantity(quantity);
        this.recalculateTotals();
    }

    public boolean containsUnavailableItems() {
        return items.stream().anyMatch(i -> !i.available());
    }

    public boolean isEmpty() {
        return this.items().isEmpty();
    }

    /////////////////////////////////////
    ///  GETTERS
    ////////////////////////////////////

    public ShoppingCartId id() {
        return id;
    }

    public Long version() {
        return version;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public Quantity totalItems() {
        return totalItems;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public Set<ShoppingCartItem> items() {
        return Collections.unmodifiableSet(this.items);
    }

    /////////////////////////////////////
    ///  SETTERS
    ////////////////////////////////////

    private void setId(ShoppingCartId id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    private void setVersion(Long version) {
        this.version = version;
    }

    private void setCustomerId(CustomerId customerId) {
        Objects.requireNonNull(customerId);
        this.customerId = customerId;
    }

    private void setTotalAmount(Money totalAmount) {
        Objects.requireNonNull(totalAmount);
        this.totalAmount = totalAmount;
    }

    private void setTotalItems(Quantity totalItems) {
        Objects.requireNonNull(totalItems);
        this.totalItems = totalItems;
    }

    private void setCreatedAt(OffsetDateTime createdAt) {
        Objects.requireNonNull(createdAt);
        this.createdAt = createdAt;
    }

    private void setItems(Set<ShoppingCartItem> items) {
        Objects.requireNonNull(items);
        this.items = items;
    }

    /////////////////////////////////////
    ///  HELPERS
    ////////////////////////////////////

    private ShoppingCartItem findItem(ShoppingCartItemId shoppingCartItemId) {
        return this.items().stream()
                .filter(i -> i.id().equals(shoppingCartItemId))
                .findFirst()
                .orElseThrow(() -> new ShoppingCartDoesNotContainItemException(this.id(), shoppingCartItemId));
    }

    private ShoppingCartItem findItem(ProductId productId) {
        return this.items().stream()
                .filter(i -> i.productId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ShoppingCartDoesNotContainProductException(this.id(), productId));
    }

    private void recalculateTotals() {
        BigDecimal totalAmountValue = items.stream()
                .map(i -> i.totalAmount().money())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer totalItemsValue = items.stream()
                .map(i -> i.quantity().quantity())
                .reduce(0, Integer::sum);

        this.totalAmount = new Money(totalAmountValue);
        this.totalItems = new Quantity(totalItemsValue);
    }

    private void insertItem(ShoppingCartItem shoppingCartItem) {
        this.items.add(shoppingCartItem);
    }

    private Optional<ShoppingCartItem> searchItemByProduct(ProductId productId) {
        Objects.requireNonNull(productId);
        return this.items.stream()
                .filter(i -> i.productId().equals(productId))
                .findFirst();
    }

    private void updatedItem(ShoppingCartItem shoppingCartItem, Product product, Quantity quantity) {
        shoppingCartItem.refresh(product);
        shoppingCartItem.changeQuantity(shoppingCartItem.quantity().add(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCart that = (ShoppingCart) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
