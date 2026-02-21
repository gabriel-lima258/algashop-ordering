package com.gtech.algashop.domain.model.entity;

import com.gtech.algashop.domain.model.entity.VO.Money;
import com.gtech.algashop.domain.model.entity.VO.Product;
import com.gtech.algashop.domain.model.entity.VO.ProductName;
import com.gtech.algashop.domain.model.entity.VO.Quantity;
import com.gtech.algashop.domain.model.entity.VO.id.ProductId;
import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartId;
import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartItemId;
import com.gtech.algashop.domain.model.exceptions.ShoppingCartItemIncompatibleProductException;
import lombok.Builder;

import java.util.Objects;

public class ShoppingCartItem {

    private ShoppingCartItemId id;
    private ShoppingCartId shoppingCartId;
    private ProductId productId;
    private ProductName name;
    private Money price;
    private Quantity quantity;
    private Money totalAmount;
    private Boolean available;

    @Builder(builderClassName = "ExistingShoppingCartItemBuilder", builderMethodName = "existing")
    public ShoppingCartItem(ShoppingCartItemId id, ShoppingCartId shoppingCartId, ProductId productId,
                            ProductName name, Money price, Quantity quantity, Money totalAmount,
                            Boolean available) {
        this.setId(id);
        this.setShoppingCartId(shoppingCartId);
        this.setProductId(productId);
        this.setName(name);
        this.setPrice(price);
        this.setQuantity(quantity);
        this.setTotalAmount(totalAmount);
        this.setAvailable(available);
    }

    @Builder(builderClassName = "BrandNewShoppingCartItemBuilder", builderMethodName = "brandNew")
    private static ShoppingCartItem createBrandNew(ShoppingCartId shoppingCartId,
                            ProductId productId, ProductName productName, Money price,
                            Quantity quantity, Boolean available) {

        ShoppingCartItem shoppingCartItem = new ShoppingCartItem(
                new ShoppingCartItemId(),
                shoppingCartId,
                productId,
                productName,
                price,
                quantity,
                Money.ZERO,
                available
        );

        shoppingCartItem.recalculateTotals();

        return shoppingCartItem;
    }

    /////////////////////////////////////
    ///  METHODS
    ////////////////////////////////////

    void refresh(Product product) {
        Objects.requireNonNull(product);
        Objects.requireNonNull(product.id());

        if (!product.id().equals(this.productId)) {
            throw new ShoppingCartItemIncompatibleProductException(this.id(), this.productId());
        }

        this.setName(product.productName());
        this.setPrice(product.price());
        this.setAvailable(product.inStock());
        this.recalculateTotals();
    }

    private void recalculateTotals() {
        this.setTotalAmount(this.price().multiply(this.quantity));
    }

    void changeQuantity(Quantity quantity) {
        Objects.requireNonNull(quantity);
        this.setQuantity(quantity);
        this.recalculateTotals();
    }

    /////////////////////////////////////
    ///  GETTERS
    ////////////////////////////////////

    public ShoppingCartItemId id() {
        return id;
    }

    public ShoppingCartId shoppingCartId() {
        return shoppingCartId;
    }

    public ProductId productId() {
        return productId;
    }

    public ProductName name() {
        return name;
    }

    public Money price() {
        return price;
    }

    public Quantity quantity() {
        return quantity;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public Boolean available() {
        return available;
    }

    /////////////////////////////////////
    ///  SETTERS
    ////////////////////////////////////

    public void setId(ShoppingCartItemId id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    public void setShoppingCartId(ShoppingCartId shoppingCartId) {
        Objects.requireNonNull(shoppingCartId);
        this.shoppingCartId = shoppingCartId;
    }

    public void setProductId(ProductId productId) {
        Objects.requireNonNull(productId);
        this.productId = productId;
    }

    public void setName(ProductName name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    public void setPrice(Money price) {
        Objects.requireNonNull(price);
        this.price = price;
    }

    public void setQuantity(Quantity quantity) {
        Objects.requireNonNull(quantity);
        if (quantity.equals(Quantity.ZERO)) {
            throw new IllegalArgumentException();
        }
        this.quantity = quantity;
    }

    public void setTotalAmount(Money totalAmount) {
        Objects.requireNonNull(totalAmount);
        this.totalAmount = totalAmount;
    }

    public void setAvailable(Boolean available) {
        Objects.requireNonNull(available);
        this.available = available;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ShoppingCartItem that = (ShoppingCartItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
