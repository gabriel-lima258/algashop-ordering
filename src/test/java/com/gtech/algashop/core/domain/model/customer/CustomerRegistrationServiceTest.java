package com.gtech.algashop.core.domain.model.customer;

import com.gtech.algashop.core.domain.model.commons.*;
import com.gtech.algashop.core.domain.model.costumer.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

// O Mockito injeta uma dependência falsa (mock) no lugar da dependência real.
@ExtendWith(MockitoExtension.class)
class CustomerRegistrationServiceTest {

    // possivel dependencia fake
    @Mock
    private Customers customers;

    // aqui instanciamos services e injetamos o customers dentro dele
    @InjectMocks
    private CustomerRegistrationService customerRegistrationService;

    @Test
    void shouldRegister() {
        // aqui dizemos que sempre o email é unico para que não precise consultar o banco de dados
        Mockito.when(customers.isEmailUnique(Mockito.any(Email.class), Mockito.any(CustomerId.class)))
                .thenReturn(true);

        Customer customer = customerRegistrationService.register(
                new FullName("John", "Doe"),
                new BirthDate(LocalDate.of(1991, 7, 5)),
                new Email("john@gmail.com"),
                new Phone("478-256-2684"),
                new Document("255-08-9548"),
                true,
                Address.builder()
                        .street("Bourbon Street")
                        .number("1134")
                        .neighborhood("North Ville")
                        .city("York")
                        .state("South California")
                        .zipCode(new ZipCode("83743"))
                        .complement("Apt. 114")
                        .build()
        );

        Assertions.assertThat(customer.fullName()).isEqualTo(new FullName("John", "Doe"));
        Assertions.assertThat(customer.email()).isEqualTo(new Email("john@gmail.com"));
    }
}