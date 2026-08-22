package com.gtech.algashop.core.ports.in.checkout;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gtech.algashop.core.ports.in.order.BillingData;
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
    @JsonIgnore
    private UUID customerId;

    @NotBlank
    private String paymentMethod;

    @NotNull
    @Valid
    private ShippingInput shipping;

    @NotNull
    @Valid
    private BillingData billing;

    private UUID creditCardId;
}
