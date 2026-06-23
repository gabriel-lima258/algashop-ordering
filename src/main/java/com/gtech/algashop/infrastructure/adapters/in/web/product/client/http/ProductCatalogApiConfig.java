package com.gtech.algashop.infrastructure.adapters.in.web.product.client.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class ProductCatalogApiConfig {

    @Bean
    public ProductCatalogApiClient productCatalogApiClient(RestClient.Builder builder,
        @Value("${algashop.integrations.product-catalog.url}") String url) {
        RestClient restClient = builder.baseUrl(url).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(ProductCatalogApiClient.class);
    }
}
