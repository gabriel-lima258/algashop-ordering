package com.gtech.algashop.infrastructure.persistence.customer;

import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerPersistenceEntityDisassemblerTest {

    private final CustomerPersistenceEntityDisassembler disassembler = new CustomerPersistenceEntityDisassembler();

    @Test
    public void shouldConvertToDomain() {
        CustomerPersistenceEntity persistenceEntity = CustomerPersistenceEntityTestDataBuilder.existingCustomer().build();
        Customer domainEntity = disassembler.toDomainEntity(persistenceEntity);
        assertThat(domainEntity).satisfies(
                c -> assertThat(c.id()).isEqualTo(new CustomerId(persistenceEntity.getId())),
                c -> assertThat(c.fullName().firstName()).isEqualTo(persistenceEntity.getFirstName()),
                c -> assertThat(c.fullName().lastName()).isEqualTo(persistenceEntity.getLastName()),
                c -> assertThat(c.birthDate().birthDate()).isEqualTo(persistenceEntity.getBirthDate()),
                c -> assertThat(c.email().email()).isEqualTo(persistenceEntity.getEmail()),
                c -> assertThat(c.phone().phone()).isEqualTo(persistenceEntity.getPhone()),
                c -> assertThat(c.document().document()).isEqualTo(persistenceEntity.getDocument()),
                c -> assertThat(c.isPromotionNotificationsAllowed()).isEqualTo(persistenceEntity.getPromotionNotificationsAllowed()),
                c -> assertThat(c.isArchived()).isEqualTo(persistenceEntity.getArchived()),
                c -> assertThat(c.registeredAt()).isEqualTo(persistenceEntity.getRegisteredAt()),
                c -> assertThat(c.archivedAt()).isEqualTo(persistenceEntity.getArchivedAt()),
                c -> assertThat(c.loyaltyPoints().point()).isEqualTo(persistenceEntity.getLoyaltyPoints()),
                c -> assertThat(c.address().city()).isEqualTo(persistenceEntity.getAddress().getCity()),
                c -> assertThat(c.address().neighborhood()).isEqualTo(persistenceEntity.getAddress().getNeighborhood()),
                c -> assertThat(c.address().number()).isEqualTo(persistenceEntity.getAddress().getNumber()),
                c -> assertThat(c.address().street()).isEqualTo(persistenceEntity.getAddress().getStreet()),
                c -> assertThat(c.address().state()).isEqualTo(persistenceEntity.getAddress().getState()),
                c -> assertThat(c.address().zipCode().zipcode()).isEqualTo(persistenceEntity.getAddress().getZipCode()),
                c -> assertThat(c.address().complement()).isEqualTo(persistenceEntity.getAddress().getComplement()),
                c -> assertThat(c.version()).isEqualTo(persistenceEntity.getVersion())

        );
    }
}
