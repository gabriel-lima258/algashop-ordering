package com.gtech.algashop.core.domain.model.product;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.commons.Quantity;

public class ProductTestDataBuilder {

    public static final ProductId DEFAULT_PRODUCT_ID = new ProductId();

    private ProductTestDataBuilder() {}

    public static Product.ProductBuilder aProduct() {
        return Product.builder()
                .id(DEFAULT_PRODUCT_ID)
                .inStock(true)
                .productName(new ProductName("Notebook Max Pro"))
                .quantityInStock(new Quantity(100))
                .price(new Money("4500.00"));
    }

    public static Product.ProductBuilder aProductUnavailable() {
        return Product.builder()
                .id(new ProductId())
                .inStock(false)
                .productName(new ProductName("Desktop Pro"))
                .quantityInStock(Quantity.ZERO)
                .price(new Money("9500.00"));
    }

    public static Product.ProductBuilder aProductRamMemory() {
        return Product.builder()
                .id(new ProductId())
                .inStock(true)
                .productName(new ProductName("16GB RAM"))
                .quantityInStock(new Quantity(30))
                .price(new Money("200.00"));
    }

    public static Product.ProductBuilder aProductMousePad() {
        return Product.builder()
                .id(new ProductId())
                .inStock(true)
                .productName(new ProductName("Mouse Pad Gamer"))
                .quantityInStock(new Quantity(50))
                .price(new Money("120.00"));
    }
}
