package com.gtech.algashop.infrastructure.adapters.out.messaging.kafka.saga;

import com.gtech.algashop.core.application.CommandPublishingException;
import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import com.gtech.algashop.core.ports.out.order.saga.ForPublishingPlaceOrderSagaIntegrationCommands;
import com.gtech.algashop.infrastructure.adapters.out.messaging.saga.PlaceOrderSagaCommandTopics;
import com.gtech.algashop.infrastructure.config.kafka.AlgaShopMessagingKafkaProperties;
import com.gtech.algashop.infrastructure.config.kafka.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
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

// Envia os comandos da saga direto ao Kafka quando o outbox esta desligado (ou a
// propriedade nao existe) - o par do OutboxPlaceOrderSagaIntegrationCommandPublisher, para a
// porta ter sempre uma implementacao so. Mesmo contrato do outbox: topico pelo mesmo
// PlaceOrderSagaCommandTopics, key = orderId, kafka_correlationId e kafka_replyTopic.
// Bloqueante e sem transacao propria: roda dentro da transacao do coordenador, e uma falha
// de envio (CommandPublishingException) desfaz o pedido e a saga junto.
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.messaging.outbox.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class KafkaPlaceOrderSagaIntegrationCommandPublisher implements ForPublishingPlaceOrderSagaIntegrationCommands {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PlaceOrderSagaCommandTopics topics;
    private final AlgaShopMessagingKafkaProperties properties;

    @Override
    public void send(String correlationId, OutboundIntegrationCommand command) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                topics.of(command),
                command.getAggregateId(),
                command);

        record.headers().add(KafkaHeaders.CORRELATION_ID, correlationId.getBytes(StandardCharsets.UTF_8));
        record.headers().add(KafkaHeaders.REPLY_TOPIC,
                properties.getSagaRepliesTopicName().getBytes(StandardCharsets.UTF_8));

        if (command.getIdempotencyKey() != null) {
            record.headers().add(KafkaConfig.IDEMPOTENCY_KEY_HEADER,
                    command.getIdempotencyKey().toString().getBytes(StandardCharsets.UTF_8));
        }

        SendResult<String, Object> result;
        try {
            result = kafkaTemplate.send(record).get(40, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandPublishingException("Interrupted while publishing", command, e);
        } catch (TimeoutException | ExecutionException | KafkaException e) {
            throw new CommandPublishingException("Failed to publish", command, e);
        }

        RecordMetadata metadata = result.getRecordMetadata();
        log.info("Published {} to {}-{} at offset {}",
                command.getClass().getSimpleName(),
                metadata.topic(),
                metadata.partition(),
                metadata.offset());
    }
}
