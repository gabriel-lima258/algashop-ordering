package com.gtech.algashop.infrastructure.persistence.disassembler;

import com.gtech.algashop.domain.model.entity.Customer;
import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.OrderStatus;
import com.gtech.algashop.domain.model.entity.PaymentMethod;
import com.gtech.algashop.domain.model.entity.VO.Money;
import com.gtech.algashop.domain.model.entity.VO.Quantity;
import com.gtech.algashop.domain.model.entity.VO.id.CustomerId;
import com.gtech.algashop.domain.model.entity.VO.id.OrderId;
import com.gtech.algashop.domain.model.entity.factory.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.factory.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.domain.model.factory.OrderPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.entity.CustomerPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.entity.OrderPersistenceEntity;
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
