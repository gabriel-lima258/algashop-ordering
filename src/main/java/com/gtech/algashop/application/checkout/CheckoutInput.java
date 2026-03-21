package com.gtech.algashop.application.checkout;

import com.gtech.algashop.application.order.query.BillingData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckoutInput {
    @NotNull
    private UUID shoppingCartId;

    @NotBlank
    private String paymentMethod;

    @NotNull
    @Valid
    private ShippingInput shipping;

    @NotNull
    @Valid
    private BillingData billing;
}
