package com.gtech.algashop.infrastructure.adapters.out.web.product.client.http;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.gtech.algashop.core.domain.model.AbstractDomainIT;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.BadGatewayException;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.GatewayTimeoutException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreaker;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfig;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.moreThanOrExactly;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Cobre o que o ResilientProductCatalogAPIClient promete: cada resposta do catalogo tem um
// resultado definido, o retry acontece so onde deve, e o circuito para de bater na API
// depois da primeira falha.
//
// O CONTRATO QUE ESTES TESTES FIXAM:
//   200 com produto      -> Optional com o produto
//   404 / 4xx / sem corpo-> Optional.empty() (o chamador transforma em 422) e NAO abre o circuito
//   5xx                  -> BadGatewayException.ServerErrorException, com retry (4 chamadas)
//   conexao perdida      -> GatewayTimeoutException, com retry
// So o que ESCAPA como excecao conta como falha para o circuito - por isso os testes de
// circuito usam 5xx, e nao 4xx.
//
// POR QUE OS TEMPOS SAO SOBRESCRITOS: com o backoff de producao (3s -> 6s -> 12s) cada
// caso de retry levaria 21s. Os defaults do SpringCircuitBreakerConfig continuam sendo os
// de producao; aqui so encurtamos via property.
//
// OBS: o cache NAO participa destes testes. O @EnableCaching mora no RedisCacheConfig, que
// e @ConditionalOnProperty(spring.cache.type=redis), e essa property nao existe no perfil
// de teste - entao o @Cacheable fica inerte e cada chamada vai mesmo para o WireMock.
@TestPropertySource(properties = {
        "algashop.resilience.circuit-breaker.max-retries=3",
        "algashop.resilience.circuit-breaker.delay=20ms",
        "algashop.resilience.circuit-breaker.multiplier=1",
        "algashop.resilience.circuit-breaker.open-timeout=200ms",
        "algashop.resilience.circuit-breaker.reset-timeout=5s"
})
class ResilientProductCatalogAPIClientIT extends AbstractDomainIT {

    // ids que existem nos mappings de src/test/resources/wiremock/product-catalog
    private static final UUID EXISTING_PRODUCT = UUID.fromString("fffe6ec2-7103-48b3-8e4f-3b58e43fb75a");
    private static final UUID NOT_FOUND_PRODUCT = UUID.fromString("21651a12-b126-4213-ac21-19f66ff4642e");
    private static final UUID UNAUTHORIZED_PRODUCT = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SERVER_ERROR_PRODUCT = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID NO_CONTENT_PRODUCT = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID CONNECTION_LOST_PRODUCT = UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final long OPEN_TIMEOUT_MILLIS = 200;

    @Autowired
    private ResilientProductCatalogAPIClient client;

    @Autowired
    private CircuitBreakerFactory<FrameworkRetryConfig, FrameworkRetryConfigBuilder> circuitBreakerFactory;

    // PORTA DINAMICA, nao a 8781 fixa: os ITs de apresentacao (AbstractPresentationIT) sobem
    // e derrubam um WireMock naquela porta o tempo todo. Disputar a mesma porta faz o server
    // de um deles nao subir, e o sintoma aparece longe daqui - o outro teste recebe 504 de
    // conexao recusada. O start fica em bloco estatico porque o @DynamicPropertySource e
    // avaliado durante o refresh do contexto, antes de qualquer @BeforeAll.
    private static final WireMockServer wireMockProductCatalog;

    static {
        wireMockProductCatalog = new WireMockServer(options()
                .dynamicPort()
                .usingFilesUnderDirectory("src/test/resources/wiremock/product-catalog")
                .globalTemplating(true));

        wireMockProductCatalog.start();
    }

    @DynamicPropertySource
    static void productCatalogUrl(DynamicPropertyRegistry registry) {
        registry.add("algashop.integrations.product-catalog.url",
                () -> "http://localhost:" + wireMockProductCatalog.port());
    }

    @BeforeEach
    void setup() {
        // o journal do WireMock e por servidor, nao por teste: sem isso os verify() de
        // contagem somariam as chamadas dos testes anteriores
        wireMockProductCatalog.resetRequests();

        // o circuito e um singleton compartilhado por todo o contexto: sem este reset, um
        // teste que abre o circuito faria o proximo falhar sem nem chamar o WireMock
        circuitBreakerPolicyReset();
    }

    @AfterAll
    static void clean() {
        wireMockProductCatalog.stop();
    }

    @Test
    void shouldReturnProductWhenCatalogAnswers() {
        Optional<ProductResponse> product = client.getById(EXISTING_PRODUCT);

        assertThat(product).isPresent();
        assertThat(product.get().getName()).isEqualTo("Notebook X11");
        assertThat(product.get().getInStock()).isTrue();
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        Optional<ProductResponse> product = client.getById(NOT_FOUND_PRODUCT);

        assertThat(product).isEmpty();
        verifyCallCount(NOT_FOUND_PRODUCT, 1);
    }

    @Test
    void shouldReturnEmptyWhenCatalogAnswersClientError() {
        // TODO 4xx vira vazio, entao um 401 chega ao consumidor como "produto nao
        // encontrado" (422) - um erro de configuracao disfarcado de erro de negocio. Em
        // troca, o pedido nao cai por causa disso e o circuito NAO abre (4xx e culpa da
        // requisicao, nao do catalogo). Se o mapeamento mudar, este teste muda junto.
        Optional<ProductResponse> product = client.getById(UNAUTHORIZED_PRODUCT);

        assertThat(product).isEmpty();
        verifyCallCount(UNAUTHORIZED_PRODUCT, 1); // sem retry: repetir daria o mesmo 401
    }

    @Test
    void shouldRetryAndFailWithBadGatewayWhenCatalogAnswersServerError() {
        assertThatThrownBy(() -> client.getById(SERVER_ERROR_PRODUCT))
                .isInstanceOf(BadGatewayException.ServerErrorException.class);

        // 1 chamada original + 3 retentativas: 5xx esta no includes da RetryPolicy
        verifyCallCount(SERVER_ERROR_PRODUCT, 4);
    }

    @Test
    void shouldFailWithGatewayTimeoutWhenConnectionIsLost() {
        assertThatThrownBy(() -> client.getById(CONNECTION_LOST_PRODUCT))
                .isInstanceOf(GatewayTimeoutException.class);

        // "pelo menos 4" e nao "exatamente 4" de proposito: quando a conexao cai, o
        // HttpURLConnection do JDK (por baixo do SimpleClientHttpRequestFactory) reenvia o
        // GET por conta propria, ANTES de devolver o erro para nos. Na pratica chegam ~7
        // requests para as nossas 4 tentativas - detalhe que so aparece em falha de rede,
        // nao em resposta HTTP de erro (veja o teste de 5xx, que bate exatamente 4).
        verifyAtLeastCallCount(CONNECTION_LOST_PRODUCT, 4);
    }

    @Test
    void shouldReturnEmptyWhenCatalogAnswersWithoutBody() {
        // 204/200 sem corpo: o RestClient devolve null e o Optional.ofNullable transforma em
        // vazio. Nao estoura NPE (que ninguem traduziria e ainda abriria o circuito), mas
        // tambem nao distingue de "produto nao existe" - o consumidor recebe 422.
        Optional<ProductResponse> product = client.getById(NO_CONTENT_PRODUCT);

        assertThat(product).isEmpty();
        verifyCallCount(NO_CONTENT_PRODUCT, 1);
    }

    @Test
    void shouldOpenCircuitAndStopCallingCatalogAfterFailure() {
        // usa 5xx porque so o que ESCAPA do supplier conta como falha para o circuito -
        // 4xx e corpo vazio viram Optional e nao mexem no estado
        assertThatThrownBy(() -> client.getById(SERVER_ERROR_PRODUCT))
                .isInstanceOf(BadGatewayException.ServerErrorException.class);

        verifyCallCount(SERVER_ERROR_PRODUCT, 4);

        // circuito aberto: a segunda chamada falha com a MESMA excecao, mas instantanea
        assertThatThrownBy(() -> client.getById(SERVER_ERROR_PRODUCT))
                .isInstanceOf(BadGatewayException.ServerErrorException.class);

        // a prova de que o circuito cortou: o contador nao subiu de 4
        verifyCallCount(SERVER_ERROR_PRODUCT, 4);
    }

    @Test
    void shouldCloseCircuitWhenCatalogRecoversAfterOpenTimeout() throws InterruptedException {
        assertThatThrownBy(() -> client.getById(SERVER_ERROR_PRODUCT))
                .isInstanceOf(BadGatewayException.ServerErrorException.class);

        // passado o openTimeout, o circuito vira HALF_OPEN e deixa UMA chamada de teste passar
        Thread.sleep(OPEN_TIMEOUT_MILLIS + 100);

        Optional<ProductResponse> product = client.getById(EXISTING_PRODUCT);

        assertThat(product).isPresent();
        verifyCallCount(EXISTING_PRODUCT, 1);
    }

    private void verifyCallCount(UUID productId, int expectedCount) {
        wireMockProductCatalog.verify(expectedCount,
                getRequestedFor(urlEqualTo("/api/v1/products/" + productId)));
    }

    private void verifyAtLeastCallCount(UUID productId, int expectedCount) {
        wireMockProductCatalog.verify(moreThanOrExactly(expectedCount),
                getRequestedFor(urlEqualTo("/api/v1/products/" + productId)));
    }

    private void circuitBreakerPolicyReset() {
        FrameworkRetryCircuitBreaker circuitBreaker =
                (FrameworkRetryCircuitBreaker) circuitBreakerFactory.create("productCatalogCB");

        circuitBreaker.getCircuitBreakerPolicy().reset();
    }
}
