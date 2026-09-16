package com.gtech.algashop.core.application.checkout.command;

// Dados de cobranca do OrderSnapshot: FullName/Document/Phone/Email achatados em String.
public record BillingSnapshot(
        String firstName,
        String lastName,
        String document,
        String phone,
        String email,
        AddressSnapshot address
) {
}
