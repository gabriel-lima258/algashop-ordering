package com.gtech.algashop.application.checkout;

import com.gtech.algashop.application.commons.AddressData;
import com.gtech.algashop.application.order.query.RecipientData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingInput {
    @NotNull
    @Valid
    private RecipientData recipient;

    @NotNull
    @Valid
    private AddressData address;
}
