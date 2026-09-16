package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import com.gtech.algashop.infrastructure.config.kafka.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

// A metade TRANSACIONAL da outbox: em vez de mandar ao Kafka, grava a mensagem na
// tabela outbox - e o Propagation.MANDATORY e o contrato: este metodo SO roda dentro
// de uma transacao ja aberta (a do checkout). Sem transacao ativa, estoura
// IllegalTransactionStateException na hora, em vez de gravar um outbox orfao.
// Pedido e mensagem entram no MESMO commit - o dual-write morre aqui.
// O payload ja sai SERIALIZADO (mesmo JacksonJsonSerializer do caminho Kafka, mesmo
// __TypeId__ logico): o dispatcher depois so transporta bytes, sem reserializar.

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.messaging.outbox.enabled", havingValue = "true")
@Slf4j
public class OutboxRecorder {

    private final OutboxMessageRepository repository;
    private final JacksonJsonSerializer<Object> outboxJsonSerializer;

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String channelName, String aggregateId, Object message) {
        RecordHeaders headers = new RecordHeaders();
        byte[] payload = outboxJsonSerializer.serialize(channelName, headers, message);

        // extrai o typeId do headers
        String eventType = readEventType(headers, message);

        // cria o construtor de message
        OutboxMessage outboxMessage = OutboxMessage.builder()
                .channelName(channelName)
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(new String(payload, StandardCharsets.UTF_8))
                .build();

        // salva message outbox
        repository.save(outboxMessage);

        log.info("Recorder {} on outbox: channel={} aggregateId={} id={}",
                message.getClass().getSimpleName(),
                channelName,
                aggregateId,
                outboxMessage.getId()
        );
    }

    private String readEventType(RecordHeaders headers, Object message) {
        Header typeId = headers.lastHeader(KafkaConfig.TYPE_ID_HEADER);

        if (typeId == null) {
            return message.getClass().getSimpleName();
        }

        return new String(typeId.value(), StandardCharsets.UTF_8);
    }
}
