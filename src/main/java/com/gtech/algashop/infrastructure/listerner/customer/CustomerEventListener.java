package com.gtech.algashop.infrastructure.listerner.customer;

import com.gtech.algashop.application.customer.loyaltypoints.CustomerLoyaltyPointsApplicationService;
import com.gtech.algashop.application.customer.notifications.CustomerNotificationApplicationService;
import com.gtech.algashop.domain.model.costumer.CustomerArchivedEvent;
import com.gtech.algashop.domain.model.costumer.CustomerLoyaltyPointsService;
import com.gtech.algashop.domain.model.costumer.CustomerRegisteredEvent;
import com.gtech.algashop.domain.model.order.OrderReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.gtech.algashop.application.customer.notifications.CustomerNotificationApplicationService.*;

// classe de escuta dos eventos publicos em customer
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    // efeito colateral do evento
    private final CustomerNotificationApplicationService customerNotificationService;
    private final CustomerLoyaltyPointsApplicationService customerLoyaltyPointsService;

    @EventListener
    public void listen(CustomerRegisteredEvent event) {
        log.info("CustomerRegisteredEvent listen 1");
        NotifyNewRegistrationInput input = new NotifyNewRegistrationInput(
                event.customerId().value(),
                event.fullName().firstName(),
                event.email().email()
        );
        customerNotificationService.notificateNewRegistration(input);
    }

    @EventListener
    public void listen(CustomerArchivedEvent event) {
        log.info("CustomerArchivedEvent listen 1");
    }

    @EventListener
    public void listen(OrderReadyEvent event) {
        customerLoyaltyPointsService.addLoyaltyPoints(event.customerId().value(), event.orderId().toString());
    }
}
