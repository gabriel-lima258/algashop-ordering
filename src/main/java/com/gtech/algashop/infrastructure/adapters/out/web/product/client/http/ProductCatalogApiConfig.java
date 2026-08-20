package com.gtech.algashop.infrastructure.adapters.out.web.product.client.http;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;
import java.util.Collections;

// Monta o client HTTP do product-catalog e fecha o circuito do papel de CLIENT OAuth2:
// o registration declarado no YAML descreve COMO obter token, o manager sabe EXECUTAR o
// client_credentials, e o interceptor daqui e quem de fato ANEXA o Bearer em cada request.
// Sem este interceptor a chamada sairia sem Authorization e tomaria 401 do catalogo.
@Configuration
public class ProductCatalogApiConfig {

    @Bean
    public ProductCatalogApiClient productCatalogApiClient(RestClient.Builder builder,
           ProductCatalogIntegrationProperties properties,
           @Qualifier("productCatalogAPIClientInterceptor") OAuth2ClientHttpRequestInterceptor interceptor) {

        RestClient restClient = builder.baseUrl(properties.getUrl())
                .requestFactory(generateClientHttpRequestFactory())
                .requestInterceptor(interceptor)
                .build();

        // ProductCatalogApiClient e so uma interface com @GetExchange: o proxy gerado aqui
        // traduz cada metodo anotado em chamada do RestClient acima - e por isso herda
        // baseUrl, timeouts e o interceptor OAuth2 sem nenhum codigo HTTP escrito a mao.
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory.builderFor(adapter).build();
        return proxyFactory.createClient(ProductCatalogApiClient.class);
    }

    @Bean("productCatalogAPIClientInterceptor")
    public OAuth2ClientHttpRequestInterceptor productCatalogAPIClientInterceptor(
            ProductCatalogIntegrationProperties properties,
            OAuth2AuthorizedClientManager manager
    ) {
        // Antes de cada request o interceptor pede ao manager um token valido: se ja ha um
        // cacheado e nao expirado, reusa; senao o manager vai ao /oauth2/token e renova.
        // O registrationIdResolver diz QUAL registration do YAML usar (a URL alvo nao
        // identifica o client - a escolha e nossa, por configuracao).
        var interceptor = new OAuth2ClientHttpRequestInterceptor(manager);
        interceptor.setClientRegistrationIdResolver(_ -> properties.getOauth2ClientRegistrationId());

        // O manager guarda o token obtido indexado pelo par (registrationId, principalName).
        // O principal aqui NAO autentica nada - e so a chave de cache do token. O resolver
        // default usaria o Authentication da thread: o JWT do usuario final que chamou o
        // ordering (um token de maquina cacheado POR USUARIO, fragmentando o cache) ou
        // "anonymousUser" em thread sem SecurityContext (jobs, pools assincronos). Com um
        // principal sintetico constante, toda chamada compartilha o MESMO token cacheado.
        interceptor.setPrincipalResolver(_ -> generatePrincipal(properties.getOauth2ClientRegistrationId()));

        return interceptor;
    }

    // Authentication de fachada: existe so para dar um nome estavel ao "dono" do token no
    // cache do manager. Credentials null, authorities vazias, nunca entra no SecurityContext.
    private Authentication generatePrincipal(String principalName) {
        return new AbstractAuthenticationToken(Collections.emptySet()) {
            @Override
            public @Nullable Object getPrincipal() {
                return principalName;
            }

            @Override
            public @Nullable Object getCredentials() {
                return null;
            }
        };
    }

    // função para definir a resiliencia de timeout
    private ClientHttpRequestFactory generateClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofSeconds(7));
        factory.setConnectTimeout(Duration.ofSeconds(3));
        return factory;
    }
}
