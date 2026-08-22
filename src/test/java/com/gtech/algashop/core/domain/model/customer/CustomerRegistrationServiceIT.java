package com.gtech.algashop.core.domain.model.customer;

import com.gtech.algashop.core.domain.model.AbstractDomainIT;
import com.gtech.algashop.core.domain.model.commons.*;
import com.gtech.algashop.core.domain.model.costumer.BirthDate;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.costumer.CustomerRegistrationService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

class CustomerRegistrationServiceIT extends AbstractDomainIT {

    @Autowired
    private CustomerRegistrationService customerRegistrationService;

    @Test
    void shouldRegister() {
        Customer customer = customerRegistrationService.register(
                new CustomerId(),
                new FullName("John", "Doe"),
                new BirthDate(LocalDate.of(1991, 7, 5)),
                new Email("johndoe@email.com"),
                new Phone("478-256-2604"),
                new Document("255-08-0578"),
                true,
                Address.builder()
                        .street("Bourbon Street")
                        .number("1134")
                        .neighborhood("North Ville")
                        .city("Yostfort")
                        .state("South Carolina")
                        .zipCode(new ZipCode("70283"))
                        .complement("Apt. 901")
                        .build()
        );

        Assertions.assertThat(customer.fullName()).isEqualTo(new FullName("John", "Doe"));
        Assertions.assertThat(customer.email()).isEqualTo(new Email("johndoe@email.com"));
    }
}