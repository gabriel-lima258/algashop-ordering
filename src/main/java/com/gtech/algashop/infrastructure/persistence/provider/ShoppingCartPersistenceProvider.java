package com.gtech.algashop.infrastructure.persistence.provider;

import com.gtech.algashop.domain.model.entity.Customer;
import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.ShoppingCart;
import com.gtech.algashop.domain.model.entity.VO.Money;
import com.gtech.algashop.domain.model.entity.VO.id.CustomerId;
import com.gtech.algashop.domain.model.entity.VO.id.OrderId;
import com.gtech.algashop.domain.model.entity.VO.id.ShoppingCartId;
import com.gtech.algashop.domain.model.repository.Orders;
import com.gtech.algashop.domain.model.repository.ShoppingCarts;
import com.gtech.algashop.infrastructure.persistence.assembler.OrderPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.assembler.ShoppingCartPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.disassembler.OrderPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.disassembler.ShoppingCartPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.entity.CustomerPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.entity.OrderPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.entity.ShoppingCartPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.repository.OrderJpaEntityRepository;
import com.gtech.algashop.infrastructure.persistence.repository.ShoppingCartJpaEntityRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartPersistenceProvider implements ShoppingCarts {

    // O repositório JPA de baixo nível — só este adapter sabe que ele existe
    private final ShoppingCartJpaEntityRepository persistenceRepository;
    private final ShoppingCartPersistenceEntityAssembler assembler;
    private final ShoppingCartPersistenceEntityDisassembler disassembler;
    private final EntityManager entityManager; // uso da entity para controlar o fluxo na persistencia


    @Override
    public Optional<ShoppingCart> ofCustomer(CustomerId customerId) {
        return persistenceRepository.findByCustomer_Id(customerId.value())
                .map(disassembler::toDomainEntity);
    }

    @Override
    @Transactional(readOnly = false)
    public void remove(ShoppingCart shoppingCart) {
        persistenceRepository.deleteById(shoppingCart.id().value());
    }

    @Override
    @Transactional(readOnly = false)
    public void remove(ShoppingCartId shoppingCartId) {
        persistenceRepository.deleteById(shoppingCartId.value());
    }

    @Override
    public Optional<ShoppingCart> ofId(ShoppingCartId shoppingCartId) {
        return persistenceRepository.findById(shoppingCartId.value())
                .map(disassembler::toDomainEntity);
    }

    @Override
    public boolean exists(ShoppingCartId shoppingCartId) {
        return persistenceRepository.existsById(shoppingCartId.value());
    }

    @Override
    @Transactional(readOnly = false)
    public void add(ShoppingCart aggregateRoot) {
        persistenceRepository.findById(aggregateRoot.id().value())
                .ifPresentOrElse(
                        persistenceEntity -> update(aggregateRoot, persistenceEntity),
                        () -> insert(aggregateRoot)
                );
    }

    @Override
    public long count() {
        return persistenceRepository.count();
    }


    private void insert(ShoppingCart aggregateRoot) {
        ShoppingCartPersistenceEntity persistenceEntity = assembler.fromDomain(aggregateRoot);
        persistenceRepository.saveAndFlush(persistenceEntity);
        updateVersion(aggregateRoot, persistenceEntity);
    }

    private void update(ShoppingCart aggregateRoot, ShoppingCartPersistenceEntity persistenceEntity) {
        persistenceEntity = assembler.merge(persistenceEntity, aggregateRoot);
        // Retira a responsabilidade do Hibernate de gerenciar a entidade automaticamente,
        // garantindo que o saveAndFlush execute um UPDATE explícito e controlado.
        entityManager.detach(persistenceEntity);
        persistenceEntity = persistenceRepository.saveAndFlush(persistenceEntity);
        updateVersion(aggregateRoot, persistenceEntity);
    }

    /**
     * Usa reflection para atualizar o campo version no Aggregate Root após a persistência.
     *
     * O campo version é private sem setter público — intencionalmente protegido para
     * evitar modificações externas indevidas. O valor correto vem do banco após o
     * @Version do JPA incrementá-lo, e precisamos sincronizar o objeto em memória.
     */
    @SneakyThrows
    private void updateVersion(ShoppingCart aggregateRoot, ShoppingCartPersistenceEntity persistenceEntity) {
        Field version = aggregateRoot.getClass().getDeclaredField("version");
        version.setAccessible(true);
        ReflectionUtils.setField(version, aggregateRoot, persistenceEntity.getVersion());
        version.setAccessible(false);
    }
}
