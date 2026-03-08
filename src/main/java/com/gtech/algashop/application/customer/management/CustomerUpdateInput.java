package com.gtech.algashop.application.customer.management;

import com.gtech.algashop.application.commons.AddressData;
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
