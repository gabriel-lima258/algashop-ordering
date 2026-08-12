package com.gtech.algashop.infrastructure.adapters.in.web.customer;

import com.gtech.algashop.core.ports.in.customer.CustomerInput;
import com.gtech.algashop.core.application.customer.management.CustomerInputTestDataBuilder;
import com.gtech.algashop.core.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerPersistenceEntity;
import com.gtech.algashop.infrastructure.adapters.in.web.AbstractPresentationIT;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerControllerIT extends AbstractPresentationIT {

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

        String customerId = givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .post("/api/v1/customers")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.CREATED.value())
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body("id", Matchers.not(Matchers.emptyString()),
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

        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .post("/api/v1/customers")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    void shouldArchiveCustomer() {
        CustomerPersistenceEntity customer = CustomerPersistenceEntityTestDataBuilder
                .existingCustomer()
                .build();
        customerJpaEntityRepository.saveAndFlush(customer);

        UUID customerId = customer.getId();

        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .delete("/api/v1/customers/{customerId}", customerId)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NO_CONTENT.value());

        CustomerPersistenceEntity archivedCustomer = customerJpaEntityRepository
                .findById(customerId)
                .orElseThrow();

        assertThat(archivedCustomer.getArchived()).isTrue();
        assertThat(archivedCustomer.getArchivedAt()).isNotNull();
    }

    @Test
    void shouldReturnNotFoundWhenArchivingNonExistentCustomer() {
        UUID nonExistentId = UUID.randomUUID();

        givenAuthenticated()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .delete("/api/v1/customers/{customerId}", nonExistentId)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

    @Test
    void shouldReturnForbiddenWhenCreatingCustomerWithoutWriteScope() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();

        givenAuthenticatedWithNoScopeToken()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .body(input)
                .when()
                    .post("/api/v1/customers")
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.FORBIDDEN.value());

    }

    @Test
    void shouldReturnUnathorizedWhenExpiredTokenIsGiven() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();

        givenAuthenticatedWithExpiredToken()
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(input)
                .when()
                .post("/api/v1/customers")
                .then()
                .assertThat()
                .statusCode(HttpStatus.UNAUTHORIZED.value());

    }

}
