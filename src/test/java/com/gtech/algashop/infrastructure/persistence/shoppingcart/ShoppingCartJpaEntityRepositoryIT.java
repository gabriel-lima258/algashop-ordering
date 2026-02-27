package com.gtech.algashop.infrastructure.persistence.shoppingcart;

import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartPersistenceEntityTestDataBuilder;
import com.gtech.algashop.domain.model.IdGenerator;
import com.gtech.algashop.infrastructure.persistence.SpringDataAuditingConfig;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntity;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * TESTE DE INTEGRAÇÃO do repositório JPA de baixo nível para ShoppingCartPersistenceEntity.
 *
 * Verifica o correto funcionamento da camada de persistência:
 * mapeamento JPA, auditoria automática, buscas por customerId, e deleção.
 */
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Import(SpringDataAuditingConfig.class)
class ShoppingCartJpaEntityRepositoryIT {

    private final ShoppingCartJpaEntityRepository shoppingCartJpaEntityRepository;
    private final CustomerJpaEntityRepository customerJpaEntityRepository;

    private CustomerPersistenceEntity customerPersistenceEntity;

    @Autowired
    public ShoppingCartJpaEntityRepositoryIT(ShoppingCartJpaEntityRepository shoppingCartJpaEntityRepository,
                                             CustomerJpaEntityRepository customerJpaEntityRepository) {
        this.shoppingCartJpaEntityRepository = shoppingCartJpaEntityRepository;
        this.customerJpaEntityRepository = customerJpaEntityRepository;
    }

    @BeforeEach
    void setUp() {
        UUID customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID.value();
        if (!customerJpaEntityRepository.existsById(customerId)) {
            customerPersistenceEntity = customerJpaEntityRepository.saveAndFlush(
                    CustomerPersistenceEntityTestDataBuilder.existingCustomer().build()
            );
        } else {
            customerPersistenceEntity = customerJpaEntityRepository.findById(customerId).orElseThrow();
        }
    }

    @Test
    void shouldPersistCartWithItems() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart()
                .customer(customerPersistenceEntity)
                .build();

        shoppingCartJpaEntityRepository.saveAndFlush(entity);

        Assertions.assertThat(shoppingCartJpaEntityRepository.existsById(entity.getId())).isTrue();

        ShoppingCartPersistenceEntity saved = shoppingCartJpaEntityRepository.findById(entity.getId()).orElseThrow();
        Assertions.assertThat(saved.getItems()).isNotEmpty();
        Assertions.assertThat(saved.getItems()).hasSize(2);
    }

    @Test
    void shouldPersistCartWithMultipleItems() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart()
                .customer(customerPersistenceEntity)
                .items(Set.of(
                        ShoppingCartPersistenceEntityTestDataBuilder.existingItem().build(),
                        ShoppingCartPersistenceEntityTestDataBuilder.existingItemAlt().build(),
                        ShoppingCartItemPersistenceEntity.builder()
                                .id(IdGenerator.generateTSID().toLong())
                                .productId(IdGenerator.generateTimeBasedUUID())
                                .productName("16GB RAM Memory")
                                .price(new BigDecimal("200"))
                                .quantity(2)
                                .totalAmount(new BigDecimal("400"))
                                .available(true)
                                .build()
                ))
                .totalItems(4)
                .totalAmount(new BigDecimal("5020"))
                .build();

        shoppingCartJpaEntityRepository.saveAndFlush(entity);

        ShoppingCartPersistenceEntity saved = shoppingCartJpaEntityRepository.findById(entity.getId()).orElseThrow();
        Assertions.assertThat(saved.getItems()).hasSize(3);
        Assertions.assertThat(saved.getTotalItems()).isEqualTo(4);
    }

    @Test
    void shouldSetAuditingValues() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart()
                .customer(customerPersistenceEntity)
                .build();

        entity = shoppingCartJpaEntityRepository.saveAndFlush(entity);

        Assertions.assertThat(entity.getCreatedByUserId()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedAt()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedByUserId()).isNotNull();
    }

    @Test
    void shouldFillCreatedAtAndLastModifiedAtAfterSave() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart()
                .customer(customerPersistenceEntity)
                .createdAt(null)
                .build();

        entity = shoppingCartJpaEntityRepository.saveAndFlush(entity);

        // createdAt é preenchido pela anotação @CreatedDate da auditoria
        Assertions.assertThat(entity.getCreatedAt()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedAt()).isNotNull();
    }

    @Test
    void shouldCount() {
        long countBefore = shoppingCartJpaEntityRepository.count();
        Assertions.assertThat(countBefore).isZero();
    }

    @Test
    void shouldCountAfterPersisting() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart()
                .customer(customerPersistenceEntity)
                .build();
        shoppingCartJpaEntityRepository.saveAndFlush(entity);

        long count = shoppingCartJpaEntityRepository.count();
        Assertions.assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldFindByCustomerId() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart()
                .customer(customerPersistenceEntity)
                .build();
        shoppingCartJpaEntityRepository.saveAndFlush(entity);

        Optional<ShoppingCartPersistenceEntity> found = shoppingCartJpaEntityRepository
                .findByCustomer_Id(customerPersistenceEntity.getId());

        Assertions.assertThat(found).isPresent();
        Assertions.assertThat(found.get().getId()).isEqualTo(entity.getId());
        Assertions.assertThat(found.get().getCustomerId()).isEqualTo(customerPersistenceEntity.getId());
    }

    @Test
    void shouldFindByCustomerIdAndValidateItems() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart()
                .customer(customerPersistenceEntity)
                .build();
        shoppingCartJpaEntityRepository.saveAndFlush(entity);

        ShoppingCartPersistenceEntity found = shoppingCartJpaEntityRepository
                .findByCustomer_Id(customerPersistenceEntity.getId())
                .orElseThrow();

        Assertions.assertThat(found.getItems()).isNotEmpty();
        Assertions.assertThat(found.getItems()).allSatisfy(item -> {
            Assertions.assertThat(item.getProductId()).isNotNull();
            Assertions.assertThat(item.getProductName()).isNotBlank();
            Assertions.assertThat(item.getPrice()).isPositive();
            Assertions.assertThat(item.getQuantity()).isPositive();
            Assertions.assertThat(item.getTotalAmount()).isPositive();
        });
    }

    @Test
    void shouldNotFindByCustomerIdWhenNoCartExists() {
        Optional<ShoppingCartPersistenceEntity> found = shoppingCartJpaEntityRepository
                .findByCustomer_Id(customerPersistenceEntity.getId());

        Assertions.assertThat(found).isEmpty();
    }

    @Test
    void shouldDeleteCartAndNotFindAfterwards() {
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart()
                .customer(customerPersistenceEntity)
                .build();
        shoppingCartJpaEntityRepository.saveAndFlush(entity);
        UUID cartId = entity.getId();

        Assertions.assertThat(shoppingCartJpaEntityRepository.existsById(cartId)).isTrue();

        shoppingCartJpaEntityRepository.deleteById(cartId);
        shoppingCartJpaEntityRepository.flush();

        Assertions.assertThat(shoppingCartJpaEntityRepository.existsById(cartId)).isFalse();
    }

    @Test
    void shouldUpdateVersionOnSubsequentSave() {
        // Usa HashSet mutável para evitar UnsupportedOperationException do Set.of() no segundo saveAndFlush.
        // O @DataJpaTest executa tudo em uma única transação, então findById retorna a mesma instância
        // do cache L1 do Hibernate — que ainda tem o Set.of() imutável. Com HashSet vazio, o Hibernate
        // pode gerenciar a coleção sem problemas.
        ShoppingCartPersistenceEntity entity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart()
                .customer(customerPersistenceEntity)
                .items(new HashSet<>())
                .build();
        entity = shoppingCartJpaEntityRepository.saveAndFlush(entity);
        Long versionAfterInsert = entity.getVersion();

        entity.setTotalItems(entity.getTotalItems() + 1);
        entity = shoppingCartJpaEntityRepository.saveAndFlush(entity);
        Long versionAfterUpdate = entity.getVersion();

        Assertions.assertThat(versionAfterInsert).isNotNull();
        Assertions.assertThat(versionAfterUpdate).isGreaterThan(versionAfterInsert);
    }
}
