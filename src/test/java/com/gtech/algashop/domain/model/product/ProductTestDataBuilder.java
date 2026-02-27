package com.gtech.algashop.domain.model.product;

import com.gtech.algashop.domain.model.commons.Money;

public class ProductTestDataBuilder {

    public static final ProductId DEFAULT_PRODUCT_ID = new ProductId();

    private ProductTestDataBuilder() {}

    public static Product.ProductBuilder aProduct() {
        return Product.builder()
                .id(DEFAULT_PRODUCT_ID)
                .inStock(true)
                .productName(new ProductName("Notebook Max Pro"))
                .price(new Money("4500.00"));
    }

    public static Product.ProductBuilder aProductUnavailable() {
        return Product.builder()
                .id(new ProductId())
                .inStock(false)
                .productName(new ProductName("Desktop Pro"))
                .price(new Money("9500.00"));
    }

    public static Product.ProductBuilder aProductRamMemory() {
        return Product.builder()
                .id(new ProductId())
                .inStock(true)
                .productName(new ProductName("16GB RAM"))
                .price(new Money("200.00"));
    }

    public static Product.ProductBuilder aProductMousePad() {
        return Product.builder()
                .id(new ProductId())
                .inStock(true)
                .productName(new ProductName("Mouse Pad Gamer"))
                .price(new Money("120.00"));
    }
}
