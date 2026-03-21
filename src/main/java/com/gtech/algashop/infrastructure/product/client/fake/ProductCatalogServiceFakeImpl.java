package com.gtech.algashop.infrastructure.product.client.fake;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.product.ProductName;
import com.gtech.algashop.domain.model.product.ProductId;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import org.springframework.stereotype.Component;

import java.util.Optional;

//@Component
public class ProductCatalogServiceFakeImpl implements ProductCatalogService {
    @Override
    public Optional<Product> ofId(ProductId productId) {
        Product product = Product.builder().id(productId)
                .inStock(true)
                .productName(new ProductName("Notebook"))
                .price(new Money("3000"))
                .build();
        return Optional.of(product);
    }
}
