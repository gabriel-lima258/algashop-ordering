package com.gtech.algashop.infrastructure.adapters.out.persistence.customer;

import com.gtech.algashop.core.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.adapters.out.persistence.AbstractPersistenceIT;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * TESTE DE INTEGRAÇÃO do repositório JPA de baixo nível.
 *
 * --- OBJETIVO DESTE TESTE ---
 * Verificar se a camada de persistência (mapeamento JPA + banco de dados) funciona corretamente.
 * Aqui testamos a entidade de infraestrutura CustomerPersistenceEntity, NÃO o Aggregate de domínio.
 *
 * --- @DataJpaTest ---
 * Sobe apenas o contexto JPA do Spring (EntityManager, DataSource, Spring Data repositories).
 * Não carrega controllers, services, ou beans da aplicação completa.
 * Por padrão, cada teste roda em uma transação que é revertida ao final (rollback automático),
 * garantindo isolamento entre testes sem sujar o banco.
 *
 * --- @AutoConfigureTestDatabase(replace = NONE) ---
 * Por padrão, @DataJpaTest substitui o banco de dados configurado por um H2 em memória.
 * "replace = NONE" desativa esse comportamento e usa o banco real configurado no application.properties.
 * Isso é importante para testar comportamentos específicos do banco (tipos de dados, constraints, etc.)
 * que podem diferir entre H2 e PostgreSQL/MySQL.
 */
class CustomerJpaEntityRepositoryIT extends AbstractPersistenceIT {

    private final CustomerJpaEntityRepository customerJpaRepository;

    @Autowired
    public CustomerJpaEntityRepositoryIT(CustomerJpaEntityRepository customerJpaRepository) {
        this.customerJpaRepository = customerJpaRepository;
    }

    @Test
    void shouldPersist() {
        CustomerPersistenceEntity entity = CustomerPersistenceEntityTestDataBuilder.existingCustomer().build();


        customerJpaRepository.saveAndFlush(entity);
        Assertions.assertThat(customerJpaRepository.existsById(entity.getId())).isTrue();

        CustomerPersistenceEntity savedEntity = customerJpaRepository.findById(entity.getId()).orElseThrow();
        Assertions.assertThat(savedEntity).isNotNull();
    }

    @Test
    void shouldCount() {
        long orderCount = customerJpaRepository.count();
        Assertions.assertThat(orderCount).isZero();
    }

    @Test
    void shouldSetAuditingValues() {
        CustomerPersistenceEntity entity = CustomerPersistenceEntityTestDataBuilder.existingCustomer().build();
        entity = customerJpaRepository.saveAndFlush(entity);

        Assertions.assertThat(entity.getCreatedByUserId()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedAt()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedByUserId()).isNotNull();
    }

}
