package com.gtech.algashop.infrastructure.adapters.in.web.customer;

import com.gtech.algashop.core.application.customer.management.CustomerUpdatedInputTestDataBuilder;
import com.gtech.algashop.core.ports.in.customer.CustomerInput;
import com.gtech.algashop.core.ports.in.customer.CustomerUpdateInput;
import com.gtech.algashop.core.application.customer.management.CustomerInputTestDataBuilder;
import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.in.web.AbstractPresentationIT;
import com.gtech.algashop.utils.MockJwtFactory;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O cadastro e a edicao de customer migraram do controller administrativo para o /me:
 * o id sai do sub do token, nunca do corpo ou do path. O DELETE (arquivar) deixou de
 * existir na camada web - a cobertura vive no CustomerManagementApplicationServiceIT.
 * As rotas administrativas que restaram sao de leitura e exigem NAO ser CUSTOMER.
 */
class CustomerControllerIT extends AbstractPresentationIT {

    // sub do token padrao == cliente do seed, ja cadastrado
    private static final UUID seedCustomerId = UUID.fromString(MockJwtFactory.DEFAULT_SUBJECT);

    @Autowired
    private CustomerJpaEntityRepository customerJpaEntityRepository;

    @BeforeEach
    void setup() {
        super.beforeEach();
    }

    @AfterEach
    void after() {
        AbstractPresentationIT.stopMock();
    }

    @Test
    void shouldCreateCustomer() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();

        // o token de "cliente novo" e o unico cujo sub ainda nao existe na base
        String customerId = givenAuthenticatedAsNewCustomer()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .post("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.CREATED.value())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body("id", Matchers.is(MockJwtFactory.NEW_CUSTOMER_SUBJECT),
                            "firstName", Matchers.is(input.getFirstName()),
                            "lastName", Matchers.is(input.getLastName()),
                            "email", Matchers.is(input.getEmail()))
                    .extract().jsonPath().getString("id");

        boolean exists = customerJpaEntityRepository.existsById(UUID.fromString(customerId));
        assertThat(exists).isTrue();
    }

    @Test
    void shouldNotCreateCustomerWithInvalidData() {
        CustomerInput input = CustomerInput.builder().build();

        givenAuthenticatedAsNewCustomer()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .post("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    void shouldLoadOwnProfile() {
        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body("id", Matchers.is(seedCustomerId.toString()));
    }

    @Test
    void shouldUpdateOwnProfile() {
        CustomerUpdateInput input = CustomerUpdatedInputTestDataBuilder.aUpdatedCustomer().build();

        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .put("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body("id", Matchers.is(seedCustomerId.toString()),
                            "firstName", Matchers.is(input.getFirstName()),
                            "lastName", Matchers.is(input.getLastName()));
    }

    @Test
    void shouldReturnForbiddenWhenCreatingCustomerWithoutWriteScope() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();

        givenAuthenticatedWithNoScopeToken()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .post("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.FORBIDDEN.value());

    }

    @Test
    void shouldReturnForbiddenWhenManagerCreatesCustomerProfile() {
        // escrever o proprio perfil exige o papel CUSTOMER: escopo sozinho nao basta
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();

        givenAuthenticatedAsManager()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .post("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldReturnUnauthorizedWhenExpiredTokenIsGiven() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();

        givenAuthenticatedWithExpiredToken()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(input)
                .when()
                .post("/api/v1/customers/me")
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

    }

    // =========================================================================
    // Rotas administrativas - leitura, exigem NAO ser CUSTOMER
    // =========================================================================

    @Test
    void shouldListCustomersAsManager() {
        givenAuthenticatedAsManager()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/customers")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.OK.value())
                    .body("totalElements", Matchers.greaterThan(0));
    }

    @Test
    void shouldForbidCustomerOnAdminCustomersList() {
        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/customers")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.FORBIDDEN.value());
    }

}
