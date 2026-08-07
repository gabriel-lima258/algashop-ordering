package com.gtech.algashop.infrastructure.adapters.in.web;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.gtech.algashop.infrastructure.adapters.in.web.utils.TestContainerPostgresSQLConfig;
import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreaker;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfig;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.config.JsonConfig.jsonConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// crie dados antes do teste
@Sql(scripts = "classpath:db/testdata/afterMigrate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
// limpa o banco apos o teste
@Sql(scripts = "classpath:db/clean/afterMigrate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@Import(TestContainerPostgresSQLConfig.class) // importando o gerenciamento de bean do postgres
public class AbstractPresentationIT {

//    @Container
//    @ServiceConnection // uma forma alternativa de criar dynamicSource de configs
//    protected static PostgreSQLContainer postgresSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine")
//            .withDatabaseName("ordering_test");

    @LocalServerPort
    protected int port;

    // O estado do circuito e um singleton do contexto, e o contexto e COMPARTILHADO por
    // todas as classes que estendem esta - inclusive a OrderControllerWithoutProductIT, que
    // derruba o WireMock de proposito para testar o 504. Aquela falha abre o circuito, e sem
    // este reset a proxima classe recebe 504 sem nem chamar o catalogo (dentro do openTimeout).
    @Autowired
    private CircuitBreakerFactory<FrameworkRetryConfig, FrameworkRetryConfigBuilder> circuitBreakerFactory;

    protected static WireMockServer wireMockProductCatalog;
    protected static WireMockServer wireMockRapidex;

    protected void beforeEach() {
        resetProductCatalogCircuitBreaker();

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = port;

        RestAssured.config().jsonConfig(jsonConfig()
                .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));

        wireMockRapidex = new WireMockServer(options()
                .port(8780)
                .usingFilesUnderDirectory("src/test/resources/wiremock/rapidex")
                .globalTemplating(true));

        wireMockProductCatalog = new WireMockServer(options()
                .port(8781)
                .usingFilesUnderDirectory("src/test/resources/wiremock/product-catalog")
                .globalTemplating(true));

        wireMockRapidex.start();
        wireMockProductCatalog.start();
    }

    protected static void stopMock() {
        wireMockRapidex.stop();
        wireMockProductCatalog.stop();
    }

    private void resetProductCatalogCircuitBreaker() {
        FrameworkRetryCircuitBreaker circuitBreaker =
                (FrameworkRetryCircuitBreaker) circuitBreakerFactory.create("productCatalogCB");

        circuitBreaker.getCircuitBreakerPolicy().reset();
    }

}
