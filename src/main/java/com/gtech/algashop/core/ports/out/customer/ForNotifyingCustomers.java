package com.gtech.algashop.core.ports.out.customer;

import java.util.UUID;

public interface ForNotifyingCustomers {
    // evento a ser disparado
    void notificateNewRegistration(NotifyNewRegistrationInput input);

    // input dos valores que eventos irá utilizar
    record NotifyNewRegistrationInput(UUID customerId, String firstName, String email){}
}
