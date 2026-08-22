package com.gtech.algashop.infrastructure.adapters.in.web.shoppingcart;

import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartItemInput;
import com.gtech.algashop.infrastructure.adapters.in.web.AbstractPresentationIT;
import com.gtech.algashop.infrastructure.adapters.out.persistence.shoppingcart.ShoppingCartJpaEntityRepository;
import com.gtech.algashop.utils.MockJwtFactory;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

// O recurso /me nao recebe id nenhum: o carrinho e sempre o do sub do token. Por isso a
// classe inteira gira em torno do token padrao (sub 6e148bd5..., papel CUSTOMER), cujo
// carrinho vem do seed. Os testes sao ORDENADOS porque compartilham o estado do banco
// (o @Sql roda uma vez por classe): leitura e 403 primeiro, mutacoes depois, e a
// exclusao/recriacao do carrinho por ultimo.
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MyShoppingCartControllerIT extends AbstractPresentationIT {

    private static final String MY_SHOPPING_CART_PATH = "/api/v1/customers/me/shopping-cart";

    // do seed (afterMigrate.sql): carrinho do sub do token padrao, e o de outro cliente
    private static final UUID authenticatedCustomerId = UUID.fromString(MockJwtFactory.DEFAULT_SUBJECT);
    private static final UUID myShoppingCartId = UUID.fromString("4f31582a-66e6-4601-a9d3-ff608c2d4461");
    private static final UUID otherCustomerShoppingCartId = UUID.fromString("1b2c3d4e-5a6b-7c8d-9e0f-1a2b3c4d5e6f");
    private static final UUID validProductId = UUID.fromString("fffe6ec2-7103-48b3-8e4f-3b58e43fb75a");

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

    // -------------------------------------------------------------------------
    // Acesso indevido: sem token, sem escopo, e com escopo mas sem papel CUSTOMER
    // -------------------------------------------------------------------------

    @Test
    @Order(1)
    void shouldRejectRequestWithoutToken() {
        RestAssured.given()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    @Order(2)
    void shouldRejectCustomerWithoutScope() {
        givenAuthenticatedWithNoScopeToken()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.FORBIDDEN.value());
    }

    // MANAGER carrega todos os escopos, mas o recurso /me exige o papel CUSTOMER
    @Test
    @Order(3)
    void shouldRejectManagerToken() {
        givenAuthenticatedAsManager()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.FORBIDDEN.value());
    }

    // token de maquina (client_credentials) nao tem papel nenhum
    @Test
    @Order(4)
    void shouldRejectMachineToken() {
        givenAuthenticatedAsMachine()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.FORBIDDEN.value());
    }

    // -------------------------------------------------------------------------
    // Leituras
    // -------------------------------------------------------------------------

    // o carrinho devolvido e o do sub do token - nunca o de outro cliente
    @Test
    @Order(5)
    void shouldGetMyShoppingCart() {
        givenAuthenticated()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(APPLICATION_JSON_VALUE)
                    .body("id", Matchers.is(myShoppingCartId.toString()),
                            "customerId", Matchers.is(authenticatedCustomerId.toString()),
                            "id", Matchers.not(otherCustomerShoppingCartId.toString()));
    }

    @Test
    @Order(6)
    void shouldListMyShoppingCartItems() {
        givenAuthenticated()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH + "/items")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(APPLICATION_JSON_VALUE)
                    .body("items.size()", Matchers.is(1),
                            "items[0].productId", Matchers.is(validProductId.toString()),
                            "items[0].quantity", Matchers.is(2));
    }

    // -------------------------------------------------------------------------
    // Mutacoes de itens
    // -------------------------------------------------------------------------

    @Test
    @Order(7)
    void shouldAddItemToMyShoppingCart() {
        ShoppingCartItemInput itemInput = ShoppingCartItemInput.builder()
                .productId(validProductId)
                .quantity(1)
                .build();

        givenAuthenticated()
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(itemInput)
                .when()
                    .post(MY_SHOPPING_CART_PATH + "/items")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NO_CONTENT.value());

        givenAuthenticated()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("totalItems", Matchers.is(3));
    }

    @Test
    @Order(8)
    void shouldNotAddItemWithInvalidData() {
        givenAuthenticated()
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body("{}")
                .when()
                    .post(MY_SHOPPING_CART_PATH + "/items")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .contentType(APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    @Order(9)
    void shouldRemoveItemFromMyShoppingCart() {
        String itemId =
                givenAuthenticated()
                            .accept(APPLICATION_JSON_VALUE)
                        .when()
                            .get(MY_SHOPPING_CART_PATH + "/items")
                        .then()
                            .statusCode(HttpStatus.OK.value())
                            .extract().jsonPath().getString("items[0].id");

        givenAuthenticated()
                .when()
                    .delete(MY_SHOPPING_CART_PATH + "/items/{itemId}", itemId)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NO_CONTENT.value());

        givenAuthenticated()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH + "/items")
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("items.size()", Matchers.is(0));
    }

    @Test
    @Order(10)
    void shouldEmptyMyShoppingCart() {
        ShoppingCartItemInput itemInput = ShoppingCartItemInput.builder()
                .productId(validProductId)
                .quantity(2)
                .build();

        givenAuthenticated()
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(itemInput)
                .when()
                    .post(MY_SHOPPING_CART_PATH + "/items")
                .then()
                    .statusCode(HttpStatus.NO_CONTENT.value());

        givenAuthenticated()
                .when()
                    .delete(MY_SHOPPING_CART_PATH + "/items")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NO_CONTENT.value());

        givenAuthenticated()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH)
                .then()
                    .statusCode(HttpStatus.OK.value())
                    .body("totalItems", Matchers.is(0),
                            "items.size()", Matchers.is(0));
    }

    // -------------------------------------------------------------------------
    // Ciclo de vida do carrinho: excluir e recriar
    // -------------------------------------------------------------------------

    @Test
    @Order(11)
    void shouldDeleteMyShoppingCart() {
        givenAuthenticated()
                .when()
                    .delete(MY_SHOPPING_CART_PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NO_CONTENT.value());

        assertThat(shoppingCartJpaEntityRepository.existsById(myShoppingCartId)).isFalse();

        givenAuthenticated()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get(MY_SHOPPING_CART_PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .contentType(APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    @Order(12)
    void shouldCreateMyShoppingCart() {
        String createdShoppingCartId =
                givenAuthenticated()
                            .accept(APPLICATION_JSON_VALUE)
                        .when()
                            .post(MY_SHOPPING_CART_PATH)
                        .then()
                            .assertThat()
                            .statusCode(HttpStatus.CREATED.value())
                            .contentType(APPLICATION_JSON_VALUE)
                            .header("Location", Matchers.containsString(MY_SHOPPING_CART_PATH))
                            .body("id", Matchers.not(Matchers.emptyString()),
                                    "customerId", Matchers.is(authenticatedCustomerId.toString()))
                            .extract().jsonPath().getString("id");

        assertThat(shoppingCartJpaEntityRepository
                .existsById(UUID.fromString(createdShoppingCartId))).isTrue();
    }

    // CUSTOMER autenticado no authorization server mas sem cadastro de customer aqui:
    // nao ha para quem criar carrinho
    @Test
    @Order(13)
    void shouldNotCreateShoppingCartForUnregisteredCustomer() {
        givenAuthenticatedAsNewCustomer()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .post(MY_SHOPPING_CART_PATH)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                    .contentType(APPLICATION_PROBLEM_JSON_VALUE);
    }

}
