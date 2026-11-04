package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import com.gtech.algashop.core.application.OutboundIntegrationEvent;
import com.gtech.algashop.core.ports.out.order.ForPublishingOrderIntegrationEvents;
import com.gtech.algashop.infrastructure.config.kafka.AlgaShopMessagingKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.messaging.outbox.enabled", havingValue = "true")
public class OutboxOrderIntegrationEventPublisher implements ForPublishingOrderIntegrationEvents {

    private final OutboxRecorder recorder;
    private final AlgaShopMessagingKafkaProperties properties;

    @Override
    public void send(OutboundIntegrationEvent event) {
        recorder.record(
                OutboxDraft.builder()
                        .channelName(properties.getOrderEventTopicName())
                        .aggregateId(event.getAggregateId())
                        .payload(event)
                        .build()
        );
    }
}
