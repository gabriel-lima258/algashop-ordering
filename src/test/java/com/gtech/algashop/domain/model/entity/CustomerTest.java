package com.gtech.algashop.domain.model.entity;

import com.gtech.algashop.domain.model.entity.factory.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.entity.VO.*;
import com.gtech.algashop.domain.model.exceptions.CustomerArchivedException;
import com.gtech.algashop.domain.model.exceptions.LoyaltyValueException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CustomerTest {

    @Test
    void given_invalidEmail_whenTryCreateCustomer_shouldThrowException() {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    CustomerTestDataBuilder.brandNewCustomer().email(new Email("invalid")).build();
                });
    }

    @Test
    void given_invalidEmail_whenTryChangeEmail_shouldThrowException() {

        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();

        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> {
                    customer.changeEmail(new Email("invalid"));
                });
    }

    @Test
    void given_unarchivedCustomer_whenArchive_shouldAnonymize() {

        Customer customer =  CustomerTestDataBuilder.existingCustomer().build();

        customer.archive();

        Assertions.assertWith(customer,
                c -> Assertions.assertThat(c.fullName()).isEqualTo(new FullName("Anonymous", "Anonymous")),
                c -> Assertions.assertThat(c.email()).isNotEqualTo(new Email("johndoe@email.com")),
                c -> Assertions.assertThat(c.phone()).isEqualTo(new Phone("000-000-0000")),
                c -> Assertions.assertThat(c.document()).isEqualTo(new Document("000-00-0000")),
                c -> Assertions.assertThat(c.birthDate()).isNull(),
                c -> Assertions.assertThat(c.isPromotionNotificationsAllowed()).isFalse(),
                c -> Assertions.assertThat(c.address()).isEqualTo(
                        Address.builder()
                                .street("Bourbon Street")
                                .number("Anonymized")
                                .neighborhood("North Ville")
                                .city("York")
                                .state("South California")
                                .zipCode(new ZipCode("12345"))
                                .complement(null)
                                .build()
                )
                );
    }

    @Test
    void given_archivedCustomer_whenTryToUpdate_shouldThrowException() {

        Customer customer = CustomerTestDataBuilder.existingAnonymizedCustomer().build();

        Assertions.assertThatExceptionOfType(CustomerArchivedException.class)
                .isThrownBy(customer::archive);

        Assertions.assertThatExceptionOfType(CustomerArchivedException.class)
                .isThrownBy(()-> customer.changeEmail(new Email("email@gmail.com")));

        Assertions.assertThatExceptionOfType(CustomerArchivedException.class)
                .isThrownBy(()-> customer.changePhone(new Phone("123-123-1111")));

        Assertions.assertThatExceptionOfType(CustomerArchivedException.class)
                .isThrownBy(customer::enablePromotionNotifications);

        Assertions.assertThatExceptionOfType(CustomerArchivedException.class)
                .isThrownBy(customer::disablePromotionNotifications);
    }

    @Test
    void given_newCustomer_whenAddNewPoints_shouldSumPoints() {

        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();

        customer.addLoyaltyPoints(new LoyaltyPoints(20));

        Assertions.assertWith(customer,
                c -> Assertions.assertThat(c.loyaltyPoints().point()).isEqualTo(20)
        );
    }

    @Test
    void given_newCustomer_whenAddInvalidPoints_shouldThrowException() {

        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();

        Assertions.assertThatExceptionOfType(LoyaltyValueException.class)
                .isThrownBy(() -> customer.addLoyaltyPoints(new LoyaltyPoints(0)));

        Assertions.assertThatExceptionOfType(LoyaltyValueException.class)
                .isThrownBy(() -> customer.addLoyaltyPoints(new LoyaltyPoints(-10)));
    }


}