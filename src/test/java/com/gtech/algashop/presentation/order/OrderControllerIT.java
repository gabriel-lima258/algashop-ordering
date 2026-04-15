package com.gtech.algashop.presentation.order;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.gtech.algashop.application.checkout.BuyNowInput;
import com.gtech.algashop.application.checkout.BuyNowInputTestDataBuilder;
import com.gtech.algashop.application.checkout.CheckoutInput;
import com.gtech.algashop.application.checkout.CheckoutInputTestDataBuilder;
import com.gtech.algashop.application.order.query.OrderDetailOutput;
import com.gtech.algashop.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.order.OrderJpaEntityRepository;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartJpaEntityRepository;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntity;
import com.gtech.algashop.presentation.utils.AlgaShopResourceUtils;
import io.restassured.RestAssured;
import io.restassured.path.json.config.JsonPathConfig;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static io.restassured.config.JsonConfig.*;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureStubRunner(stubsMode = StubRunnerProperties.StubsMode.LOCAL, ids = "com.algaworks.algashop:product-catalog:0.0.1-SNAPSHOT:8781")
// crie dados antes do teste
@Sql(scripts = "classpath:db/testdata/afterMigrate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
// limpa o banco apos o teste
@Sql(scripts = "classpath:db/clean/afterMigrate.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
class OrderControllerIT {

    @LocalServerPort
    private int port;

    private static final UUID validCustomerId = UUID.fromString("6e148bd5-47f6-4022-b9da-07cfaa294f7a");
    private static final UUID validProductId = UUID.fromString("fffe6ec2-7103-48b3-8e4f-3b58e43fb75a");

    @Autowired
    private CustomerJpaEntityRepository customerJpaEntityRepository;

    @Autowired
    private OrderJpaEntityRepository orderJpaEntityRepository;

    @Autowired
    private ShoppingCartJpaEntityRepository shoppingCartJpaEntityRepository;

    private WireMockServer wireMockProductCatalog;
    private WireMockServer wireMockRapidex;

    @BeforeEach
    void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = port;

        RestAssured.config().jsonConfig(jsonConfig()
                .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL));

        // setando o wiremock dentro do java

        wireMockRapidex = new WireMockServer(options()
                .port(8780)
                .usingFilesUnderDirectory("src/test/resources/wiremock/rapidex")
                .extensions(new ResponseTemplateTransformer(true)));

        wireMockProductCatalog = new WireMockServer(options()
                .port(8781)
                .usingFilesUnderDirectory("src/test/resources/wiremock/product-catalog")
                .extensions(new ResponseTemplateTransformer(true)));

        wireMockRapidex.start();
        wireMockProductCatalog.start();
    }

    // matando os processos do wiremock
    @AfterEach
    void after() {
        wireMockRapidex.stop();
        wireMockProductCatalog.stop();
    }

    @Test
    void shouldAndOrderUsingProduct() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-with-product.json");
        String orderCreatedId = RestAssured
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-product.v1+json")
                    .body(jsonOrder)
                .when()
                    .post("/api/v1/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", Matchers.not(Matchers.emptyString()),
                            "customer.id", Matchers.is(validCustomerId.toString()))
                    .extract().jsonPath().getString("id");

        boolean orderExists = orderJpaEntityRepository.existsById(new OrderId(orderCreatedId).value().toLong());

        Assertions.assertThat(orderExists).isTrue();
    }

    @Test
    void shouldAndOrderUsingProductUsingDTO() {
        UUID creditCardId = UUID.randomUUID();

        BuyNowInput input = BuyNowInputTestDataBuilder.aBuyNowInput()
                .productId(validProductId)
                .customerId(validCustomerId)
                .creditCardId(creditCardId)
                .build();

        OrderDetailOutput orderCreated = RestAssured
                .given()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType("application/vnd.order-with-product.v1+json")
                .body(input)
                .when()
                .post("/api/v1/orders")
                .then()
                .assertThat()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .statusCode(HttpStatus.CREATED.value())
                .body("id", Matchers.not(Matchers.emptyString()),
                        "customer.id", Matchers.is(validCustomerId.toString()))
                .extract()
                .body().as(OrderDetailOutput.class);

        Assertions.assertThat(orderCreated.getCustomer().getId()).isEqualTo(validCustomerId);
        Assertions.assertThat(orderCreated.getCreditCardId()).isEqualTo(creditCardId);


        boolean orderExists = orderJpaEntityRepository.existsById(new OrderId(orderCreated.getId()).value().toLong());

        Assertions.assertThat(orderExists).isTrue();
    }

    @Test
    void shouldNotCreateOrderWhenCustomerWasNotFound() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-with-invalid-customer.json");
        RestAssured
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-product.v1+json")
                    .body(jsonOrder)
                .when()
                    .post("/api/v1/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());

    }


    @Test
    void shouldNotCreateOrderWhenProductApiIsUnavailable() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-with-product.json");

        wireMockProductCatalog.stop();

        RestAssured
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-product.v1+json")
                    .body(jsonOrder)
                .when()
                    .post("/api/v1/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.GATEWAY_TIMEOUT.value());

    }

    @Test
    void shouldNotCreateOrderWhenProductWhenProductDoesNotExists() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-product-with-invalid-product.json");

        RestAssured
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-product.v1+json")
                    .body(jsonOrder)
                .when()
                    .post("/api/v1/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());

    }

    @Test
    void shouldCreateOrderUsingShoppingCart() {
        CustomerPersistenceEntity customer = customerJpaEntityRepository
                .findById(validCustomerId).orElseThrow();

        ShoppingCartPersistenceEntity cart = ShoppingCartPersistenceEntityTestDataBuilder
                .existingCart()
                .customer(customer)
                .build();
        shoppingCartJpaEntityRepository.saveAndFlush(cart);

        CheckoutInput input = CheckoutInputTestDataBuilder
                .aCheckoutInput(cart.getId())
                .build();

        OrderDetailOutput orderCreated = RestAssured
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-shopping-cart.v1+json")
                    .body(input)
                .when()
                    .post("/api/v1/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", Matchers.not(Matchers.emptyString()),
                            "customer.id", Matchers.is(validCustomerId.toString()),
                            "status", Matchers.is("PLACED"),
                            "paymentMethod", Matchers.is("CREDIT_CARD"),
                            "totalItems", Matchers.greaterThan(0))
                    .extract()
                    .body().as(OrderDetailOutput.class);

        Assertions.assertThat(orderCreated.getCustomer().getId()).isEqualTo(validCustomerId);

        boolean orderExists = orderJpaEntityRepository
                .existsById(new OrderId(orderCreated.getId()).value().toLong());
        Assertions.assertThat(orderExists).isTrue();
    }

    @Test
    void shouldNotCreateOrderWithShoppingCartWhenCartDoesNotExist() {
        UUID nonExistentCartId = UUID.randomUUID();

        CheckoutInput input = CheckoutInputTestDataBuilder
                .aCheckoutInput(nonExistentCartId)
                .build();

        RestAssured
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-shopping-cart.v1+json")
                    .body(input)
                .when()
                    .post("/api/v1/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }
}