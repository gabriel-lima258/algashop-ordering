package com.gtech.algashop.infrastructure.shipping.client.rapidex;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * Configuração do client HTTP para a RapiDex API usando Spring 6 HTTP Interfaces.
 *
 * Esta classe registra um Bean do tipo `RapiDexAPICLient` gerado dinamicamente
 * pelo `HttpServiceProxyFactory`, evitando a necessidade de implementar o client
 * manualmente.
 *
 * Funcionamento:
 * 1. O `RestClient.Builder` é injetado pelo Spring Boot já configurado
 *    (serialização JSON, interceptors, observabilidade, etc.).
 *
 * 2. A URL base da API externa é lida do `application.yml`
 *    (`algashop.integrations.rapidex.url`).
 *
 * 3. É criado um `RestClient` com a baseUrl configurada.
 *
 * 4. O `RestClientAdapter` conecta o RestClient ao mecanismo de proxy HTTP.
 *
 * 5. O `HttpServiceProxyFactory` gera uma implementação dinâmica da interface
 *    `RapiDexAPICLient`, baseada nas anotações (@GetExchange, @PostExchange, etc.).
 *
 * Resultado:
 * - Permite chamar a API externa de forma tipada e declarativa.
 * - Centraliza a configuração de integração externa na camada de infraestrutura.
 * - Mantém o domínio desacoplado de detalhes HTTP.
 *
 * Fluxo na arquitetura hexagonal:
 * Domain Port → Adapter (ACL) → RapiDexAPICLient (proxy HTTP) → API externa/WireMock
 */
@Configuration
public class RapiDexAPIClientConfig {

    @Bean
    public RapiDexAPICLient rapiDexAPICLient(
            RestClient.Builder builder,
            @Value("${algashop.integrations.rapidex.url}") String rapiDexUrl) {
        RestClient restClient = builder.baseUrl(rapiDexUrl).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(RapiDexAPICLient.class);
    }
}
