package com.gtech.algashop.infrastructure.adapters.in.web.order;

import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.out.persistence.order.OrderJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.out.persistence.shoppingcart.ShoppingCartJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.in.web.AbstractPresentationIT;
import com.gtech.algashop.utils.AlgaShopResourceUtils;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

class OrderControllerWithoutProductIT extends AbstractPresentationIT {

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
    void shouldNotCreateOrderWhenProductApiIsUnavailable() {
        String jsonOrder = AlgaShopResourceUtils.readContent("json/create-order-with-product.json");

        // derruba product catalog
        wireMockProductCatalog.stop();

        givenAuthenticated()
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

}