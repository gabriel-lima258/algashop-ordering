package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import com.gtech.algashop.core.application.IntegrationCommand;
import com.gtech.algashop.core.ports.out.order.ForPublishingOrderIntegrationCommands;
import com.gtech.algashop.infrastructure.config.kafka.AlgaShopMessagingKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.messaging.outbox.enabled", havingValue = "true")
public class OutboxOrderIntegrationCommandPublisher implements ForPublishingOrderIntegrationCommands {

    private final OutboxRecorder recorder;
    private final AlgaShopMessagingKafkaProperties properties;

    @Override
    public void send(IntegrationCommand command) {
        recorder.record(properties.getOrderCommandTopicName(), command.getAggregateId(), command);
    }
}
