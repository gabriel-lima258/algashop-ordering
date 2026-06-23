package com.gtech.algashop.core.domain.model.product;

import com.gtech.algashop.core.domain.model.commons.Money;
import lombok.Builder;

import java.util.Objects;

@Builder
public record Product(
        ProductId id,
        ProductName productName,
        Money price,
        Boolean inStock
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
}
