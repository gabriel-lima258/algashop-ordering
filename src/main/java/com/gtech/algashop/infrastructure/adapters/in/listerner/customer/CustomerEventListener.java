package com.gtech.algashop.infrastructure.adapters.in.listerner.customer;

import com.gtech.algashop.core.ports.in.customer.ForAddingLoyaltyPoints;
import com.gtech.algashop.core.ports.in.customer.ForConfirmCustomerRegistration;
import com.gtech.algashop.core.ports.out.customer.ForNotifyingCustomers;
import com.gtech.algashop.core.domain.model.costumer.CustomerArchivedEvent;
import com.gtech.algashop.core.domain.model.costumer.CustomerRegisteredEvent;
import com.gtech.algashop.core.domain.model.order.OrderReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// classe de escuta dos eventos publicos em customer
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerEventListener {

    // efeito colateral do evento
    private final ForConfirmCustomerRegistration forConfirmCustomerRegistration;
    private final ForAddingLoyaltyPoints forAddingLoyaltyPoints;

    @EventListener
    public void listen(CustomerRegisteredEvent event) {
        log.info("CustomerRegisteredEvent listen 1");
        forConfirmCustomerRegistration.confirm(event.customerId().value());
    }

    @EventListener
    public void listen(CustomerArchivedEvent event) {
        log.info("CustomerArchivedEvent listen 1");
    }

    @EventListener
    public void listen(OrderReadyEvent event) {
        forAddingLoyaltyPoints.addLoyaltyPoints(event.customerId().value(), event.orderId().toString());
    }
}
