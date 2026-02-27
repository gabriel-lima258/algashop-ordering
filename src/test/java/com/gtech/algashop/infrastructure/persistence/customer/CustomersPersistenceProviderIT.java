package com.gtech.algashop.infrastructure.persistence.provider;

import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.SpringDataAuditingConfig;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.customer.CustomersPersistenceProvider;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerJpaEntityRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import({
        CustomersPersistenceProvider.class,
        CustomerPersistenceEntityAssembler.class,
        CustomerPersistenceEntityDisassembler.class,
        SpringDataAuditingConfig.class
})
class CustomersPersistenceProviderIT {

    private final CustomersPersistenceProvider persistenceProvider;
    private final CustomerJpaEntityRepository entityRepository;

    @Autowired
    public CustomersPersistenceProviderIT(CustomersPersistenceProvider persistenceProvider,
                                          CustomerJpaEntityRepository entityRepository) {
        this.persistenceProvider = persistenceProvider;
        this.entityRepository = entityRepository;
    }

    @Test
    void shouldPersistNewCustomerWithAllFields() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        persistenceProvider.add(customer);

        CustomerPersistenceEntity entity = entityRepository.findById(customer.id().value()).orElseThrow();

        Assertions.assertThat(entity.getId()).isEqualTo(customer.id().value());
        Assertions.assertThat(entity.getFirstName()).isEqualTo("John");
        Assertions.assertThat(entity.getLastName()).isEqualTo("Doe");
        Assertions.assertThat(entity.getEmail()).isEqualTo("johndoe@email.com");
        Assertions.assertThat(entity.getPhone()).isEqualTo("478-256-2604");
        Assertions.assertThat(entity.getDocument()).isEqualTo("255-08-0578");
        Assertions.assertThat(entity.getPromotionNotificationsAllowed()).isTrue();
        Assertions.assertThat(entity.getArchived()).isFalse();
        Assertions.assertThat(entity.getLoyaltyPoints()).isEqualTo(0);
        Assertions.assertThat(entity.getRegisteredAt()).isNotNull();
        // auditoria preenchida pelo Spring Data
        Assertions.assertThat(entity.getCreatedByUserId()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedByUserId()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedAt()).isNotNull();
        // endereço embutido
        Assertions.assertThat(entity.getAddress()).isNotNull();
        Assertions.assertThat(entity.getAddress().getStreet()).isEqualTo("Bourbon Street");
        Assertions.assertThat(entity.getAddress().getZipCode()).isEqualTo("12345");
    }

    @Test
    void shouldReturnCustomerWithCorrectMappingOnFindById() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        persistenceProvider.add(customer);

        Customer found = persistenceProvider.ofId(customer.id()).orElseThrow();

        Assertions.assertThat(found.id()).isEqualTo(customer.id());
        Assertions.assertThat(found.fullName().firstName()).isEqualTo("John");
        Assertions.assertThat(found.fullName().lastName()).isEqualTo("Doe");
        Assertions.assertThat(found.email().email()).isEqualTo("johndoe@email.com");
        Assertions.assertThat(found.phone().phone()).isEqualTo("478-256-2604");
        Assertions.assertThat(found.document().document()).isEqualTo("255-08-0578");
        Assertions.assertThat(found.isPromotionNotificationsAllowed()).isTrue();
        Assertions.assertThat(found.isArchived()).isFalse();
        Assertions.assertThat(found.loyaltyPoints()).isEqualTo(LoyaltyPoints.ZERO);
        Assertions.assertThat(found.address().street()).isEqualTo("Bourbon Street");
        Assertions.assertThat(found.address().zipCode().zipcode()).isEqualTo("12345");
        Assertions.assertThat(found.version()).isNotNull();
    }

    @Test
    void shouldUpdateCustomerAndIncrementVersion() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        persistenceProvider.add(customer);
        Long versionAfterInsert = customer.version();

        customer = persistenceProvider.ofId(customer.id()).orElseThrow();
        customer.addLoyaltyPoints(new LoyaltyPoints(100));
        persistenceProvider.add(customer);

        CustomerPersistenceEntity entity = entityRepository.findById(customer.id().value()).orElseThrow();

        Assertions.assertThat(entity.getLoyaltyPoints()).isEqualTo(100);
        Assertions.assertThat(entity.getVersion()).isGreaterThan(versionAfterInsert);
        Assertions.assertThat(customer.version()).isEqualTo(entity.getVersion());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldThrowOnOptimisticLockConflict() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        persistenceProvider.add(customer);

        // Duas leituras independentes — ambas com a mesma version
        Customer firstRead = persistenceProvider.ofId(customer.id()).orElseThrow();
        Customer secondRead = persistenceProvider.ofId(customer.id()).orElseThrow();

        // Primeira atualização: bem-sucedida, incrementa a version no banco
        firstRead.addLoyaltyPoints(new LoyaltyPoints(50));
        persistenceProvider.add(firstRead);

        // Segunda atualização: falha porque a version do secondRead está desatualizada
        secondRead.addLoyaltyPoints(new LoyaltyPoints(200));
        Assertions.assertThatThrownBy(() -> persistenceProvider.add(secondRead))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void shouldReturnTrueWhenCustomerExists() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        persistenceProvider.add(customer);

        Assertions.assertThat(persistenceProvider.exists(customer.id())).isTrue();
    }

    @Test
    void shouldReturnFalseWhenCustomerDoesNotExist() {
        Customer nonExistent = CustomerTestDataBuilder.brandNewCustomer().build();

        Assertions.assertThat(persistenceProvider.exists(nonExistent.id())).isFalse();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldAddFindAndNotFailWhenNoTransaction() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        persistenceProvider.add(customer);

        Assertions.assertThatNoException().isThrownBy(
                () -> persistenceProvider.ofId(customer.id()).orElseThrow()
        );
    }
}
