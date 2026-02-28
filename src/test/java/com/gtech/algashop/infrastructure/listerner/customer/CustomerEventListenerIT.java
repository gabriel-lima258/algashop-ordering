package com.gtech.algashop.infrastructure.listerner.customer;

import com.gtech.algashop.application.customer.loyaltypoints.CustomerLoyaltyPointsApplicationService;
import com.gtech.algashop.application.customer.notifications.CustomerNotificationApplicationService;
import com.gtech.algashop.domain.model.commons.Email;
import com.gtech.algashop.domain.model.commons.FullName;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.costumer.CustomerRegisteredEvent;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.OrderReadyEvent;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.gtech.algashop.application.customer.notifications.CustomerNotificationApplicationService.*;

@SpringBootTest
class CustomerEventListenerIT {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @MockitoSpyBean
    private CustomerEventListener customerEventListener;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    @MockitoBean
    private CustomerLoyaltyPointsApplicationService customerLoyaltyPointsApplicationService;

    @MockitoBean
    private CustomerNotificationApplicationService customerNotificationApplicationService;

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
        applicationEventPublisher.publishEvent(
                new CustomerRegisteredEvent(
                        new CustomerId(),
                        OffsetDateTime.now(),
                        new FullName("John", "Doe"),
                        new Email("johndoe@gmail.com")
                )
        );

        Mockito.verify(customerEventListener).listen(Mockito.any(CustomerRegisteredEvent.class));
        Mockito.verify(customerNotificationApplicationService)
                .notificateNewRegistration(Mockito.any(NotifyNewRegistrationInput.class));
    }
}