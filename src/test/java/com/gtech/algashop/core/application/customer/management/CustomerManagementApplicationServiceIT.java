package com.gtech.algashop.core.application.customer.management;

import com.gtech.algashop.core.application.AbstractIntegrationTest;
import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.ports.out.customer.ForNotifyingCustomers;
import com.gtech.algashop.core.ports.in.customer.CustomerOutput;
import com.gtech.algashop.core.ports.in.customer.ForQueryCustomers;
import com.gtech.algashop.core.domain.model.costumer.*;
import com.gtech.algashop.core.ports.in.customer.CustomerInput;
import com.gtech.algashop.core.ports.in.customer.CustomerUpdateInput;
import com.gtech.algashop.core.ports.in.customer.ForManagingCustomer;
import com.gtech.algashop.infrastructure.adapters.in.listerner.customer.CustomerEventListener;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.UUID;

class CustomerManagementApplicationServiceIT extends AbstractIntegrationTest {

    @Autowired
    private ForManagingCustomer customerManagementApplicationService;

    // bean real (OAuth2SecurityCheckApplicationServiceImpl) lendo a identidade do @WithMockJwt
    @Autowired
    private SecurityCheckApplicationService securityCheck;

    // verifica se esta sendo chamado
    @MockitoSpyBean
    private CustomerEventListener customerEventListener;

    @MockitoSpyBean
    private ForNotifyingCustomers customerNotificationApplicationService;

    // CQRS de consulta de banco
    @Autowired
    private ForQueryCustomers customerQueryService;

    @Test
    void shouldRegister() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();
        UUID customerId = customerManagementApplicationService.create(securityCheck.getAuthenticatedUserId(), input);
        Assertions.assertThat(customerId).isNotNull();

        CustomerOutput customerOutput = customerQueryService.findById(customerId);

        Assertions.assertThat(customerOutput)
                        .extracting(
                                CustomerOutput::getId,
                                CustomerOutput::getFirstName,
                                CustomerOutput::getLastName,
                                CustomerOutput::getBirthDate,
                                CustomerOutput::getDocument,
                                CustomerOutput::getPhone,
                                CustomerOutput::getEmail,
                                CustomerOutput::getPromotionNotificationsAllowed
                        ).containsExactly(
                                customerId,
                                "John",
                                "Doe",
                                LocalDate.of(1991, 7,5),
                                "255-08-0578",
                                "478-256-2604",
                                input.getEmail(),
                                false
                        );

        Assertions.assertThat(customerOutput.getRegisteredAt()).isNotNull();
        Assertions.assertThat(customerOutput.getAddress()).isNotNull();

        Mockito.verify(customerEventListener)
                .listen(Mockito.any(CustomerRegisteredEvent.class));

        // verifica os colaterais do evento disparado
        Mockito.verify(customerNotificationApplicationService)
                .notificateNewRegistration(Mockito.any(
                        ForNotifyingCustomers.NotifyNewRegistrationInput.class
                ));
    }

    @Test
    void shouldUpdate() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();
        CustomerUpdateInput updatedInput = CustomerUpdatedInputTestDataBuilder.aUpdatedCustomer().build();

        UUID customerId = customerManagementApplicationService.create(securityCheck.getAuthenticatedUserId(), input);
        Assertions.assertThat(customerId).isNotNull();

        customerManagementApplicationService.update(customerId, updatedInput);

        CustomerOutput customerOutput = customerQueryService.findById(customerId);

        Assertions.assertThat(customerOutput)
                .extracting(
                        CustomerOutput::getFirstName,
                        CustomerOutput::getLastName,
                        CustomerOutput::getPhone,
                        CustomerOutput::getPromotionNotificationsAllowed
                ).containsExactly(
                        "Matheus",
                        "Damon",
                        "478-256-1123",
                        true
                );

        Assertions.assertThat(updatedInput.getAddress()).isNotNull();
    }

    @Test
    void shouldArchiveCustomer() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();
        UUID customerId = customerManagementApplicationService.create(securityCheck.getAuthenticatedUserId(), input);

        customerManagementApplicationService.archive(customerId);

        CustomerOutput customerOutput = customerQueryService.findById(customerId);

        Assertions.assertThat(customerOutput.getArchived()).isTrue();
        Assertions.assertThat(customerOutput.getArchivedAt()).isNotNull();
        Assertions.assertThat(customerOutput.getFirstName()).isEqualTo("Anonymous");
        Assertions.assertThat(customerOutput.getLastName()).isEqualTo("Anonymous");
        Assertions.assertThat(customerOutput.getEmail()).endsWith("@anonymous.com");
        Assertions.assertThat(customerOutput.getPhone()).isEqualTo("000-000-0000");
        Assertions.assertThat(customerOutput.getDocument()).isEqualTo("000-00-0000");
        Assertions.assertThat(customerOutput.getBirthDate()).isNull();
        Assertions.assertThat(customerOutput.getPromotionNotificationsAllowed()).isFalse();
        Assertions.assertThat(customerOutput.getAddress().getNumber()).isEqualTo("Anonymized");
        Assertions.assertThat(customerOutput.getAddress().getComplement()).isNull();

        Mockito.verify(customerEventListener)
                .listen(Mockito.any(CustomerArchivedEvent.class));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenArchivingNonExistentCustomer() {
        UUID nonExistentId = UUID.randomUUID();

        Assertions.assertThatThrownBy(() -> customerManagementApplicationService.archive(nonExistentId))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void shouldThrowCustomerArchivedExceptionWhenArchivingAlreadyArchivedCustomer() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();
        UUID customerId = customerManagementApplicationService.create(securityCheck.getAuthenticatedUserId(), input);

        customerManagementApplicationService.archive(customerId);

        Assertions.assertThatThrownBy(() -> customerManagementApplicationService.archive(customerId))
                .isInstanceOf(CustomerArchivedException.class);
    }

    @Test
    void shouldChangeEmail() {
        UUID customerId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());

        customerManagementApplicationService.changeEmail(customerId, "newemail@email.com");

        CustomerOutput customerOutput = customerQueryService.findById(customerId);
        Assertions.assertThat(customerOutput.getEmail()).isEqualTo("newemail@email.com");
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenChangingEmailForNonExistentCustomer() {
        UUID nonExistentId = UUID.randomUUID();

        Assertions.assertThatThrownBy(() ->
                customerManagementApplicationService.changeEmail(nonExistentId, "newemail@email.com"))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void shouldThrowCustomerArchivedExceptionWhenChangingEmailForArchivedCustomer() {
        UUID customerId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());

        customerManagementApplicationService.archive(customerId);

        Assertions.assertThatThrownBy(() ->
                customerManagementApplicationService.changeEmail(customerId, "newemail@email.com"))
                .isInstanceOf(CustomerArchivedException.class);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenChangingEmailWithInvalidFormat() {
        UUID customerId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());

        Assertions.assertThatThrownBy(() ->
                customerManagementApplicationService.changeEmail(customerId, "email-invalido"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowCustomerEmailIsInUseExceptionWhenEmailAlreadyBelongsToAnotherCustomer() {
        CustomerInput firstInput = CustomerInputTestDataBuilder.aCustomer().build();
        customerManagementApplicationService.create(securityCheck.getAuthenticatedUserId(), firstInput);

        // o segundo cliente precisa de outro id: repetir o id do token sobrescreveria o primeiro
        UUID secondCustomerId = customerManagementApplicationService.create(
                UUID.randomUUID(), CustomerInputTestDataBuilder.aCustomer().build());

        Assertions.assertThatThrownBy(() ->
                customerManagementApplicationService.changeEmail(secondCustomerId, firstInput.getEmail()))
                .isInstanceOf(CustomerEmailInUseException.class);
    }

}