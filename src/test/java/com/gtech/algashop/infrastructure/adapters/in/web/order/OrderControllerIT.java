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

    // o sub do token padrao E o cliente do seed: nas rotas /me o pedido sai sempre no nome dele
    private static final UUID validCustomerId = UUID.fromString("6e148bd5-47f6-4022-b9da-07cfaa294f7a");
    private static final UUID validProductId = UUID.fromString("fffe6ec2-7103-48b3-8e4f-3b58e43fb75a");

    // segundo cliente do seed, com pedidos proprios - o "outro" dos testes de isolamento
    private static final UUID anotherCustomerId = UUID.fromString("f6a7b8c9-d0e1-f2a3-b4c5-d6e7f8a9b0c1");
    private static final long anotherCustomerOrderId = 1727196000004L;
    private static final long myOrderId = 1727196000001L;

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
                    .post("/api/v1/customers/me/orders")
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
                .creditCardId(creditCardId)
                .build();

        OrderDetailOutput orderCreated = givenAuthenticated()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType("application/vnd.order-with-product.v1+json")
                .body(input)
                .when()
                .post("/api/v1/customers/me/orders")
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
     * Substitui o antigo shouldReturnForbiddenWhenOrderingForAnotherCustomer.
     *
     * Na Fase 27 o 403 vinha de comparar o customerId do CORPO com o do token. A rota /me
     * eliminou a comparacao eliminando a escolha: o campo virou @JsonIgnore e o controller
     * sobrescreve com o sub do token. Pedir para outro cliente deixou de ser uma requisicao
     * proibida e passou a ser uma requisicao INEXPRIMIVEL - o fixture ainda envia o
     * customerId alheio justamente para provar que ele e ignorado.
     */
    @Test
    void shouldIgnoreBodyCustomerIdAndOrderForAuthenticatedCustomer() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-with-invalid-customer.json");

        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-product.v1+json")
                    .body(jsonOrder)
                .when()
                    .post("/api/v1/customers/me/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .statusCode(HttpStatus.CREATED.value())
                    .body("customer.id", Matchers.is(validCustomerId.toString()));

    }

    @Test
    void shouldNotCreateOrderWhenProductWhenProductDoesNotExists() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-product-with-invalid-product.json");

        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-product.v1+json")
                    .body(jsonOrder)
                .when()
                    .post("/api/v1/customers/me/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());

    }

    @Test
    void shouldCreateOrderUsingShoppingCart() {
        CustomerPersistenceEntity customer = customerJpaEntityRepository
                .findById(validCustomerId).orElseThrow();

        // o carrinho e resolvido pelo cliente autenticado - deve haver exatamente UM;
        // limpar antes torna o teste imune a ordem de execucao (o seed tambem tem carrinho)
        shoppingCartJpaEntityRepository.deleteAll();

        ShoppingCartPersistenceEntity cart = ShoppingCartPersistenceEntityTestDataBuilder
                .existingCart()
                .customer(customer)
                .build();
        shoppingCartJpaEntityRepository.saveAndFlush(cart);

        CheckoutInput input = CheckoutInputTestDataBuilder
                .aCheckoutInput()
                .build();

        OrderDetailOutput orderCreated = givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-shopping-cart.v1+json")
                    .body(input)
                .when()
                    .post("/api/v1/customers/me/orders")
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
    void shouldNotCreateOrderWithShoppingCartWhenCustomerHasNoCart() {
        // "carrinho inexistente" agora significa: o cliente autenticado nao tem carrinho
        shoppingCartJpaEntityRepository.deleteAll();

        CheckoutInput input = CheckoutInputTestDataBuilder
                .aCheckoutInput()
                .build();

        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType("application/vnd.order-with-shopping-cart.v1+json")
                    .body(input)
                .when()
                    .post("/api/v1/customers/me/orders")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }

    // =========================================================================
    // Leitura /me - isolamento por dono
    // =========================================================================

    /**
     * Trava o fechamento do IDOR: o customerId da query string e IGNORADO e a listagem
     * sai sempre escopada pelo sub do token. Se o MyOrderController deixar de sobrescrever
     * o filtro, este teste devolve os pedidos do outro cliente e fica vermelho.
     */
    @Test
    void shouldScopeMyOrdersListToAuthenticatedCustomer() {
        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .queryParam("customerId", anotherCustomerId.toString())
                .when()
                    .get("/api/v1/customers/me/orders")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("totalElements", Matchers.greaterThan(0))
                    .body("content.customer.id",
                            Matchers.everyItem(Matchers.is(validCustomerId.toString())));
    }

    @Test
    void shouldFindMyOrderById() {
        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/customers/me/orders/{orderId}", new OrderId(myOrderId).toString())
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("customer.id", Matchers.is(validCustomerId.toString()));
    }

    @Test
    void shouldReturnNotFoundWhenFetchingAnotherCustomersOrder() {
        // pedido existe, mas e de outro dono: 404 e nao 403, para nao virar oraculo de ids
        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/customers/me/orders/{orderId}", new OrderId(anotherCustomerOrderId).toString())
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.NOT_FOUND.value());
    }

    // =========================================================================
    // Rotas administrativas - exigem NAO ser CUSTOMER
    // =========================================================================

    @Test
    void shouldListAllOrdersAsManager() {
        givenAuthenticatedAsManager()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/orders")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("totalElements", Matchers.greaterThan(0));
    }

    @Test
    void shouldForbidCustomerOnAdminOrders() {
        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/orders")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.FORBIDDEN.value());
    }
}
