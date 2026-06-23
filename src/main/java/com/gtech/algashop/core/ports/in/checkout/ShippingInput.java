package com.gtech.algashop.core.ports.in.checkout;

import com.gtech.algashop.core.ports.in.commons.AddressData;
import com.gtech.algashop.core.ports.in.order.RecipientData;
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
