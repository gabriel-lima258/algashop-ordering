package com.gtech.algashop.core.application.checkout;

import com.gtech.algashop.core.ports.in.checkout.ShippingInput;
import com.gtech.algashop.core.ports.in.commons.AddressData;
import com.gtech.algashop.core.domain.model.commons.*;
import com.gtech.algashop.core.domain.model.order.Recipient;
import com.gtech.algashop.core.domain.model.order.Shipping;
import com.gtech.algashop.core.domain.model.order.shipping.ShippingCostService;
import org.springframework.stereotype.Component;

@Component
class ShippingInputDisassembler {

    // converte input para o dominio
    public Shipping toDomainModel(ShippingInput shippingInput,
                                  ShippingCostService.CalculationResult shippingCalculationResult) {
        AddressData address = shippingInput.getAddress();
        return Shipping.builder()
                .cost(shippingCalculationResult.cost())
                .expectedDate(shippingCalculationResult.expectedDate())
                .recipient(Recipient.builder()
                        .fullName(new FullName(
                                shippingInput.getRecipient().getFirstName(),
                                shippingInput.getRecipient().getLastName()))
                        .document(new Document(shippingInput.getRecipient().getDocument()))
                        .phone(new Phone(shippingInput.getRecipient().getPhone()))
                        .build())
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
