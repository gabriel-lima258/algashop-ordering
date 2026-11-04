package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import com.gtech.algashop.core.ports.out.order.saga.ForPublishingPlaceOrderSagaIntegrationCommands;
import com.gtech.algashop.infrastructure.adapters.out.messaging.saga.PlaceOrderSagaCommandTopics;
import com.gtech.algashop.infrastructure.config.kafka.AlgaShopMessagingKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Adaptador da porta de comandos: o comando sai pela outbox, gravado na mesma transacao
// que mudou a saga (sem dual-write). Diferente do evento, o draft leva correlationId e
// replyTopic, que o OutboxKafkaSender transforma em kafka_correlationId e kafka_replyTopic.
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.messaging.outbox.enabled", havingValue = "true")
public class OutboxPlaceOrderSagaIntegrationCommandPublisher implements ForPublishingPlaceOrderSagaIntegrationCommands {

    private final OutboxRecorder recorder;
    private final PlaceOrderSagaCommandTopics topics;
    private final AlgaShopMessagingKafkaProperties properties;

    @Override
    public void send(String correlationId, OutboundIntegrationCommand command) {
        recorder.record(
                OutboxDraft.builder()
                        .channelName(topics.of(command))
                        .aggregateId(command.getAggregateId())
                        .correlationId(correlationId)
                        .replyTopic(properties.getSagaRepliesTopicName())
                        .payload(command)
                        .build()
        );
    }
}
