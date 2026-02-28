package com.gtech.algashop.application.customer.management;

import com.gtech.algashop.application.customer.notifications.CustomerNotificationApplicationService;
import com.gtech.algashop.domain.model.costumer.*;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import com.gtech.algashop.infrastructure.listerner.customer.CustomerEventListener;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest
@Transactional // nao afeta o cenario de teste do outro
class CustomerManagementApplicationServiceIT {

    @Autowired
    private CustomerManagementApplicationService customerManagementApplicationService;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    // verifica se esta sendo chamado
    @MockitoSpyBean
    private CustomerEventListener customerEventListener;

    @MockitoSpyBean
    private CustomerNotificationApplicationService customerNotificationApplicationService;

    @Test
    void shouldRegister() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();
        UUID customerId = customerManagementApplicationService.create(input);
        Assertions.assertThat(customerId).isNotNull();

        CustomerOutput customerOutput = customerManagementApplicationService.findById(customerId);

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
                                "johndoe@email.com",
                                false
                        );

        Assertions.assertThat(customerOutput.getRegisteredAt()).isNotNull();
        Assertions.assertThat(customerOutput.getAddress()).isNotNull();

        Mockito.verify(customerEventListener)
                .listen(Mockito.any(CustomerRegisteredEvent.class));

        // verifica os colaterais do evento disparado
        Mockito.verify(customerNotificationApplicationService)
                .notificateNewRegistration(Mockito.any(
                        CustomerNotificationApplicationService.NotifyNewRegistrationInput.class
                ));
    }

    @Test
    void shouldUpdate() {
        CustomerInput input = CustomerInputTestDataBuilder.aCustomer().build();
        CustomerUpdateInput updatedInput = CustomerUpdatedInputTestDataBuilder.aUpdatedCustomer().build();

        UUID customerId = customerManagementApplicationService.create(input);
        Assertions.assertThat(customerId).isNotNull();

        customerManagementApplicationService.update(customerId, updatedInput);

        CustomerOutput customerOutput = customerManagementApplicationService.findById(customerId);

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
        UUID customerId = customerManagementApplicationService.create(input);

        customerManagementApplicationService.archive(customerId);

        CustomerOutput customerOutput = customerManagementApplicationService.findById(customerId);

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
        UUID customerId = customerManagementApplicationService.create(input);

        customerManagementApplicationService.archive(customerId);

        Assertions.assertThatThrownBy(() -> customerManagementApplicationService.archive(customerId))
                .isInstanceOf(CustomerArchivedException.class);
    }

    @Test
    void shouldChangeEmail() {
        UUID customerId = customerManagementApplicationService.create(
                CustomerInputTestDataBuilder.aCustomer().build());

        customerManagementApplicationService.changeEmail(customerId, "newemail@email.com");

        CustomerOutput customerOutput = customerManagementApplicationService.findById(customerId);
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
                CustomerInputTestDataBuilder.aCustomer().build());

        customerManagementApplicationService.archive(customerId);

        Assertions.assertThatThrownBy(() ->
                customerManagementApplicationService.changeEmail(customerId, "newemail@email.com"))
                .isInstanceOf(CustomerArchivedException.class);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenChangingEmailWithInvalidFormat() {
        UUID customerId = customerManagementApplicationService.create(
                CustomerInputTestDataBuilder.aCustomer().build());

        Assertions.assertThatThrownBy(() ->
                customerManagementApplicationService.changeEmail(customerId, "email-invalido"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowCustomerEmailIsInUseExceptionWhenEmailAlreadyBelongsToAnotherCustomer() {
        customerManagementApplicationService.create(
                CustomerInputTestDataBuilder.aCustomer().build());

        UUID secondCustomerId = customerManagementApplicationService.create(
                CustomerInputTestDataBuilder.aCustomer().email("janedoe@email.com").build());

        Assertions.assertThatThrownBy(() ->
                customerManagementApplicationService.changeEmail(secondCustomerId, "johndoe@email.com"))
                .isInstanceOf(CustomerEmailInUseException.class);
    }

}