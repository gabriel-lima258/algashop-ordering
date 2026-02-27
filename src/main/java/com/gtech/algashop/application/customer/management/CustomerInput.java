package com.gtech.algashop.application.model;

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
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private String document;
    private String phone;
    private String email;
    private Boolean promotionNotificationsAllowed;
    private AddressData address;
}
