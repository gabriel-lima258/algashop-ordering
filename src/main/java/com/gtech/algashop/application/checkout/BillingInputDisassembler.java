package com.gtech.algashop.application.checkout;

import com.gtech.algashop.application.commons.AddressData;
import com.gtech.algashop.domain.model.commons.*;
import com.gtech.algashop.domain.model.order.Billing;
import org.springframework.stereotype.Component;

@Component
class BillingInputDisassembler {

    // converte input para o dominio
    public Billing toDomainModel(BillingData billingData) {
        AddressData address = billingData.getAddress();
        return Billing.builder()
                .fullName(new FullName(billingData.getFirstName(), billingData.getLastName()))
                .document(new Document(billingData.getDocument()))
                .phone(new Phone(billingData.getPhone()))
                .email(new Email(billingData.getEmail()))
                .address(Address.builder()
                        .street(address.getStreet())
                        .number(address.getNumber())
                        .complement(address.getComplement())
                        .neighborhood(address.getNeighborhood())
                        .city(address.getCity())
                        .state(address.getState())
                        .zipCode(new ZipCode(address.getZipCode()))
                        .build())
                .build();
    }
}
