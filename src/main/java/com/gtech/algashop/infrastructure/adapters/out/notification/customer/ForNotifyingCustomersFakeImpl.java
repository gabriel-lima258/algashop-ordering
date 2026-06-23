package com.gtech.algashop.infrastructure.adapters.out.notification.customer;

import com.gtech.algashop.core.ports.out.customer.ForNotifyingCustomers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForNotifyingCustomersFakeImpl implements ForNotifyingCustomers {

    @Override
    public void notificateNewRegistration(NotifyNewRegistrationInput input) {
        log.info("Welcome {}", input.firstName());
        log.info("User account with email {}", input.email());
    }
}
