package com.gtech.algashop.domain.model.factory;

import com.gtech.algashop.domain.model.util.IdGenerator;
import com.gtech.algashop.infrastructure.persistence.embeddable.AddressEmbeddable;
import com.gtech.algashop.infrastructure.persistence.entity.CustomerPersistenceEntity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public class CustomerPersistenceEntityTestDataBuilder {

    private CustomerPersistenceEntityTestDataBuilder() {}

    public static CustomerPersistenceEntity.CustomerPersistenceEntityBuilder existingCustomer() {
        return CustomerPersistenceEntity.builder()
                .id(IdGenerator.generateTimeBasedUUID())
                .firstName("John")
                .lastName("Doe")
                .birthDate(LocalDate.now())
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
