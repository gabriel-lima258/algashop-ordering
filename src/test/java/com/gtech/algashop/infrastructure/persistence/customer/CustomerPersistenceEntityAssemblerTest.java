package com.gtech.algashop.infrastructure.persistence.customer;

import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerPersistenceEntityAssemblerTest {

    private CustomerPersistenceEntityAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new CustomerPersistenceEntityAssembler();
    }

    @Test
    void shouldMapAllFieldsFromNewCustomer() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();

        CustomerPersistenceEntity entity = assembler.fromDomain(customer);

        // CustomerId (TSID) → Long
        assertThat(entity.getId()).isEqualTo(customer.id().value());

        assertThat(entity.getFirstName()).isEqualTo(customer.fullName().firstName());
        assertThat(entity.getLastName()).isEqualTo(customer.fullName().lastName());
        assertThat(entity.getEmail()).isEqualTo(customer.email().email());
        assertThat(entity.getPhone()).isEqualTo(customer.phone().phone());
        assertThat(entity.getDocument()).isEqualTo(customer.document().document());
        assertThat(entity.getPromotionNotificationsAllowed()).isEqualTo(customer.isPromotionNotificationsAllowed());
        assertThat(entity.getLoyaltyPoints()).isEqualTo(customer.loyaltyPoints().point());
        assertThat(entity.getRegisteredAt()).isEqualTo(customer.registeredAt());
    }


    @Test
    void shouldMapBillingAddressEmbeddable() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();

        CustomerPersistenceEntity entity = assembler.fromDomain(customer);

        assertThat(entity.getAddress()).isNotNull();
        assertThat(entity.getAddress().getStreet()).isEqualTo(customer.address().street());
        assertThat(entity.getAddress().getNumber()).isEqualTo(customer.address().number());
        assertThat(entity.getAddress().getComplement()).isEqualTo(customer.address().complement());
        assertThat(entity.getAddress().getNeighborhood()).isEqualTo(customer.address().neighborhood());
        assertThat(entity.getAddress().getCity()).isEqualTo(customer.address().city());
        assertThat(entity.getAddress().getState()).isEqualTo(customer.address().state());
        assertThat(entity.getAddress().getZipCode()).isEqualTo(customer.address().zipCode().zipcode());
    }

}
