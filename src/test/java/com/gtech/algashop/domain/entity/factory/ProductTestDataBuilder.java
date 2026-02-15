package com.gtech.algashop.domain.entity.factory;

import com.gtech.algashop.domain.entity.VO.Money;
import com.gtech.algashop.domain.entity.VO.Product;
import com.gtech.algashop.domain.entity.VO.ProductName;
import com.gtech.algashop.domain.entity.VO.id.ProductId;

public class ProductTestDataBuilder {

    private ProductTestDataBuilder() {}

    public static Product.ProductBuilder aProduct() {
        return Product.builder()
                .id(new ProductId())
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
