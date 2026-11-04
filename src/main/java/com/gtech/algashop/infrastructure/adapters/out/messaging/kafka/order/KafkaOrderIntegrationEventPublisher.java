package com.gtech.algashop.infrastructure.adapters.out.messaging.kafka.order;

import com.gtech.algashop.core.application.EventPublishingException;
import com.gtech.algashop.core.application.OutboundIntegrationEvent;
import com.gtech.algashop.core.ports.out.order.ForPublishingOrderIntegrationEvents;
import com.gtech.algashop.infrastructure.config.kafka.AlgaShopMessagingKafkaProperties;
import com.gtech.algashop.infrastructure.config.kafka.KafkaConfig;
import com.gtech.algashop.infrastructure.config.util.BeanValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.messaging.outbox.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class KafkaOrderIntegrationEventPublisher implements ForPublishingOrderIntegrationEvents {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AlgaShopMessagingKafkaProperties properties;
    private final BeanValidationUtil beanValidationUtil;

    @Override
    public void send(OutboundIntegrationEvent event) {
        beanValidationUtil.validate(event);
        SendResult<String, Object> result = null;
        try {
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                    properties.getOrderEventTopicName(),
                    event.getAggregateId(),
                    event);

            if (event.getIdempotencyKey() != null) {
                // publica a chave de idempotencia no header
                record.headers().add(KafkaConfig.IDEMPOTENCY_KEY_HEADER, event.getIdempotencyKey().toString().getBytes());
            }

            result = kafkaTemplate.send(record).get(40, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new EventPublishingException("Interrupted while publishing", event, e);
        } catch (TimeoutException | ExecutionException | KafkaException e) {
            throw new EventPublishingException("Failed to publish", event, e);
        }

        RecordMetadata metadata = result.getRecordMetadata();

        log.info("Published to {} {}-{} at offset {}",
                event.getClass().getSimpleName(),
                metadata.topic(),
                metadata.partition(),
                metadata.offset()
        );
    }
}
