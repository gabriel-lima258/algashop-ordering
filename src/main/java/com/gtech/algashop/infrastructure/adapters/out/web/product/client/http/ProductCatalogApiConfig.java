package com.gtech.algashop.infrastructure.adapters.out.web.product.client.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;

@Configuration
public class ProductCatalogApiConfig {

    @Bean
    public ProductCatalogApiClient productCatalogApiClient(RestClient.Builder builder,
        @Value("${algashop.integrations.product-catalog.url}") String url) {
        RestClient restClient = builder.baseUrl(url)
                .requestFactory(generateClientHttpRequestFactory())
                .build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(ProductCatalogApiClient.class);
    }

    // função para definir a resiliencia de timeout
    private ClientHttpRequestFactory generateClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(7));
        factory.setConnectTimeout(Duration.ofSeconds(3));
        return factory;
    }
}
