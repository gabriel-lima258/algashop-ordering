package com.gtech.algashop.domain.model.customer;

import com.gtech.algashop.domain.model.commons.*;
import com.gtech.algashop.domain.model.costumer.BirthDate;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.CustomerRegistrationService;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@SpringBootTest
class CustomerRegistrationServiceIT {

    @Autowired
    private CustomerRegistrationService customerRegistrationService;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    @Test
    void shouldRegister() {
        Customer customer = customerRegistrationService.register(
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