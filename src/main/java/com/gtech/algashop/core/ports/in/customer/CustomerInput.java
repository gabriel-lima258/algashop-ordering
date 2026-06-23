package com.gtech.algashop.core.ports.in.customer;

import com.gtech.algashop.core.ports.in.commons.AddressData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerInput {
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotBlank
    private String document;

    @NotBlank
    private String phone;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private Boolean promotionNotificationsAllowed;

    @NotNull
    @Valid
    private AddressData address;
}
