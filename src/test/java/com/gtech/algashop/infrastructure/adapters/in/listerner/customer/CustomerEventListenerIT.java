package com.gtech.algashop.infrastructure.adapters.in.listerner.customer;

import com.gtech.algashop.core.application.AbstractIntegrationTest;
import com.gtech.algashop.core.application.customer.query.CustomerOutputTestDataBuilder;
import com.gtech.algashop.core.ports.in.customer.ForAddingLoyaltyPoints;
import com.gtech.algashop.core.ports.out.customer.ForNotifyingCustomers;
import com.gtech.algashop.core.ports.out.customer.ForObtainingCustomers;
import com.gtech.algashop.core.domain.model.commons.Email;
import com.gtech.algashop.core.domain.model.commons.FullName;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.costumer.CustomerRegisteredEvent;
import com.gtech.algashop.core.domain.model.order.OrderId;
import com.gtech.algashop.core.domain.model.order.OrderReadyEvent;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.OffsetDateTime;
import java.util.UUID;

class CustomerEventListenerIT extends AbstractIntegrationTest {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @MockitoSpyBean
    private CustomerEventListener customerEventListener;

    @MockitoBean
    private ForAddingLoyaltyPoints customerLoyaltyPointsApplicationService;

    @MockitoBean
    private ForNotifyingCustomers customerNotificationApplicationService;

    @MockitoBean
    private ForObtainingCustomers forObtainingCustomers;

    @Test
    void shouldListenOrderReadyEvent() {
        applicationEventPublisher.publishEvent(
                new OrderReadyEvent(
                        new OrderId(),
                        new CustomerId(),
                        OffsetDateTime.now()
                )
        );

        Mockito.verify(customerEventListener).listen(Mockito.any(OrderReadyEvent.class));
        Mockito.verify(customerLoyaltyPointsApplicationService)
                .addLoyaltyPoints(
                        Mockito.any(UUID.class),
                        Mockito.any(String.class)
                );
    }

    @Test
    void shouldListenCustomerRegisteredEvent() {
        CustomerId customerId = new CustomerId();

        Mockito.when(forObtainingCustomers.findById(customerId.value()))
                .thenReturn(CustomerOutputTestDataBuilder.existing().id(customerId.value()).build());

        applicationEventPublisher.publishEvent(
                new CustomerRegisteredEvent(
                        customerId,
                        OffsetDateTime.now(),
                        new FullName("John", "Doe"),
                        new Email("johndoe@gmail.com")
                )
        );

        Mockito.verify(customerEventListener).listen(Mockito.any(CustomerRegisteredEvent.class));
        Mockito.verify(customerNotificationApplicationService)
                .notificateNewRegistration(Mockito.any(ForNotifyingCustomers.NotifyNewRegistrationInput.class));
    }
}