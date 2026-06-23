package com.gtech.algashop.core.domain.model.costumer;

import com.gtech.algashop.core.domain.model.commons.*;
import com.gtech.algashop.core.domain.model.DomainService;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@DomainService
public class CustomerRegistrationService {

    private final Customers customers;

    public Customer register(
            FullName fullName, BirthDate birthDate, Email email,
            Phone phone, Document document, Boolean promotionNotificationsAllowed,
            Address address
    ) {
        Customer customer = Customer.brandNew()
                .fullName(fullName)
                .birthDate(birthDate)
                .email(email)
                .phone(phone)
                .document(document)
                .promotionNotificationsAllowed(promotionNotificationsAllowed)
                .address(address)
                .build();

        verifyEmailUniqueness(customer.email(), customer.id());

        return customer;
    }

    public void changeEmail(Customer customer, Email newEmail) {
        verifyEmailUniqueness(newEmail, customer.id());
        customer.changeEmail(newEmail);
    }

    private void verifyEmailUniqueness(Email email, CustomerId id) {
        if (!customers.isEmailUnique(email, id)) {
            throw new CustomerEmailInUseException(id);
        }
    }

}
