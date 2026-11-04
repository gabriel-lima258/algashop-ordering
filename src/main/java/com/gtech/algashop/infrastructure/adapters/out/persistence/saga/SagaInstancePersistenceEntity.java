package com.gtech.algashop.infrastructure.adapters.out.persistence.saga;

import com.gtech.algashop.core.application.saga.SagaStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

// Base de persistencia de TODA saga do servico. Abstrata porque nao existe "saga generica"
// gravada no banco: cada linha e de um tipo concreto (hoje so PLACE_ORDER).
//
// Heranca SINGLE_TABLE: todas as sagas dividem a tabela saga_instance, e a coluna
// saga_type (o discriminador) diz ao Hibernate qual subclasse instanciar ao ler a linha.
// Uma tabela so permite perguntar "quais sagas estao ativas?" sem join, qualquer que
// seja o tipo - e o que um job de recuperacao/timeout vai fazer.
//
// Divisao de responsabilidades:
//   aqui (comum a qualquer saga) -> identidade, agregado conduzido, status do ciclo de
//                                   vida, auditoria e @Version para lock otimista
//   na subclasse (especifico)    -> o passo, porque cada saga tem o seu enum de passos
//
// O aggregateId e String para servir a qualquer agregado (TSID do pedido hoje), e a
// constraint unique (saga_type, aggregate_id) da V010 garante uma saga por pedido.
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(of = { "sagaId", "status" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "saga_instance")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "saga_type", length = 50)
public abstract class SagaInstancePersistenceEntity {

    @Id
    @EqualsAndHashCode.Include
    private UUID sagaId;

    private String aggregateId;

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    @CreatedDate
    private OffsetDateTime createdAt;

    @LastModifiedDate
    private OffsetDateTime updatedAt;

    @Version
    private long version;
}
