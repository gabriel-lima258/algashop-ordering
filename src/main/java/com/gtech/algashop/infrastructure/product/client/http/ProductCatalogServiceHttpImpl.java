package com.gtech.algashop.infrastructure.product.client.http;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import com.gtech.algashop.domain.model.product.ProductId;
import com.gtech.algashop.domain.model.product.ProductName;
import com.gtech.algashop.presentation.BadGatewayException;
import com.gtech.algashop.presentation.GatewayTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.Optional;

// integracao real com o micro serviço de products
@Component
@RequiredArgsConstructor
public class ProductCatalogServiceHttpImpl implements ProductCatalogService {

    private final ProductCatalogApiClient productCatalogApiClient;

    @Override
    public Optional<Product> ofId(ProductId productId) {
        ProductResponse productResponse;

        try {
            productResponse = productCatalogApiClient.getById(productId.value());
        } catch (ResourceAccessException e) {
            throw new GatewayTimeoutException("Product catalog API timeout", e);
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        } catch (HttpClientErrorException e) {
            throw new BadGatewayException("Product catalog API bad gateway", e);
        }

        return Optional.of(
                Product.builder()
                        .id(new ProductId(productResponse.getId()))
                        .productName(new ProductName(productResponse.getName()))
                        .price(new Money(productResponse.getSalePrice()))
                        .inStock(productResponse.getInStock())
                        .build()
        );
    }
}
