package com.gtech.algashop.application.customer.notifications;

import java.util.UUID;

public interface CustomerNotificationApplicationService {
    // evento a ser disparado
    void notificateNewRegistration(NotifyNewRegistrationInput input);

    // input dos valores que eventos irá utilizar
    record NotifyNewRegistrationInput(UUID customerId, String firstName, String email){}
}
