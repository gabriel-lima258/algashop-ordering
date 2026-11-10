package com.gtech.algashop.infrastructure.adapters.out.messaging.kafka.notification;

import com.gtech.algashop.core.application.CommandPublishingException;
import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import com.gtech.algashop.core.ports.out.notification.ForPublishingNotificationCommands;
import com.gtech.algashop.infrastructure.config.kafka.AlgaShopMessagingKafkaProperties;
import com.gtech.algashop.infrastructure.config.kafka.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Envia os comandos do notifications direto ao Kafka quando o outbox esta desligado (ou a
// propriedade nao existe) - o par do OutboxNotificationIntegrationCommandPublisher, para a
// porta ter sempre uma implementacao so (sem ele o ordering nem sobe com o outbox desligado).
// Mesmo contrato do outbox: key = id do pedido e __TypeId__ pelo spring.json.type.mapping.
// Sem correlationId nem replyTopic: o notifications nao responde.
// Diferenca que importa: sem outbox nao ha id de mensagem, entao o idempotency-key so vai se o
// comando trouxer um; e o envio nao e atomico com o pedido (dual-write). Bloqueante: uma falha
// (CommandPublishingException) desfaz o checkout - modo de teste, nao de producao.
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.messaging.outbox.enabled", havingValue = "false", matchIfMissing = true)
@Slf4j
public class KafkaNotificationIntegrationCommandPublisher implements ForPublishingNotificationCommands {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final AlgaShopMessagingKafkaProperties properties;

    @Override
    public void send(OutboundIntegrationCommand command) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                properties.getNotificationsEmailCommandsTopicName(),
                command.getAggregateId(),
                command);

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
