package com.gtech.algashop.infrastructure.persistence.repository;

import com.gtech.algashop.infrastructure.persistence.config.SpringDataAuditingConfig;
import com.gtech.algashop.infrastructure.persistence.entity.OrderPersistenceEntity;
import com.gtech.algashop.domain.model.factory.OrderPersistenceEntityTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * TESTE DE INTEGRAÇÃO do repositório JPA de baixo nível.
 *
 * --- OBJETIVO DESTE TESTE ---
 * Verificar se a camada de persistência (mapeamento JPA + banco de dados) funciona corretamente.
 * Aqui testamos a entidade de infraestrutura OrderPersistenceEntity, NÃO o Aggregate de domínio.
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
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Import(SpringDataAuditingConfig.class)
class OrderJpaEntityRepositoryIT {

    private final OrderJpaEntityRepository orderJpaEntityRepository;

    @Autowired
    public OrderJpaEntityRepositoryIT(OrderJpaEntityRepository orderJpaEntityRepository) {
        this.orderJpaEntityRepository = orderJpaEntityRepository;
    }

    @Test
    void shouldPersist() {
        OrderPersistenceEntity entity = OrderPersistenceEntityTestDataBuilder.existingOrder().build();


        orderJpaEntityRepository.saveAndFlush(entity);
        Assertions.assertThat(orderJpaEntityRepository.existsById(entity.getId())).isTrue();

        OrderPersistenceEntity savedEntity = orderJpaEntityRepository.findById(entity.getId()).orElseThrow();
        Assertions.assertThat(savedEntity.getItems()).isNotEmpty();
    }

    @Test
    void shouldCount() {
        long orderCount = orderJpaEntityRepository.count();
        Assertions.assertThat(orderCount).isZero();
    }

    @Test
    void shouldSetAuditingValues() {
        OrderPersistenceEntity entity = OrderPersistenceEntityTestDataBuilder.existingOrder().build();
        entity = orderJpaEntityRepository.saveAndFlush(entity);

        Assertions.assertThat(entity.getCreatedByUserId()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedAt()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedByUserId()).isNotNull();
    }

}
