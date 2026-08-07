package com.gtech.algashop.infrastructure.adapters.out.web.product.client.http;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.product.Product;
import com.gtech.algashop.core.domain.model.product.ProductCatalogService;
import com.gtech.algashop.core.domain.model.product.ProductId;
import com.gtech.algashop.core.domain.model.product.ProductName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

// integracao real com o micro serviço de products
//
// O que sobrou e a unica coisa que era realmente dela: ADAPTAR. Ela implementa a porta
// ProductCatalogService (do dominio) e traduz ProductResponse (DTO de infra) para
// Product (agregado). O dominio nao sabe que existe HTTP, cache ou retry.
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductCatalogServiceHttpImpl implements ProductCatalogService {

    // nao e mais a interface HTTP crua, e o decorator resiliente/cacheado
    private final ResilientProductCatalogAPIClient productCatalogApiClient;

    @Override
    public Optional<Product> ofId(ProductId productId) {
        return productCatalogApiClient.getById(productId.value()).map(productResponse ->
                Product.builder()
                        .id(new ProductId(productResponse.getId()))
                        .productName(new ProductName(productResponse.getName()))
                        .price(new Money(productResponse.getSalePrice()))
                        .inStock(productResponse.getInStock())
                        .build()
        );
    }
}
