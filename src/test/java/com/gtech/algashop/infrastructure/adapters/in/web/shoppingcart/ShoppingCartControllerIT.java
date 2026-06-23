package com.gtech.algashop.infrastructure.adapters.in.web.shoppingcart;

import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartItemInput;
import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.out.persistence.shoppingcart.ShoppingCartJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.in.web.AbstractPresentationIT;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

class ShoppingCartControllerIT extends AbstractPresentationIT {

    @Autowired
    private CustomerJpaEntityRepository customerJpaEntityRepository;

    @Autowired
    private ShoppingCartJpaEntityRepository shoppingCartJpaEntityRepository;

    private static final UUID customerIdForCreateCart = UUID.fromString("3a4b5c6d-7e8f-9a0b-1c2d-3e4f5a6b7c8d");
    private static final UUID customerIdForAddItem = UUID.fromString("5f6b7d8e-9c0a-1b2d-3c4a-5f6b7d8e9c0a");
    private static final UUID validProductId = UUID.fromString("fffe6ec2-7103-48b3-8e4f-3b58e43fb75a");

    @BeforeEach
    void setup() {
        super.beforeEach();
    }

    @AfterEach
    void after() {
        AbstractPresentationIT.stopMock();
    }

    @Test
    void shouldCreateAShoppingCart() {
        ShoppingCartInput input = ShoppingCartInput.builder()
                .customerId(customerIdForCreateCart)
                .build();

        String shoppingCartCreatedId = RestAssured
                .given()
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .post("/api/v1/shopping-carts")
                .then()
                    .assertThat()
                    .contentType(APPLICATION_JSON_VALUE)
                    .statusCode(HttpStatus.CREATED.value())
                    .body("id", Matchers.not(Matchers.emptyString()),
                            "customerId", Matchers.is(customerIdForCreateCart.toString()))
                    .extract().jsonPath().getString("id");

        boolean shoppingCartExists = shoppingCartJpaEntityRepository
                .existsById(UUID.fromString(shoppingCartCreatedId));

        assertThat(shoppingCartExists).isTrue();
    }

    @Test
    void shouldNotCreateShoppingCartWithInvalidData() {
        ShoppingCartInput input = ShoppingCartInput.builder().build();

        RestAssured
                .given()
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .post("/api/v1/shopping-carts")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .contentType(APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    void shouldAddItemToShoppingCart() {
        ShoppingCartInput cartInput = ShoppingCartInput.builder()
                .customerId(customerIdForAddItem)
                .build();

        String shoppingCartId = RestAssured
                .given()
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(cartInput)
                .when()
                    .post("/api/v1/shopping-carts")
                .then()
                    .statusCode(HttpStatus.CREATED.value())
                    .extract().jsonPath().getString("id");

        ShoppingCartItemInput itemInput = ShoppingCartItemInput.builder()
                .productId(validProductId)
                .quantity(2)
                .build();

        RestAssured
                .given()
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(itemInput)
                .when()
                    .post("/api/v1/shopping-carts/{shoppingCartId}/items", shoppingCartId)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NO_CONTENT.value());

        RestAssured
                .given()
                    .accept(APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/shopping-carts/{shoppingCartId}", shoppingCartId)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("totalItems", Matchers.is(2),
                            "items.size()", Matchers.is(1),
                            "items[0].productId", Matchers.is(validProductId.toString()),
                            "items[0].quantity", Matchers.is(2));
    }

    @Test
    void shouldNotAddItemToNonExistentShoppingCart() {
        UUID nonExistentCartId = UUID.randomUUID();

        ShoppingCartItemInput itemInput = ShoppingCartItemInput.builder()
                .productId(validProductId)
                .quantity(1)
                .build();

        RestAssured
                .given()
                    .accept(APPLICATION_JSON_VALUE)
                    .contentType(APPLICATION_JSON_VALUE)
                    .body(itemInput)
                .when()
                    .post("/api/v1/shopping-carts/{shoppingCartId}/items", nonExistentCartId)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .contentType(APPLICATION_PROBLEM_JSON_VALUE);
    }

}
