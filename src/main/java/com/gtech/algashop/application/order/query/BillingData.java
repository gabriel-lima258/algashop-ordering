package com.gtech.algashop.application.order.query;

import com.gtech.algashop.application.commons.AddressData;
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
    private String firstName;
    private String lastName;
    private String document;
    private String email;
    private String phone;
    private AddressData address;
}
