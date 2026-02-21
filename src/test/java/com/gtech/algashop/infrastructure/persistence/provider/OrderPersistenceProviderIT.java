package com.gtech.algashop.infrastructure.persistence.provider;

import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.OrderStatus;
import com.gtech.algashop.domain.model.entity.factory.OrderTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.assembler.OrderPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.config.SpringDataAuditingConfig;
import com.gtech.algashop.infrastructure.persistence.disassembler.OrderPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.repository.OrderJpaEntityRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import({
        OrderPersistenceProvider.class,
        OrderPersistenceEntityDisassembler.class,
        OrderPersistenceEntityAssembler.class,
        SpringDataAuditingConfig.class
})
class OrderPersistenceProviderIT {

    private OrderPersistenceProvider persistenceProvider;
    private OrderJpaEntityRepository entityRepository;

    @Autowired
    public OrderPersistenceProviderIT(OrderPersistenceProvider persistenceProvider, OrderJpaEntityRepository entityRepository) {
        this.persistenceProvider = persistenceProvider;
        this.entityRepository = entityRepository;
    }

    @Test
    void shouldUpdateAndKeepPersistenceEntityState() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        long orderId = order.id().value().toLong();
        persistenceProvider.add(order);

        var persistenceEntity = entityRepository.findById(orderId).orElseThrow();

        Assertions.assertThat(persistenceEntity.getStatus()).isEqualTo(OrderStatus.PLACED.name());
        Assertions.assertThat(persistenceEntity.getCreatedByUserId()).isNotNull();
        Assertions.assertThat(persistenceEntity.getLastModifiedByUserId()).isNotNull();
        Assertions.assertThat(persistenceEntity.getLastModifiedAt()).isNotNull();

        order = persistenceProvider.ofId(order.id()).orElseThrow();
        order.markAsPaid();
        persistenceProvider.add(order);

        persistenceEntity = entityRepository.findById(orderId).orElseThrow();

        Assertions.assertThat(persistenceEntity.getStatus()).isEqualTo(OrderStatus.PAID.name());
        Assertions.assertThat(persistenceEntity.getCreatedByUserId()).isNotNull();
        Assertions.assertThat(persistenceEntity.getLastModifiedAt()).isNotNull();
        Assertions.assertThat(persistenceEntity.getLastModifiedByUserId()).isNotNull();
    }

    // Spring Data JPA abre uma transação interna automaticamente para métodos de escrita.
    // Isso evita o Lazy error e fechar a sessão sem o mesmo ter terminado
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldAddFindAndNotFailWhenNoTransaction() {
        Order order = OrderTestDataBuilder.anOrder().build();
        // faz uma transactional rapida commita e fecha
        persistenceProvider.add(order);

        // não faz uma transactional
        Assertions.assertThatNoException().isThrownBy(
                () -> persistenceProvider.ofId(order.id()).orElseThrow()
        );
    }
}