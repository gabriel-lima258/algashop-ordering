package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import com.gtech.algashop.core.domain.model.IdGenerator;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.type.PostgreSQLJsonPGObjectJsonType;
import org.springframework.data.domain.Persistable;

import java.time.OffsetDateTime;
import java.util.UUID;

// Uma linha da fila transacional: payload JSON pronto + os metadados que o dispatcher
// precisa (channelName = topico, aggregateId = key, messageType = __TypeId__) e o estado
// do ciclo de vida (attempts, nextAttemptAt, lastError, failedAt). O id e UUIDv7
// gerado na criacao: ordenavel por tempo, ele E a ordem de envio - e tambem a chave de
// idempotencia que viaja no header. Persistable.isNew()=true: so INSERT, nunca merge.
@Entity
@Table(name = "outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OutboxMessage implements Persistable<UUID> {

    @Id
    private UUID id;

    private String channelName;

    private String replyTopic;

    private String aggregateId;

    private String correlationId;

    private String messageType;

    @JdbcType(PostgreSQLJsonPGObjectJsonType.class)
    private String payload;

    private OffsetDateTime createdAt;

    private int attempts;

    private OffsetDateTime nextAttemptAt;

    private String lastError;

    private OffsetDateTime failedAt;

    @Builder
    public OutboxMessage(String channelName, String aggregateId, String messageType, String payload, String correlationId, String replyTopic) {
        this.id = IdGenerator.generateTimeBasedUUID();
        this.channelName = channelName;
        this.replyTopic = replyTopic;
        this.aggregateId = aggregateId;
        this.correlationId = correlationId;
        this.messageType = messageType;
        this.payload = payload;
        this.createdAt = OffsetDateTime.now();
        this.nextAttemptAt = OffsetDateTime.now();
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
