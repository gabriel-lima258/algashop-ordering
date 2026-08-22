package com.gtech.algashop.infrastructure.adapters.in.web.customer;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.ports.in.customer.*;
import com.gtech.algashop.core.application.customer.query.CustomerOutputTestDataBuilder;
import com.gtech.algashop.core.domain.model.BusinessException;
import com.gtech.algashop.core.domain.model.costumer.CustomerEmailInUseException;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundException;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/// //////////////////////////
/// Teste de contrato do perfil /me
/// /////////////////////////

// O id do cliente nao vem mais do path: o controller pergunta ao SecurityCheckApplicationService,
// que aqui e um mock respondendo um UUID fixo - o mesmo que os stubs do create/update esperam.
@WebMvcTest(controllers = MyCustomerController.class)
class MyCustomerControllerContractTest {

    private static final UUID AUTHENTICATED_USER_ID =
            UUID.fromString("6e148bd5-47f6-4022-b9da-07cfaa294f7a");

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ForManagingCustomer customerManagementApplicationService;

    @MockitoBean
    private ForQueryCustomers customerQueryService;

    @MockitoBean
    private SecurityCheckApplicationService securityCheck;

    @BeforeEach
    void setupAll() {
        RestAssuredMockMvc.mockMvc(MockMvcBuilders.webAppContextSetup(context)
                .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                .build());
        RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();

        Mockito.when(securityCheck.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER_ID);
    }

    private static final String VALID_CREATE_JSON = """
    {
      "firstName": "John",
      "lastName": "Doe",
      "email": "johndoe@email.com",
      "document": "12345",
      "phone": "1191234564",
      "birthDate": "1991-07-05",
      "promotionNotificationsAllowed": false,
      "address": {
        "street": "Bourbon Street",
        "number": "2000",
        "complement": "apt 122",
        "neighborhood": "North Ville",
        "city": "Yostfort",
        "state": "South Carolina",
        "zipCode": "12321"
      }
    }
    """;

    private static final String VALID_UPDATE_JSON = """
    {
      "firstName": "John",
      "lastName": "Doe",
      "phone": "1191234564",
      "promotionNotificationsAllowed": false,
      "address": {
        "street": "Bourbon Street",
        "number": "2000",
        "complement": "apt 122",
        "neighborhood": "North Ville",
        "city": "Yostfort",
        "state": "South Carolina",
        "zipCode": "12321"
      }
    }
    """;

    @Test
    void createMyCustomerContract() {
        CustomerOutput customerOutput = CustomerOutputTestDataBuilder.existing()
                .id(AUTHENTICATED_USER_ID).build();

        Mockito.when(customerManagementApplicationService.create(
                        Mockito.eq(AUTHENTICATED_USER_ID), Mockito.any(CustomerInput.class)))
                .thenReturn(AUTHENTICATED_USER_ID);
        Mockito.when(customerQueryService.findById(AUTHENTICATED_USER_ID))
                .thenReturn(customerOutput);

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .body(VALID_CREATE_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .post("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .statusCode(HttpStatus.CREATED.value())
                .header("Location", Matchers.containsString("/api/v1/customers/me"))
                .body(
                        "id", Matchers.equalTo(AUTHENTICATED_USER_ID.toString()),
                        "registeredAt", Matchers.notNullValue(),
                        "firstName", Matchers.is("John"),
                        "lastName", Matchers.is("Doe"),
                        "email", Matchers.is("johndoe@email.com"),
                        "document", Matchers.is("12345"),
                        "phone", Matchers.is("1191234564"),
                        "birthDate", Matchers.is("1991-07-05"),
                        "promotionNotificationsAllowed", Matchers.is(false),
                        "loyaltyPoints", Matchers.is(0),
                        "address.street", Matchers.is("Bourbon Street"),
                        "address.number", Matchers.is("2000"),
                        "address.complement", Matchers.is("apt 122"),
                        "address.neighborhood", Matchers.is("North Ville"),
                        "address.city", Matchers.is("Yostfort"),
                        "address.state", Matchers.is("South Carolina"),
                        "address.zipCode", Matchers.is("12321")
                );
    }

    @Test
    void createMyCustomerErrorContract() {
        String jsonInput = """
                {
                  "firstName": "",
                  "lastName": "",
                  "email": "johndoe@email.com",
                  "document": "12345",
                  "phone": "1191234564",
                  "birthDate": "1991-07-05",
                  "promotionNotificationsAllowed": false,
                  "address": {
                    "street": "Bourbon Street",
                    "number": "2000",
                    "complement": "apt 122",
                    "neighborhood": "North Ville",
                    "city": "Yostfort",
                    "state": "South Carolina",
                    "zipCode": "12321"
                  }
                }
                """;

        RestAssuredMockMvc
                .given()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonInput)
                .when()
                .post("/api/v1/customers/me")
                .then()
                .assertThat()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(
                        "status", Matchers.is(HttpStatus.BAD_REQUEST.value()),
                        "type", Matchers.is("/errors/invalid-fields"),
                        "title", Matchers.notNullValue(),
                        "detail", Matchers.notNullValue(),
                        "instance", Matchers.notNullValue(),
                        "fields", Matchers.notNullValue()
                );
    }

    @Test
    void createMyCustomerError409Contract() {
        Mockito.when(customerManagementApplicationService.create(
                        Mockito.eq(AUTHENTICATED_USER_ID), Mockito.any(CustomerInput.class)))
                .thenThrow(CustomerEmailInUseException.class);

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .body(VALID_CREATE_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .post("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.CONFLICT.value())
                .body(
                        "status", Matchers.is(HttpStatus.CONFLICT.value()),
                        "type", Matchers.is("/errors/conflict"),
                        "title", Matchers.notNullValue(),
                        "instance", Matchers.notNullValue()
                );
    }

    @Test
    void createMyCustomerError422Contract() {
        Mockito.when(customerManagementApplicationService.create(
                        Mockito.eq(AUTHENTICATED_USER_ID), Mockito.any(CustomerInput.class)))
                .thenThrow(BusinessException.class);

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .body(VALID_CREATE_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .post("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .body(
                        "status", Matchers.is(HttpStatus.UNPROCESSABLE_ENTITY.value()),
                        "type", Matchers.is("/errors/unprocessable-entity"),
                        "title", Matchers.notNullValue(),
                        "instance", Matchers.notNullValue()
                );
    }

    @Test
    void createMyCustomerError500Contract() {
        Mockito.when(customerManagementApplicationService.create(
                        Mockito.eq(AUTHENTICATED_USER_ID), Mockito.any(CustomerInput.class)))
                .thenThrow(RuntimeException.class);

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .body(VALID_CREATE_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .post("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(
                        "status", Matchers.is(HttpStatus.INTERNAL_SERVER_ERROR.value()),
                        "type", Matchers.is("/errors/internal"),
                        "title", Matchers.notNullValue(),
                        "instance", Matchers.notNullValue()
                );
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Test
    void updateMyCustomerContract() {
        CustomerOutput customerOutput = CustomerOutputTestDataBuilder.existing()
                .id(AUTHENTICATED_USER_ID).build();

        Mockito.doNothing().when(customerManagementApplicationService)
                .update(Mockito.eq(AUTHENTICATED_USER_ID), Mockito.any(CustomerUpdateInput.class));
        Mockito.when(customerQueryService.findById(AUTHENTICATED_USER_ID)).thenReturn(customerOutput);

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .body(VALID_UPDATE_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .put("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .statusCode(HttpStatus.OK.value())
                .body(
                        "id", Matchers.equalTo(AUTHENTICATED_USER_ID.toString()),
                        "firstName", Matchers.notNullValue(),
                        "lastName", Matchers.notNullValue()
                );
    }

    @Test
    void updateMyCustomerError400Contract() {
        String jsonInput = """
        {
          "firstName": "",
          "lastName": "",
          "phone": "",
          "promotionNotificationsAllowed": false,
          "address": {
            "street": "Bourbon Street",
            "number": "2000",
            "complement": "apt 122",
            "neighborhood": "North Ville",
            "city": "Yostfort",
            "state": "South Carolina",
            "zipCode": "12321"
          }
        }
        """;

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .body(jsonInput)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .put("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.BAD_REQUEST.value())
                .body(
                        "status", Matchers.is(HttpStatus.BAD_REQUEST.value()),
                        "type", Matchers.is("/errors/invalid-fields"),
                        "title", Matchers.notNullValue(),
                        "detail", Matchers.notNullValue(),
                        "instance", Matchers.notNullValue(),
                        "fields", Matchers.notNullValue()
                );
    }

    @Test
    void updateMyCustomerError404Contract() {
        Mockito.doThrow(CustomerNotFoundException.class).when(customerManagementApplicationService)
                .update(Mockito.eq(AUTHENTICATED_USER_ID), Mockito.any(CustomerUpdateInput.class));

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .body(VALID_UPDATE_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .put("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                .body(
                        "status", Matchers.is(HttpStatus.NOT_FOUND.value()),
                        "type", Matchers.is("/errors/not-found"),
                        "title", Matchers.notNullValue(),
                        "instance", Matchers.notNullValue()
                );
    }

    @Test
    void updateMyCustomerError409Contract() {
        Mockito.doThrow(CustomerEmailInUseException.class).when(customerManagementApplicationService)
                .update(Mockito.eq(AUTHENTICATED_USER_ID), Mockito.any(CustomerUpdateInput.class));

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .body(VALID_UPDATE_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .put("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.CONFLICT.value())
                .body(
                        "status", Matchers.is(HttpStatus.CONFLICT.value()),
                        "type", Matchers.is("/errors/conflict"),
                        "title", Matchers.notNullValue(),
                        "instance", Matchers.notNullValue()
                );
    }

    @Test
    void updateMyCustomerError422Contract() {
        Mockito.doThrow(BusinessException.class).when(customerManagementApplicationService)
                .update(Mockito.eq(AUTHENTICATED_USER_ID), Mockito.any(CustomerUpdateInput.class));

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                    .body(VALID_UPDATE_JSON)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .put("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .body(
                        "status", Matchers.is(HttpStatus.UNPROCESSABLE_ENTITY.value()),
                        "type", Matchers.is("/errors/unprocessable-entity"),
                        "title", Matchers.notNullValue(),
                        "instance", Matchers.notNullValue()
                );
    }

    // =========================================================================
    // LOAD (GET /me)
    // =========================================================================

    @Test
    void loadMyCustomerContract() {
        CustomerOutput customerOutput = CustomerOutputTestDataBuilder.existing()
                .id(AUTHENTICATED_USER_ID).build();

        Mockito.when(customerQueryService.findById(AUTHENTICATED_USER_ID)).thenReturn(customerOutput);

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .statusCode(HttpStatus.OK.value())
                .body(
                        "id", Matchers.equalTo(AUTHENTICATED_USER_ID.toString()),
                        "firstName", Matchers.is("John"),
                        "lastName", Matchers.is("Doe"),
                        "email", Matchers.is("johndoe@email.com")
                );
    }

    @Test
    void loadMyCustomerError404Contract() {
        Mockito.when(customerQueryService.findById(AUTHENTICATED_USER_ID))
                .thenThrow(CustomerNotFoundException.class);

        RestAssuredMockMvc
                .given()
                    .accept(MediaType.APPLICATION_JSON_VALUE)
                .when()
                    .get("/api/v1/customers/me")
                .then()
                    .assertThat()
                    .contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE)
                    .statusCode(HttpStatus.NOT_FOUND.value())
                .body(
                        "status", Matchers.is(HttpStatus.NOT_FOUND.value()),
                        "type", Matchers.is("/errors/not-found"),
                        "title", Matchers.notNullValue(),
                        "instance", Matchers.notNullValue()
                );
    }

}
