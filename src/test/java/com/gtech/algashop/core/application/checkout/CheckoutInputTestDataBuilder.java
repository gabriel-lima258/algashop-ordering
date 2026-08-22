package com.gtech.algashop.core.application.checkout;

import com.gtech.algashop.core.ports.in.checkout.CheckoutInput;
import com.gtech.algashop.core.ports.in.checkout.ShippingInput;
import com.gtech.algashop.core.ports.in.commons.AddressData;
import com.gtech.algashop.core.ports.in.order.BillingData;
import com.gtech.algashop.core.ports.in.order.RecipientData;

import java.util.UUID;

public class CheckoutInputTestDataBuilder {

    // o carrinho deixou de vir no corpo: e resolvido pelo cliente autenticado, entao o
    // builder nao recebe mais shoppingCartId - quem precisa de dono seta customerId
    public static CheckoutInput.CheckoutInputBuilder aCheckoutInput() {
        return CheckoutInput.builder()
                .paymentMethod("CREDIT_CARD")
                .creditCardId(UUID.randomUUID())
                .shipping(ShippingInput.builder()
                        .recipient(RecipientData.builder()
                                .firstName("John")
                                .lastName("Doe")
                                .document("255-08-0578")
                                .phone("478-256-2604")
                                .build())
                        .address(AddressData.builder()
                                .street("Elm Street")
                                .number("456")
                                .complement("House A")
                                .neighborhood("Central Park")
                                .city("Springfield")
                                .state("Illinois")
                                .zipCode("62704")
                                .build())
                        .build())
                .billing(BillingData.builder()
                        .firstName("Matt")
                        .lastName("Damon")
                        .phone("123-321-1112")
                        .document("123-45-6789")
                        .email("matt.damon@email.com")
                        .address(AddressData.builder()
                                .street("Amphitheatre Parkway")
                                .number("1600")
                                .complement("")
                                .neighborhood("Mountain View")
                                .city("Mountain View")
                                .state("California")
                                .zipCode("94043")
                                .build())
                        .build());
    }
}
