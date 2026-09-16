package com.gtech.algashop.infrastructure.adapters.out.persistence.idempotency;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.OffsetDateTime;

// A tabela de idempotencia cresce a cada mensagem processada e nunca e lida de volta -
// sem limpeza vira um log infinito. Este job apaga o que passou da retencao (10 dias -
// mais que a retention dos topicos: nenhuma redelivery legitima chega depois disso),
// em batches curtos (200 x 20, cada um na propria transacao) para nao segurar lock
// longo, servidos pelo indice em processed_at (V007). O ShedLock garante UMA instancia
// por vez - mesma coordenacao do OutboxDispatcher, mecanismo diferente do SKIP LOCKED
// dos jobs de billing: la as instancias DIVIDEM o trabalho, aqui uma exclui as outras.

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessedMessageCleaner {

    private static final int BATCH_SIZE = 200; // quantidades de mensagens coletadas
    private static final int MAX_BATCHES = 20; // quantidade de batches que carregam BATCH_SIZE
    private static final Duration RETENTION = Duration.ofDays(10); // contagem de dias de vida das mensagens no banco

    private final ProcessedMessageRepository repository;
    private final TransactionTemplate transactionTemplate;

    @Scheduled(cron = "0 */10 * * * *") // cada 10 minuto
    @SchedulerLock(name = "processedMessageCleanup", lockAtMostFor = "PT9M") // nome lock, expectativa de tempo
    public void purgeExpired() {
        OffsetDateTime threshold = OffsetDateTime.now().minus(RETENTION);
        log.info("Processed message cleanup started, threshold={}", threshold);

        int totalDeleted = 0;
        for (int i = 0; i < MAX_BATCHES; i++) {
            Integer batchDeleted = transactionTemplate.execute(_ ->
                        repository.deleteBatchOlderThan(threshold, BATCH_SIZE)
                    );
            totalDeleted += batchDeleted == null ? 0 : batchDeleted;

            // se não tiver nenhuma mensagem ou se for menor que 200 a serem apagadas interrompe varredura
            if (batchDeleted == null || batchDeleted < BATCH_SIZE) {
                break;
            }
        }

        log.info("Processed message cleanup finished, total deleted={}", totalDeleted);
    }
}
