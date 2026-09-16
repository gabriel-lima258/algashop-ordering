package com.gtech.algashop.core.application.checkout.command;

// Endereco achatado, reutilizado por BillingSnapshot e ShippingSnapshot.
public record AddressSnapshot(
        String street,
        String complement,
        String neighborhood,
        String number,
        String city,
        String state,
        String zipCode
) {}