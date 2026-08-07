package com.gtech.algashop.infrastructure.adapters.out.web.product.client.http;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

import java.util.UUID;
// conexão de rota com micro serviço de product
//
// O QUE MUDOU: esta interface tinha o @Cacheable pendurado aqui. Ele saiu para o
// ResilientProductCatalogAPIClient. Hoje ela e so a declaracao da rota - nenhum
// comportamento (cache, retry, tratamento de erro) mora nela.
//
// Por que e melhor assim: o bean gerado a partir desta interface ja e um proxy do
// HttpServiceProxyFactory; colocar @Cacheable aqui obrigava o Spring a proxiar um proxy,
// e a excecao HTTP crua (404, timeout) atravessava o cache antes de alguem traduzi-la.
public interface ProductCatalogApiClient {

    @GetExchange(value = "/api/v1/products/{productId}", accept = "application/json")
    ProductResponse getById(@PathVariable UUID productId);
}
