package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxMessageRepository extends JpaRepository<OutboxMessage, UUID> {

    // O filtro e no nivel do AGGREGATE, nao da linha: excluir so a linha falha/em backoff
    // deixaria a mensagem seguinte do MESMO aggregate passar na frente, quebrando a ordem
    // que o dispatcher existe para preservar. Como o envio e serial por id (UUIDv7), a
    // mensagem inelegivel de um aggregate e sempre a mais antiga dele - bloquear o
    // aggregate inteiro equivale as guardas em memoria, mas sem deixar linhas mortas
    // ocupando o batch: sem este WHERE, mensagens com failedAt (nunca deletadas e as
    // mais antigas do ORDER BY id) monopolizariam o findBatch e o outbox pararia de
    // enviar mensagens novas - starvation.
    @Query("""
            SELECT m FROM OutboxMessage m
            WHERE m.aggregateId NOT IN (
                SELECT b.aggregateId FROM OutboxMessage b
                WHERE b.failedAt IS NOT NULL OR b.nextAttemptAt > :now)
            ORDER BY m.id
            """)
    List<OutboxMessage> findBatch(@Param("now") OffsetDateTime now, Pageable pageable);

    @Modifying
    @Query("DELETE FROM OutboxMessage m WHERE m.id = :id")
    void deleteMessage(UUID id);

    @Modifying
    @Query("""
            UPDATE OutboxMessage m
               SET m.attempts = :attempts,
                   m.nextAttemptAt = :nextAttemptAt,
                   m.lastError = :lastError,
                   m.failedAt = :failedAt
            WHERE m.id = :id
            """)
    int registerFailure(
            @Param("id") UUID id,
            @Param("attempts") int attempts,
            @Param("nextAttemptAt") OffsetDateTime nextAttemptAt,
            @Param("lastError") String lastError,
            @Param("failedAt") OffsetDateTime failedAt
    );
}
