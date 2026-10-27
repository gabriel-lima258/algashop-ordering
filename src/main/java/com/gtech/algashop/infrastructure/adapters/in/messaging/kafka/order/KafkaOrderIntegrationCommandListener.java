package com.gtech.algashop.infrastructure.adapters.in.messaging.kafka.order;

import com.gtech.algashop.core.application.checkout.command.ProcessAcceptedCheckoutIntegrationCommand;
import com.gtech.algashop.core.ports.in.checkout.ForProcessingCheckoutAccepted;
import com.gtech.algashop.core.ports.out.idempotency.ForGuardingIdempotency;
import com.gtech.algashop.infrastructure.config.kafka.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

// A metade CONSUMIDORA do listen to yourself: o proprio ordering consome os comandos
// que ele mesmo publicou (via outbox) em ordering.order.commands. O group.id vem do id
// do listener ("ordering.order-commands") - grupo proprio, offsets proprios, separado
// do listener de eventos de produto.
//
// O fluxo: comando chega -> guarda de idempotencia (runOnce: INSERT na PK de
// processed_message com a chave que veio no header idempotency-key - o ID da linha do
// outbox, estavel entre reenvios) -> ProcessAcceptedCheckout cria a Order na MESMA
// transacao. Redelivery = violacao de PK = skip. E o orders.exists() do service e a
// segunda guarda, para o caso do header faltar.
@Component
@Slf4j
@KafkaListener(
        id = "#{algaShopMessagingKafkaProperties.orderCommandsConsumerGroup}",
        concurrency = "3", // threads em paralelo para cada partição
        topics = "#{algaShopMessagingKafkaProperties.orderCommandTopicName}"
)
@RequiredArgsConstructor
public class KafkaOrderIntegrationCommandListener {

    // processamento do checkout aceito (a Order nasce la)
    private final ForProcessingCheckoutAccepted forProcessingCheckoutAccepted;

    // Idempotency
    private final ForGuardingIdempotency forGuardingIdempotency;


    @KafkaHandler
    public void handler(@Payload ProcessAcceptedCheckoutIntegrationCommand integrationCommand,
                        @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
                        @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
                        @Header(value = KafkaHeaders.OFFSET, required = false) Integer messageOffset,
                        @Header(value = KafkaConfig.IDEMPOTENCY_KEY_HEADER, required = false) byte[] rawIdempotencyKey
    ) {
        logReceived(integrationCommand, messageKey, partition, messageOffset);

        // header ausente nao pode virar retries + DLT por erro de binding: o
        // orders.exists() do service e a segunda guarda, entao processa sem dedupe
        if (rawIdempotencyKey == null) {
            log.warn("Missing idempotency-key header, processing without dedupe guard");
            forProcessingCheckoutAccepted.process(integrationCommand);
            return;
        }

        // chave de idempotencia e runnable do command
        UUID idempotencyKey = UUID.fromString(new String(rawIdempotencyKey));
        forGuardingIdempotency.runOnce(idempotencyKey, () -> forProcessingCheckoutAccepted.process(integrationCommand));
    }

    // Comando desconhecido NAO e ignorado - e a diferenca DELIBERADA para o listener de
    // eventos de produto: evento desconhecido de outro servico e evolucao normal do
    // produtor (ignora e segue); comando desconhecido NO PROPRIO topico da aplicacao e
    // anomalia. A IllegalArgumentException e nao-retryable no DefaultErrorHandler, entao
    // vai DIRETO para ordering.dlt.ordering.order.commands, sem retries inuteis.
    @KafkaHandler(isDefault = true)
    public void handle(@Payload Object integrationCommand,
                       @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
                       @Header(value = KafkaHeaders.OFFSET, required = false) String messageOffset
    ) {
        log.info("Unknown order command sent to DLT key={} | offset={} | thread={}",
                messageKey,
                messageOffset,
                Thread.currentThread().getName()
        );

        throw new IllegalArgumentException("Unsupported order command");

    }

    private void logReceived(Object event, String messageKey, Integer partition, Integer offset) {
        log.info("Received: {} | Key: {} | Partition: {} | Offset: {} | Thread: {}",
                event.getClass().getSimpleName(),
                messageKey,
                partition,
                offset,
                Thread.currentThread().getName()
        );
    }
}

