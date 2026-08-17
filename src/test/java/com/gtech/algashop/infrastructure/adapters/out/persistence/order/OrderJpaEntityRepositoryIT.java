package com.gtech.algashop.infrastructure.adapters.out.persistence.order;

import com.gtech.algashop.core.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.core.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.core.domain.model.order.OrderPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.adapters.out.persistence.AbstractPersistenceIT;
import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerPersistenceEntity;
import com.gtech.algashop.utils.TestSecurityCheckConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

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
class OrderJpaEntityRepositoryIT extends AbstractPersistenceIT {

    private final OrderJpaEntityRepository orderJpaEntityRepository;
    private final CustomerJpaEntityRepository customerJpaEntityRepository;

    private CustomerPersistenceEntity customerPersistenceEntity;

    @Autowired
    public OrderJpaEntityRepositoryIT(OrderJpaEntityRepository orderJpaEntityRepository,
                                      CustomerJpaEntityRepository customerJpaEntityRepository) {
        this.orderJpaEntityRepository = orderJpaEntityRepository;
        this.customerJpaEntityRepository = customerJpaEntityRepository;
    }

    @BeforeEach
    void setUp() {
        UUID customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID.value();
        if (!customerJpaEntityRepository.existsById(customerId)) {
            customerPersistenceEntity = customerJpaEntityRepository.saveAndFlush(
                    CustomerPersistenceEntityTestDataBuilder.existingCustomer().build()
            );
        }
    }

    @Test
    void shouldPersist() {
        OrderPersistenceEntity entity = OrderPersistenceEntityTestDataBuilder.existingOrder()
                .customer(customerPersistenceEntity)
                .build();

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
        OrderPersistenceEntity entity = OrderPersistenceEntityTestDataBuilder.existingOrder()
                .customer(customerPersistenceEntity)
                .build();
        entity = orderJpaEntityRepository.saveAndFlush(entity);

        // O autor nao e mais "qualquer valor nao nulo": desde a Fase 25 ele vem do
        // SecurityCheckApplicationService, e aqui o stub responde um UUID conhecido.
        // Afirmar o valor exato e o que faz este teste cobrir a mudanca - com isNotNull()
        // ele passaria igual se a auditoria voltasse a gravar UUID.randomUUID().
        Assertions.assertThat(entity.getCreatedByUserId())
                .isEqualTo(TestSecurityCheckConfig.TEST_USER_ID);
        Assertions.assertThat(entity.getLastModifiedAt()).isNotNull();
        Assertions.assertThat(entity.getLastModifiedByUserId())
                .isEqualTo(TestSecurityCheckConfig.TEST_USER_ID);
    }

}
