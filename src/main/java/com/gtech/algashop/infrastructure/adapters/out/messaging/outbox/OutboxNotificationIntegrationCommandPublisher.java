package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import com.gtech.algashop.core.ports.out.notification.ForPublishingNotificationCommands;
import com.gtech.algashop.infrastructure.config.kafka.AlgaShopMessagingKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

// Adaptador da porta de notificacao: o SendEmail e gravado na outbox, na mesma transacao do
// checkout (o OutboxRecorder exige transacao em andamento). Pedido desfeito = comando desfeito;
// pedido confirmado = comando garantido, mesmo com Kafka ou notifications fora do ar.
// Chave = id do pedido (ordem junto com os outros registros do pedido). O OutboxKafkaSender poe
// o idempotency-key = id da mensagem da outbox. Sem correlationId nem replyTopic: nao ha resposta.
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.messaging.outbox.enabled", havingValue = "true")
public class OutboxNotificationIntegrationCommandPublisher implements ForPublishingNotificationCommands {

    private final OutboxRecorder recorder;
    private final AlgaShopMessagingKafkaProperties properties;

    @Override
    public void send(OutboundIntegrationCommand command) {
        recorder.record(OutboxDraft.builder()
                .aggregateId(command.getAggregateId())
                .payload(command)
                .channelName(properties.getNotificationsEmailCommandsTopicName())
                .build());
    }
}
