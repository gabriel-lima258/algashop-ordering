package com.gtech.algashop.infrastructure.adapters.out.persistence.customer;

import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.commons.Email;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.costumer.Customers;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * ADAPTER de persistência para o repositório de domínio Customers.
 *
 * Implementa a interface Customers (contrato do domínio) usando JPA (detalhe de infraestrutura).
 * O domínio conhece apenas a interface — nunca esta implementação concreta.
 *
 * Fluxo (Arquitetura Hexagonal):
 *
 *   [Domínio/Serviço de Aplicação]
 *          ↓ usa a interface
 *       Customers (porta — domínio)
 *          ↓ injetado pelo Spring como
 *   CustomersPersistenceProvider (adapter — infraestrutura)
 *          ↓ usa internamente
 *   CustomerJpaEntityRepository (Spring Data JPA — infraestrutura)
 *          ↓ persiste no
 *       Banco de Dados
 */
@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class CustomersPersistenceProvider implements Customers {

    private final CustomerJpaEntityRepository persistenceRepository;
    private final CustomerPersistenceEntityAssembler assembler;
    private final CustomerPersistenceEntityDisassembler disassembler;
    private final EntityManager entityManager;

    /**
     * Busca o Aggregate Root Customer pelo seu CustomerId de domínio.
     *
     * 1. Converte CustomerId (tipo de domínio) → UUID (tipo do banco)
     * 2. Busca a CustomerPersistenceEntity no banco via JPA
     * 3. Converte CustomerPersistenceEntity → Customer (reconstrói o Aggregate)
     */
    @Override
    public Optional<Customer> ofId(CustomerId customerId) {
        return persistenceRepository.findById(customerId.value())
                .map(disassembler::toDomainEntity);
    }

    @Override
    public boolean exists(CustomerId customerId) {
        return persistenceRepository.existsById(customerId.value());
    }

    /**
     * Persiste o Aggregate Root Customer (INSERT ou UPDATE).
     *
     * Verifica se o customer já existe no banco:
     * - Se existir → UPDATE (merge na entidade JPA já gerenciada)
     * - Se não existir → INSERT (nova entidade de persistência)
     */
    @Override
    @Transactional(readOnly = false)
    public void add(Customer aggregateRoot) {
        persistenceRepository.findById(aggregateRoot.id().value())
                .ifPresentOrElse(
                        persistenceEntity -> update(aggregateRoot, persistenceEntity),
                        () -> insert(aggregateRoot)
                );

        // sempre que persiste o aggregate limpamos os eventos
        aggregateRoot.clearDomainEvents();
    }

    @Override
    public long count() {
        return persistenceRepository.count();
    }

    @Override
    public Optional<Customer> ofEmail(Email email) {
        // conversao de uma entidade persistencia para dominio
        return persistenceRepository.findByEmail(email.email())
                .map(disassembler::toDomainEntity);
    }

    // valida se o email não é unico!
    @Override
    public boolean isEmailUnique(Email email, CustomerId exceptCustomerId) {
        return !persistenceRepository.existsByEmailAndIdNot(email.email(), exceptCustomerId.value());
    }

    private void insert(Customer aggregateRoot) {
        CustomerPersistenceEntity persistenceEntity = assembler.fromDomain(aggregateRoot);
        persistenceRepository.saveAndFlush(persistenceEntity);
        updateVersion(aggregateRoot, persistenceEntity);
    }

    private void update(Customer aggregateRoot, CustomerPersistenceEntity persistenceEntity) {
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
    private void updateVersion(Customer aggregateRoot, CustomerPersistenceEntity persistenceEntity) {
        Field version = aggregateRoot.getClass().getDeclaredField("version");
        version.setAccessible(true);
        ReflectionUtils.setField(version, aggregateRoot, persistenceEntity.getVersion());
        version.setAccessible(false);
    }
}
