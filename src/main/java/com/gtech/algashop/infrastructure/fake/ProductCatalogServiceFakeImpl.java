package com.gtech.algashop.infrastructure.fake;

import com.gtech.algashop.domain.model.entity.VO.Money;
import com.gtech.algashop.domain.model.entity.VO.Product;
import com.gtech.algashop.domain.model.entity.VO.ProductName;
import com.gtech.algashop.domain.model.entity.VO.id.ProductId;
import com.gtech.algashop.domain.model.service.ProductCatalogService;

import java.util.Optional;

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
