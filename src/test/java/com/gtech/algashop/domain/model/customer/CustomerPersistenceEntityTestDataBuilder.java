package com.gtech.algashop.domain.model.customer;

import com.gtech.algashop.infrastructure.persistence.commons.AddressEmbeddable;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

public class CustomerPersistenceEntityTestDataBuilder {

    private CustomerPersistenceEntityTestDataBuilder() {}

    public static CustomerPersistenceEntity.CustomerPersistenceEntityBuilder existingCustomer() {
        return CustomerPersistenceEntity.builder()
                .id(DEFAULT_CUSTOMER_ID.value())
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.of(1991, 7,5))
                .email("john@gmail.com")
                .phone("6199273293")
                .document("12345")
                .promotionNotificationsAllowed(true)
                .archived(false)
                .loyaltyPoints(100)
                .registeredAt(OffsetDateTime.now())
                .address(anAddressEmbeddable());
    }

    public static AddressEmbeddable anAddressEmbeddable() {
        return AddressEmbeddable.builder()
                .street("Bourbon Street")
                .number("1134")
                .neighborhood("North Ville")
                .city("York")
                .state("South California")
                .zipCode("12345")
                .complement("Apt. 114")
                .build();
    }
}
