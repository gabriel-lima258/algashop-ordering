package com.gtech.algashop.infrastructure.adapters.out.persistence.idempotency;

import com.gtech.algashop.core.ports.out.idempotency.ForGuardingIdempotency;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

// A guarda por INSERT-FIRST: grava a chave na PK de processed_message ANTES de rodar
// o work, NA MESMA transacao. Os dois desfechos sao atomicos:
//   - chave inedita  -> INSERT ok, work roda, commit unico (marca + efeito juntos)
//   - chave repetida -> violacao de PK -> rollback + false (nada executou)
// Marcar DEPOIS do work abriria a janela classica: efeito commitado, marca perdida,
// redelivery reprocessa. Marcar junto fecha a janela dos dois lados.
// saveAndFlush forca o INSERT ja (a violacao precisa estourar AQUI, nao no commit).
@Component
@RequiredArgsConstructor
@Slf4j
public class ForGuardingIdempotencyJpaImpl implements ForGuardingIdempotency {

    private final ProcessedMessageRepository repository;
    private final TransactionTemplate transactionTemplate; // controla transacao manualmente, evita o transactional annotation

    @Override
    public boolean runOnce(UUID idempotencyKey, Runnable work) {

        return Boolean.TRUE.equals(transactionTemplate.execute(status -> {
            try {
                repository.saveAndFlush(new ProcessedMessage(idempotencyKey));
            } catch (DataIntegrityViolationException e) {
                status.setRollbackOnly();
                log.warn("Duplicate execution skipped: idempotencyKey={}", idempotencyKey);
                return false;
            }
            work.run();
            return true;
        }));

    }
}
