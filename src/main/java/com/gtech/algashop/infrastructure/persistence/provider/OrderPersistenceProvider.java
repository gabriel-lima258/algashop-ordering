package com.gtech.algashop.infrastructure.persistence.provider;

import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.VO.id.OrderId;
import com.gtech.algashop.domain.model.repository.Orders;
import com.gtech.algashop.infrastructure.persistence.assembler.OrderPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.disassembler.OrderPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.entity.OrderPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.repository.OrderJpaEntityRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * ADAPTER de persistência para o repositório de domínio Orders.
 *
 * --- O QUE É ESTE ADAPTER? ---
 * Esta classe é a "ponte" entre o domínio e a infraestrutura.
 * Ela implementa a interface Orders (contrato do domínio) usando JPA (detalhe de infraestrutura).
 *
 * Fluxo completo (Arquitetura Hexagonal):
 *
 *   [Domínio/Serviço de Aplicação]
 *          ↓ usa a interface
 *       Orders (porta — domínio)
 *          ↓ injetado pelo Spring como
 *   OrderPersistenceProvider (adapter — infraestrutura)
 *          ↓ usa internamente
 *   OrderJpaEntityRepository (Spring Data JPA — infraestrutura)
 *          ↓ persiste no
 *       Banco de Dados
 *
 * --- POR QUE "implements Orders" É TÃO IMPORTANTE? ---
 * O domínio conhece apenas a interface Orders.
 * Em testes unitários podemos criar um FakeOrders (in-memory) sem banco.
 * Em produção, o Spring injeta automaticamente este OrderPersistenceProvider.
 * O domínio nunca sabe qual implementação está rodando — nem precisa saber.
 *
 * --- @Component E @AllArgsConstructor ---
 * @Component registra esta classe no contexto do Spring como um bean gerenciado.
 * @AllArgsConstructor faz o Lombok gerar um construtor com todos os campos,
 * permitindo que o Spring injete OrderJpaEntityRepository via construtor (injeção preferida).
 */
@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class OrderPersistenceProvider implements Orders {

    // O repositório JPA de baixo nível — só este adapter sabe que ele existe
    private final OrderJpaEntityRepository persistenceRepository;
    private final OrderPersistenceEntityAssembler assembler;
    private final OrderPersistenceEntityDisassembler disassembler;
    private final EntityManager entityManager; // uso da entity para controlar o fluxo na persistencia

    /**
     * Busca o Aggregate Root Order pelo seu OrderId de domínio.
     *
     * RESPONSABILIDADE DESTE MÉTODO:
     * 1. Converter OrderId (tipo de domínio) → Long (tipo do banco)
     * 2. Buscar a OrderPersistenceEntity no banco via JPA
     * 3. Converter OrderPersistenceEntity → Order (reconstruir o Aggregate de domínio)
     */
    @Override
    public Optional<Order> ofId(OrderId orderId) {
        Optional<OrderPersistenceEntity> possibleEntity = persistenceRepository.findById(
                orderId.value().toLong()
        );
        return possibleEntity.map(disassembler::toDomainEntity);
    }

    /**
     * Verifica se um pedido com este id já existe no banco.
     *
     * Implementação futura:
     *   return persistenceRepository.existsById(orderId.value().toLong());
     */
    @Override
    public boolean exists(OrderId orderId) {
        return persistenceRepository.existsById(orderId.value().toLong());
    }

    /**
     * Persiste um novo Aggregate Root Order no banco de dados.
     *
     * RESPONSABILIDADE DESTE MÉTODO (mapeamento domain → persistence):
     * 1. Extrair os dados do Aggregate Order (entidade de domínio)
     * 2. Construir uma OrderPersistenceEntity (entidade JPA)
     * 3. Salvar via JPA e garantir o flush imediato (saveAndFlush)
     *
     * O Aggregate Order não sabe nada sobre JPA — ele é puro Java.
     * Este método é o único lugar do sistema onde ocorre essa tradução.
     *
     * saveAndFlush garante que o SQL INSERT é enviado ao banco imediatamente,
     * útil para testes de integração que precisam verificar a persistência na mesma transação.
     */
    @Override
    @Transactional(readOnly = false)
    public void add(Order aggregateRoot) {
        long orderId = aggregateRoot.id().value().toLong();

        persistenceRepository.findById(orderId)
                .ifPresentOrElse(
                        persistenceEntity -> update(aggregateRoot, persistenceEntity),
                        () -> insert(aggregateRoot)
                );
    }

    /**
     * Retorna a contagem total de pedidos no banco.
     * <p>
     * Implementação futura:
     * return (int) persistenceRepository.count();
     */
    @Override
    public long count() {
        return persistenceRepository.count();
    }

    private void insert(Order aggregateRoot) {
        OrderPersistenceEntity persistenceEntity = assembler.fromDomain(aggregateRoot);
        persistenceRepository.saveAndFlush(persistenceEntity);
        updateVersion(aggregateRoot, persistenceEntity);
    }

    private void update(Order aggregateRoot, OrderPersistenceEntity persistenceEntity) {
        persistenceEntity = assembler.merge(persistenceEntity, aggregateRoot);
        // tira a responsabilidade do hibernate de gerenciar a entidade automaticamente
        entityManager.detach(persistenceEntity);
        persistenceEntity = persistenceRepository.saveAndFlush(persistenceEntity);
        updateVersion(aggregateRoot, persistenceEntity);
    }

    // usando o reflection para proteger a forma de acesso de version, prevenindo ações indevidas
    @SneakyThrows
    private void updateVersion(Order aggregateRoot, OrderPersistenceEntity persistenceEntity) {
        Field version = aggregateRoot.getClass().getDeclaredField("version");
        version.setAccessible(true);
        ReflectionUtils.setField(version, aggregateRoot, persistenceEntity.getVersion());
        version.setAccessible(false);
    }
}
