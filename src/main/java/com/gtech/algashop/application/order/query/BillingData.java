package com.gtech.algashop.application.order.query;

import com.gtech.algashop.application.commons.AddressData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// modelo que serve tanto para entrada e saída
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BillingData {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String document;

    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotNull
    @Valid
    private AddressData address;
}
