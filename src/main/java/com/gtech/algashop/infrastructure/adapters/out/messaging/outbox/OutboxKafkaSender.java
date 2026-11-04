package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import com.gtech.algashop.infrastructure.config.kafka.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// O braco Kafka do dispatcher: envia os BYTES gravados na outbox, sem reserializar
// (template dedicado com ByteArraySerializer - ver OutboxConfig). Reconstroi os
// headers a partir das colunas: __TypeId__ = messageType, idempotency-key = id da
// LINHA da outbox (estavel entre reenvios - e essa chave que o consumidor deduplica).
// Em comandos da saga vao tambem kafka_correlationId e kafka_replyTopic, que dizem ao
// participante a que pedido a resposta pertence e para onde envia-la; eventos nao os tem.
// O send e sincrono com get(send-timeout): o dispatcher precisa saber AGORA se deu
// certo, porque a decisao seguinte e deletar a linha ou registrar a falha.
@Component
@ConditionalOnProperty(name = "algashop.messaging.outbox.dispatcher.enabled", havingValue = "true")
@Slf4j
public class OutboxKafkaSender {

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final OutboxProperties outboxProperties;

    public OutboxKafkaSender(@Qualifier("outboxKafkaTemplate") KafkaTemplate<String, byte[]> kafkaTemplate,
                             OutboxProperties outboxProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.outboxProperties = outboxProperties;
    }

    public void send(OutboxMessage message) {

        ProducerRecord<String, byte[]> record = new ProducerRecord<>(
                message.getChannelName(),
                message.getAggregateId(),
                message.getPayload().getBytes(StandardCharsets.UTF_8)
        );

        record.headers().add(KafkaConfig.TYPE_ID_HEADER, message.getMessageType().getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaConfig.IDEMPOTENCY_KEY_HEADER, message.getId().toString().getBytes(StandardCharsets.UTF_8));

        if (message.getCorrelationId() != null) {
            record.headers().add(KafkaHeaders.CORRELATION_ID, message.getCorrelationId().getBytes(StandardCharsets.UTF_8));
        }

        if (message.getReplyTopic() != null) {
            record.headers().add(KafkaHeaders.REPLY_TOPIC, message.getReplyTopic().getBytes(StandardCharsets.UTF_8));
        }

        SendResult<String, byte[]> result = doSend(record);

        RecordMetadata metadata = result.getRecordMetadata();

        log.info("Published {} from outbox to {}-{} at offset {} | messageId={} aggregateId={}",
                message.getMessageType(),
                metadata.topic(),
                metadata.partition(),
                metadata.offset(),
                message.getId(),
                message.getAggregateId()
                );
    }

    private SendResult<String,byte[]> doSend(ProducerRecord<String,byte[]> record) {
        try {
            return kafkaTemplate.send(record).get(outboxProperties.getSendTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OutboxSendException("Interrupted while publishing", e);
        } catch (TimeoutException | KafkaException | ExecutionException e) {
            throw new OutboxSendException("Fail to publish", e);
        }
    }
}
