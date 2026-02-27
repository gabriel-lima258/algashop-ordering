package com.gtech.algashop.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * REPOSITÓRIO JPA DE BAIXO NÍVEL para a entidade de persistência OrderPersistenceEntity.
 *
 * --- ATENÇÃO: ESTE NÃO É O REPOSITÓRIO DO DOMÍNIO ---
 * Esta interface é um detalhe de implementação da camada de infraestrutura.
 * Ela trabalha com OrderPersistenceEntity (entidade JPA/banco), NÃO com Order (entidade de domínio).
 *
 * --- POR QUE DUAS ENTIDADES DIFERENTES? ---
 * Separar a entidade de domínio (Order) da entidade de persistência (OrderPersistenceEntity)
 * protege o domínio de anotações JPA como @Entity, @Column, @Table, etc.
 * O domínio fica limpo; a infraestrutura cuida do mapeamento objeto-relacional.
 *
 * --- QUEM USA ESTA INTERFACE? ---
 * Apenas o OrderPersistenceProvider (o adapter do domínio).
 * Nenhuma classe de domínio ou serviço de aplicação deve importar este repositório diretamente.
 */
public interface OrderJpaEntityRepository extends JpaRepository<OrderPersistenceEntity, Long> {


    @Query("""
        SELECT o
        FROM OrderPersistenceEntity o
        WHERE o.customer.id = :customerId
        AND YEAR(o.placedAt) = :year
    """)
    List<OrderPersistenceEntity> placedByCustomerInYear(
            @Param("customerId") UUID customerId,
            @Param("year") Integer year
    );

    @Query("""
        SELECT COUNT(o)
        FROM OrderPersistenceEntity o
        WHERE o.customer.id = :customerId
        AND YEAR(o.placedAt) = :year
        AND o.paidAt IS NOT NULL
        AND o.canceledAt IS NULL
    """)
    long salesQuantityByCustomerInYear(
            @Param("customerId") UUID customerId,
            @Param("year") Integer year
    );

    @Query("""
        SELECT COALESCE(SUM(o.totalAmount), 0)
        FROM OrderPersistenceEntity o
        WHERE o.customer.id = :customerId
        AND o.canceledAt IS NULL
        AND o.paidAt IS NOT NULL
    """)
    BigDecimal totalSoldForCustomer(@Param("customerId") UUID customerId);
}
