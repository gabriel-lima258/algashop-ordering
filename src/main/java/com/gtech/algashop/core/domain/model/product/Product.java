package com.gtech.algashop.core.domain.model.product;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.commons.Quantity;
import lombok.Builder;

import java.util.Objects;

@Builder
public record Product(
        ProductId id,
        ProductName productName,
        Money price,
        Boolean inStock,
        Quantity quantityInStock
) {
    public Product {
        Objects.requireNonNull(id);
        Objects.requireNonNull(productName);
        Objects.requireNonNull(price);
        Objects.requireNonNull(inStock);
    }

    public void checkOutOfStock() {
        if (!inStock) {
            throw new ProductOutOfStockException(this.id());
        }
    }

    // valida a quantidade de produto antes de iniciar o fluxo saga
    public void checkForQuantity(Quantity quantity) {
        Objects.requireNonNull(quantity);
        checkOutOfStock();
        if (this.quantityInStock() != null && this.quantityInStock().compareTo(quantity) < 0) {
            throw new InsufficientQuantityException(this.productName(), quantity);
        }
    }

    private boolean isOutOfStock() {
        return !inStock();
    }
}
