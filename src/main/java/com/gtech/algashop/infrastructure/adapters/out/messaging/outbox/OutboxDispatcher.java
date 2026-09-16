package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// A metade ASSINCRONA da outbox: um polling publisher. A cada poll-interval (PT1M),
// UMA instancia (ShedLock jdbc, lockAtMostFor PT5M) le um batch elegivel e envia em
// SERIE, na ordem de insercao (id UUIDv7). As guardas do ciclo:
//   - deadline do batch (PT3M < lock PT5M): o ciclo termina antes do lock expirar,
//     para outra instancia nao comecar em cima de um ciclo vivo
//   - blockedAggregates: a 1a falha de um aggregate bloqueia as mensagens seguintes
//     DELE no ciclo - a ordem por aggregate nunca e trocada por throughput
//   - backoff (nextAttemptAt) e maxAttempts -> failedAt: mensagem que esgotou vira
//     caso de intervencao manual, e o findBatch para de enxerga-la (e ao aggregate)
// Envio confirmado -> DELETE da linha; falha -> registerFailure, cada um em sua
// propria transacao curta - o envio Kafka fica FORA de transacao de banco.

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.messaging.outbox.dispatcher.enabled", havingValue = "true")
@Slf4j
public class OutboxDispatcher {

    private final OutboxMessageRepository repository;
    private final OutboxKafkaSender sender;
    private final OutboxProperties properties;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(fixedDelayString = "${algashop.messaging.outbox.poll-interval}")
    @SchedulerLock(name = "outboxDispatcher", lockAtMostFor = "PT5M")
    public void dispatch() {
        OffsetDateTime deadLine = OffsetDateTime.now().plus(properties.getBatchDeadLine());

        Set<String> blockedAggregates = new HashSet<>();

        List<OutboxMessage> batch = repository.findBatch(OffsetDateTime.now(), PageRequest.of(0, properties.getBatchSize()));

        for (OutboxMessage message : batch) {
            if (OffsetDateTime.now().isAfter(deadLine)) {
                log.warn("Outbox batch deadline reached");
                break;
            }

            if (blockedAggregates.contains(message.getAggregateId())) {
                continue;
            }

            if (!isEligible(message)) {
                blockedAggregates.add(message.getAggregateId());
                continue;
            }

            try {
                sender.send(message);
                transactionTemplate.executeWithoutResult(_ -> repository.deleteMessage(message.getId()));
            } catch (Exception e) {
                transactionTemplate.executeWithoutResult(_ -> registerFailure(message, e));
                blockedAggregates.add(message.getAggregateId());
            }
        }
    }

    // RESILIENCIAS
    // segunda linha de defesa: o findBatch ja exclui aggregates com falha/backoff no SQL,
    // mas o estado pode mudar entre a query e o envio - a checagem em memoria fecha a janela
    private boolean isEligible(OutboxMessage message) {
        return message.getFailedAt() == null && !message.getNextAttemptAt().isAfter(OffsetDateTime.now());
    }

    private void registerFailure(OutboxMessage message, Exception e) {
        int attempts = message.getAttempts() + 1;
        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime failedAt = null;
        if (attempts >= properties.getMaxAttempts()) {
            failedAt = now;
        }

        // acrescimo de tentativas em tempo
        OffsetDateTime nextAttemptAt = now.plus(properties.getBackoff());

        String lastError = extractError(e);

        // registra o erro
        repository.registerFailure(message.getId(), attempts, nextAttemptAt, lastError, failedAt);

        if (failedAt != null) {
            log.error("Outbox message {} permanenty failed, requires manual intervetion", message.getId());
        }
    }

    private String extractError(Exception e) {
        String description;
        if (e.getCause() != null) {
            description = "Error: %s\n Cause: %s\n".formatted(e.getMessage(), e.getCause().getMessage());
        } else {
            description = "Error: %s\n".formatted(e.getMessage());
        }

        return description;
    }
}
