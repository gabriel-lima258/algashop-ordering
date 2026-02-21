package com.gtech.algashop.infrastructure.persistence.repository;

import com.gtech.algashop.infrastructure.persistence.entity.OrderPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
