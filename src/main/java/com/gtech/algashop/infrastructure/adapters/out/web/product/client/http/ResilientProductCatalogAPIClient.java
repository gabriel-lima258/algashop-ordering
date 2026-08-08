package com.gtech.algashop.infrastructure.adapters.out.web.product.client.http;

import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.BadGatewayException;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.GatewayTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreaker;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfig;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.core.retry.RetryException;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.UUID;

import static com.gtech.algashop.infrastructure.config.resilience.SpringCircuitBreakerConfig.productCatalogCBId;

// Unica camada entre o dominio e o HTTP do product-catalog: cache + resiliencia + traducao
// de erro. Devolve DTO de infra; quem traduz para o agregado e o ProductCatalogServiceHttpImpl.
//
// Cadeia real de proxies (a ordem das anotacoes NAO define isso - o bulkhead usa
// setBeforeExistingAdvisors(true) e entra por fora do cache):
//     ConcurrencyLimit -> Cacheable -> getById -> circuitBreaker -> retry -> loadProduct
// Consequencia: cache hit nao toca o circuito, mas ainda ocupa uma das 10 vagas.
//
// O QUE CADA FALHA VIRA:
//   404                  -> Optional.empty() (o chamador transforma em 422)
//   400 / 401 / 403      -> 502, sem retry
//   5xx                  -> 502, com retry
//   timeout / rede fora  -> 504, com retry
//   200 sem corpo        -> 502 (resposta invalida, nao "produto inexistente")
//   circuito aberto      -> a mesma excecao acima, instantanea
//
// CUSTO NO PIOR CASO: 4 tentativas x 7s de read timeout + 21s de backoff = ~49s segurando
// uma vaga do bulkhead. E o motivo de o circuito abrir rapido importar.
@Component
@Slf4j
public class ResilientProductCatalogAPIClient {

    private final ProductCatalogApiClient productCatalogApiClient;
    private final FrameworkRetryCircuitBreaker circuitBreaker;

    // create() no construtor, nao por chamada: o estado do circuito vive na config cacheada
    // por id dentro da fabrica, entao uma instancia so basta. E seguro aqui porque a
    // auto-config aplica os Customizers dentro do proprio @Bean da fabrica.
    // O cast e o preco de LER o estado (a interface CircuitBreaker so sabe run()) e fica
    // confinado a este construtor e aos logs de estado.
    public ResilientProductCatalogAPIClient(ProductCatalogApiClient productCatalogApiClient,
        CircuitBreakerFactory<FrameworkRetryConfig, FrameworkRetryConfigBuilder> circuitBreakerFactory) {
        this.productCatalogApiClient = productCatalogApiClient;
        this.circuitBreaker = (FrameworkRetryCircuitBreaker) circuitBreakerFactory.create(productCatalogCBId);
    }

    // NAO ha cache negativo: o Spring desembrulha o Optional antes de gravar, vira null, e o
    // cache usa disableCachingNullValues() - o put e recusado e sai um WARN a cada 404.
    // CUIDADO: unless = "#result.isEmpty()" NAO resolve - o mesmo desembrulho vale para o
    // unless, entao #result e o ProductResponse e a expressao estoura. Seria "#result == null".
    @Cacheable(cacheNames = "algashop:product-catalog-api:v1", key = "#productId")
    @ConcurrencyLimit(10) // bulkhead: no maximo 10 threads aqui dentro; as demais BLOQUEIAM
    public Optional<ProductResponse> getById(UUID productId) {
        // "Trying" sai mesmo com o circuito aberto; "Loading" (no loadProduct) so quando vai
        // de fato para a rede. A diferenca entre os dois e como se ve o circuito cortando.
        log.info("Trying to load product, there's no cache {}", productId);
        log.info("Product catalog API CB state is {}", circuitBreaker.getCircuitBreakerPolicy().getState());

        try {
            // Circuito: UMA falha ja leva CLOSED -> OPEN (nao ha threshold). Fica aberto por
            // openTimeout (10s) falhando na hora; depois disso uma chamada passa como teste -
            // sucesso fecha, falha reabre. resetTimeout (25s) sem falha nenhuma zera o estado.
            return circuitBreaker.run(() -> loadProduct(productId));
        } catch (NoFallbackAvailableException e) {
            throw unwrapException(e);
        }
    }

    private Optional<ProductResponse> loadProduct(UUID productId) {
        // uma linha por ida real ao catalogo - inclusive por retentativa
        log.info("Loading product {}", productId);

        try {
            return Optional.ofNullable(productCatalogApiClient.getById(productId));
        } catch (HttpClientErrorException e) {
            if (!(e instanceof HttpClientErrorException.NotFound)) {
                log.error("Client HTTP error when loading product {}", productId, e);
            }
            return Optional.empty();
        } catch (RestClientException e) {
            // Traduzir AQUI DENTRO e o que faz a RetryPolicy enxergar os tipos do includes;
            // traduzir so no getById deixaria o retry vendo a excecao crua do RestClient.
            throw translateException(e);
        }
    }

    // A falha chega em camadas: NoFallbackAvailableException -> RetryException -> excecao
    // traduzida (o RetryTemplate embrulha ate o que nem era retentavel). Sem desempacotar, o
    // ApiExceptionHandler cai no handler generico e devolve 500 em vez de 502/504.
    // Vale igual com o circuito aberto: o estado guarda a RetryException da ultima falha.
    // O default devolve a propria NoFallbackAvailableException de proposito - causa que nao
    // reconhecemos e bug nosso, e bug nosso e 500.
    private RuntimeException unwrapException(NoFallbackAvailableException e) {
        Throwable cause = (e.getCause() instanceof RetryException re) ? re.getCause() : e.getCause();

        return switch (cause) {
            case GatewayTimeoutException gte -> gte;
            case BadGatewayException bge -> bge; // pega tambem Server/ClientErrorException
            case null, default -> e;
        };
    }

    // traduz o vocabulario do RestClient para o da API (504 / 502)
    private RuntimeException translateException(RestClientException e) {
        // connect/read timeout (3s / 7s) ou rede fora
        if (e.getCause() instanceof SocketTimeoutException || e instanceof ResourceAccessException) {
            return new GatewayTimeoutException("Product Catalog API Timeout", e);
        }

        // 4xx que nao e 404 (esse nunca chega aqui) - sem retry
        if (e instanceof HttpClientErrorException) {
            return new BadGatewayException.ClientErrorException("Product Catalog API Bad Gateway", e);
        }

        // 5xx - o unico tipo daqui que esta no includes da RetryPolicy
        if (e instanceof HttpServerErrorException) {
            return new BadGatewayException.ServerErrorException("Product Catalog API Bad Gateway", e);
        }

        // resto: corpo ilegivel, erro de conversao...
        return new BadGatewayException("Product Catalog API Bad Gateway", e);
    }

}
