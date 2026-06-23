package com.gtech.algashop.core.ports.in.customer;

import com.gtech.algashop.core.ports.in.commons.AddressData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerUpdateInput {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String phone;
    @NotNull
    private Boolean promotionNotificationsAllowed;
    @NotNull
    private AddressData address;
}
