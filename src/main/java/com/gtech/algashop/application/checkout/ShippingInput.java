package com.gtech.algashop.application.checkout;

import com.gtech.algashop.application.commons.AddressData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShippingInput {
    private RecipientData recipient;
    private AddressData address;
}
