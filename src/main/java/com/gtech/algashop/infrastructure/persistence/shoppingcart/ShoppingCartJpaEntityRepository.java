package com.gtech.algashop.infrastructure.persistence.shoppingcart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
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
public interface ShoppingCartJpaEntityRepository extends JpaRepository<ShoppingCartPersistenceEntity, UUID> {
    Optional<ShoppingCartPersistenceEntity> findByCustomer_Id(UUID customerId);

    @Modifying // obrigatorio quando um sql deve atualizar ou modificar estado
    @Transactional
    @Query("""
        UPDATE
            ShoppingCartItemPersistenceEntity i
        SET
            i.price = :price,
            i.totalAmount = :price * i.quantity
        WHERE
            i.productId = :productId
    """)
    void updateItemPrice(@Param("productId") UUID productId, @Param("price") BigDecimal price);

    @Modifying
    @Transactional
    @Query("""
        UPDATE
            ShoppingCartItemPersistenceEntity i
        SET
            i.available = :available
        WHERE
            i.productId = :productId
    """)
    void updateItemAvailability(@Param("productId") UUID productId, @Param("available") boolean available);

    /**
     * Recalcula o total (totalAmount) de todos os carrinhos que possuem o produto informado.
     *
     * Funcionamento:
     * - Para cada carrinho que contém pelo menos um item com o productId recebido,
     *   o total do carrinho é atualizado com a soma (SUM) do totalAmount de todos
     *   os seus itens.
     *
     * Motivação:
     * - Usado quando o preço de um produto é alterado.
     * - Evita carregar carrinhos e itens em memória.
     * - Executa o recálculo diretamente no banco de dados (mais performático).
     *
     * Observações:
     * - Apenas carrinhos que possuem o produto são afetados (cláusula EXISTS).
     * - A operação é feita em lote via JPQL UPDATE.
     * - @Modifying indica que não é um SELECT.
     * - @Transactional garante que o UPDATE seja executado em transação.
     */
    @Modifying
    @Transactional
    @Query("""
        UPDATE
            ShoppingCartPersistenceEntity sc
        SET
            sc.totalAmount = (
                SELECT SUM(i.totalAmount)
                FROM ShoppingCartItemPersistenceEntity i
                WHERE i.shoppingCart.id = sc.id)
        WHERE
            EXISTS (SELECT 1
                FROM ShoppingCartItemPersistenceEntity i2
                WHERE i2.shoppingCart.id = sc.id
                AND i2.productId = :productId)
    """)
    void recalculateTotalForCartWithProduct(@Param("productId") UUID productId);
}
