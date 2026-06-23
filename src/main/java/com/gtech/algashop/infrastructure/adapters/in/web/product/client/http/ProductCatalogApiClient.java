package com.gtech.algashop.infrastructure.adapters.in.web.product.client.http;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.UUID;
// conexão de rota com micro serviço de product
public interface ProductCatalogApiClient {

    @GetExchange(value = "/api/v1/products/{productId}", accept = "application/json")
    ProductResponse getById(@PathVariable UUID productId);
}
