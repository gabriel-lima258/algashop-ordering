package com.gtech.algashop.infrastructure.adapters.in.web.order;

import com.gtech.algashop.core.ports.in.checkout.BuyNowInput;
import com.gtech.algashop.core.application.checkout.BuyNowInputTestDataBuilder;
import com.gtech.algashop.core.ports.in.checkout.CheckoutInput;
import com.gtech.algashop.core.application.checkout.CheckoutInputTestDataBuilder;
import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.domain.model.order.OrderId;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerPersistenceEntity;
import com.gtech.algashop.infrastructure.adapters.out.persistence.order.OrderJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.out.persistence.shoppingcart.ShoppingCartJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.out.persistence.shoppingcart.ShoppingCartPersistenceEntity;
import com.gtech.algashop.infrastructure.adapters.in.web.AbstractPresentationIT;
import com.gtech.algashop.utils.AlgaShopResourceUtils;
import io.restassured.RestAssured;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.UUID;

class OrderControllerIT extends AbstractPresentationIT {

    private static final UUID validCustomerId = UUID.fromString("6e148bd5-47f6-4022-b9da-07cfaa294f7a");
    private static final UUID validProductId = UUID.fromString("fffe6ec2-7103-48b3-8e4f-3b58e43fb75a");

    @Autowired
    private CustomerJpaEntityRepository customerJpaEntityRepository;

    @Autowired
    private OrderJpaEntityRepository orderJpaEntityRepository;

    @Autowired
    private ShoppingCartJpaEntityRepository shoppingCartJpaEntityRepository;

    @BeforeEach
    void setup() {
        super.beforeEach();
    }

    @AfterEach
    void after() {
        AbstractPresentationIT.stopMock();
    }

    @Test
    void shouldAndOrderUsingProduct() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-with-product.json");
        String orderCreatedId = givenAuthenticated()
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

        OrderDetailOutput orderCreated = givenAuthenticated()
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

    /**
     * Fase 27: a resposta mudou de 422 para 403, e a mudanca esta CERTA.
     *
     * Antes, pedir um pedido para um customerId inexistente chegava ao dominio e voltava
     * "cliente nao encontrado". Agora a verificacao de dono roda primeiro: o customerId do
     * corpo nao e o do token, entao a requisicao para em AccessDenied antes de o banco ser
     * consultado.
     *
     * O efeito colateral e uma propriedade desejavel: a API deixou de confirmar QUAIS ids de
     * cliente existem para quem nao tem direito a eles. Um 422 "nao encontrado" contra um 200
     * seria um oraculo de enumeracao - a mesma razao pela qual o login responde igual para
     * senha errada e usuario inexistente (Fase 24).
     */
    @Test
    void shouldReturnForbiddenWhenOrderingForAnotherCustomer() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-with-invalid-customer.json");

        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-product.v1+json")
                    .body(jsonOrder)
                .when()
                    .post("/api/v1/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.FORBIDDEN.value());

    }

    @Test
    void shouldNotCreateOrderWhenProductWhenProductDoesNotExists() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-product-with-invalid-product.json");

        givenAuthenticated()
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

        OrderDetailOutput orderCreated = givenAuthenticated()
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

        givenAuthenticated()
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