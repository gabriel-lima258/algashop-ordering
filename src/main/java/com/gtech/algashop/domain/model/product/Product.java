package com.gtech.algashop.domain.model.entity.VO;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.entity.VO.id.ProductId;
import com.gtech.algashop.domain.model.exceptions.ProductOutOfStockException;
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
