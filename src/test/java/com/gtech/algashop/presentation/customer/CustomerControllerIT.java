package com.gtech.algashop.presentation.customer;

import com.gtech.algashop.application.customer.management.CustomerInput;
import com.gtech.algashop.application.customer.management.CustomerInputTestDataBuilder;
import com.gtech.algashop.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntity;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CustomerControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    private CustomerJpaEntityRepository customerJpaEntityRepository;

    @BeforeEach
    void setup() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        RestAssured.port = port;
    }

    @Test
    void shouldCreateCustomer() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();

        String customerId = RestAssured
                .given()
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

        RestAssured
                .given()
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

        RestAssured
                .given()
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

        RestAssured
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .delete("/api/v1/customers/{customerId}", nonExistentId)
                .then()
                    .assertThat()
                    .statusCode(HttpStatus.NOT_FOUND.value())
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    }

}
