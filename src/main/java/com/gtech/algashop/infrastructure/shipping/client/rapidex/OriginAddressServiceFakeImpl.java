package com.gtech.algashop.infrastructure.fake;

import com.gtech.algashop.domain.model.commons.Address;
import com.gtech.algashop.domain.model.commons.ZipCode;
import com.gtech.algashop.domain.model.order.shipping.OriginAddressService;
import org.springframework.stereotype.Component;

@Component
public class OriginAddressServiceFakeImpl implements OriginAddressService {

    @Override
    public Address originAddress() {
        return Address.builder()
                .street("Bourbon Street")
                .number("1123")
                .neighborhood("North Ville")
                .city("New York")
                .state("South California")
                .zipCode(new ZipCode("12345"))
                .build();
    }
}
