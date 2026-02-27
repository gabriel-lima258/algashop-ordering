package com.gtech.algashop.application.customer.management;

import com.gtech.algashop.application.commons.AddressData;

import java.time.LocalDate;

public class CustomerUpdatedInputTestDataBuilder {

    public static CustomerUpdateInput.CustomerUpdateInputBuilder aUpdatedCustomer() {
        return CustomerUpdateInput.builder()
                .firstName("Matheus")
                .lastName("Damon")
                .phone("478-256-1123")
                .promotionNotificationsAllowed(true)
                .address(AddressData.builder()
                        .street("Bourbon Street")
                        .number("1200")
                        .complement("Apt. 901")
                        .neighborhood("North Ville")
                        .city("Yostfort")
                        .state("South Carolina")
                        .zipCode("70283")
                        .build());
    }
}
