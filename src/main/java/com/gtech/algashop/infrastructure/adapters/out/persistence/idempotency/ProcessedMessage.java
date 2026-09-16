package com.gtech.algashop.infrastructure.adapters.out.persistence.idempotency;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

// Registro de mensagem ja processada - a PK e a propria chave de idempotencia, e a
// UNICIDADE do banco e o mecanismo (nao ha SELECT-antes: duas threads inserindo a
// mesma chave, uma vence, a outra estoura PK). processed_at fica por conta do
// default now() do banco e so existe para a limpeza. Persistable.isNew()=true evita
// o SELECT previo do Hibernate: esta tabela so insere, nunca atualiza.

@Entity
@Table(name = "processed_message")
@NoArgsConstructor
public class ProcessedMessage implements Persistable<UUID> {

    @Id
    private UUID idempotencyKey;

    public ProcessedMessage(UUID idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    @Override
    public UUID getId() {
        return idempotencyKey;
    }

    @Override
    public boolean isNew() {
        return true; // essa tabela só insere, nunca atualiza
    }
}
