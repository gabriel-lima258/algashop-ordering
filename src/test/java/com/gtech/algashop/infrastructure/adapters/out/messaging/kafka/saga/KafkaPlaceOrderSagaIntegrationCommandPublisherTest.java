package com.gtech.algashop.infrastructure.adapters.out.messaging.kafka.saga;

import com.gtech.algashop.core.application.CommandPublishingException;
import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import com.gtech.algashop.core.application.invoice.command.CancelInvoiceIntegrationCommand;
import com.gtech.algashop.core.application.invoice.command.IssueInvoiceIntegrationCommand;
import com.gtech.algashop.core.application.stock.command.ReserveStockIntegrationCommand;
import com.gtech.algashop.infrastructure.adapters.out.messaging.saga.PlaceOrderSagaCommandTopics;
import com.gtech.algashop.infrastructure.config.kafka.AlgaShopMessagingKafkaProperties;
import com.gtech.algashop.infrastructure.config.kafka.KafkaConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// Sem Spring, Kafka ou Postgres: o KafkaTemplate e um mock, e o roteador de topicos e o
// real, para o teste provar que este publicador decide o topico do mesmo jeito que o outbox
class KafkaPlaceOrderSagaIntegrationCommandPublisherTest {

    private static final String BILLING_COMMANDS_TOPIC = "billing.invoice.commands";
    private static final String STOCK_COMMANDS_TOPIC = "product-catalog.stock.commands";
    private static final String SAGA_REPLIES_TOPIC = "ordering.saga.replies";
    private static final String ORDER_ID = "0ABCDEF123456";
    private static final String CORRELATION_ID = UUID.randomUUID().toString();

    private KafkaTemplate<String, Object> kafkaTemplate;
    private KafkaPlaceOrderSagaIntegrationCommandPublisher publisher;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        kafkaTemplate = Mockito.mock(KafkaTemplate.class);

        AlgaShopMessagingKafkaProperties properties = new AlgaShopMessagingKafkaProperties();
        properties.setBillingInvoiceCommandsTopicName(BILLING_COMMANDS_TOPIC);
        properties.setProductCatalogCommandsTopicName(STOCK_COMMANDS_TOPIC);
        properties.setSagaRepliesTopicName(SAGA_REPLIES_TOPIC);

        publisher = new KafkaPlaceOrderSagaIntegrationCommandPublisher(
                kafkaTemplate, new PlaceOrderSagaCommandTopics(properties), properties);
    }

    @Test
    void shouldSendIssueInvoiceToBillingWithKeyAndCorrelationHeaders() {
        IssueInvoiceIntegrationCommand command = IssueInvoiceIntegrationCommand.builder()
                .orderId(ORDER_ID)
                .build();
        brokerConfirms();

        publisher.send(CORRELATION_ID, command);

        ProducerRecord<String, Object> record = sentRecord();
        Assertions.assertWith(record,
                r -> Assertions.assertThat(r.topic()).isEqualTo(BILLING_COMMANDS_TOPIC),
                r -> Assertions.assertThat(r.key()).isEqualTo(ORDER_ID),
                r -> Assertions.assertThat(r.value()).isSameAs(command),
                r -> Assertions.assertThat(headerValue(r, KafkaHeaders.CORRELATION_ID)).isEqualTo(CORRELATION_ID),
                r -> Assertions.assertThat(headerValue(r, KafkaHeaders.REPLY_TOPIC)).isEqualTo(SAGA_REPLIES_TOPIC),
                r -> Assertions.assertThat(r.headers().lastHeader(KafkaConfig.IDEMPOTENCY_KEY_HEADER)).isNull());
    }

    @Test
    void shouldSendCancelInvoiceToBilling() {
        brokerConfirms();

        publisher.send(CORRELATION_ID, new CancelInvoiceIntegrationCommand(ORDER_ID));

        ProducerRecord<String, Object> record = sentRecord();
        Assertions.assertThat(record.topic()).isEqualTo(BILLING_COMMANDS_TOPIC);
        Assertions.assertThat(record.key()).isEqualTo(ORDER_ID);
    }

    @Test
    void shouldSendReserveStockToProductCatalog() {
        brokerConfirms();
        ReserveStockIntegrationCommand command = new ReserveStockIntegrationCommand(ORDER_ID,
                List.of(new ReserveStockIntegrationCommand.Item(UUID.randomUUID(), 2)));

        publisher.send(CORRELATION_ID, command);

        ProducerRecord<String, Object> record = sentRecord();
        Assertions.assertThat(record.topic()).isEqualTo(STOCK_COMMANDS_TOPIC);
        Assertions.assertThat(record.key()).isEqualTo(ORDER_ID);
    }

    @Test
    void shouldThrowCommandPublishingExceptionWhenKafkaDoesNotConfirm() {
        Mockito.when(kafkaTemplate.send(Mockito.<ProducerRecord<String, Object>>any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("broker down")));

        Assertions.assertThatThrownBy(() -> publisher.send(CORRELATION_ID, new CancelInvoiceIntegrationCommand(ORDER_ID)))
                .isInstanceOf(CommandPublishingException.class);
    }

    @Test
    void shouldRejectUnknownCommandWithoutSendingAnything() {
        OutboundIntegrationCommand unknown = () -> ORDER_ID;

        Assertions.assertThatThrownBy(() -> publisher.send(CORRELATION_ID, unknown))
                .isInstanceOf(IllegalArgumentException.class);

        Mockito.verifyNoInteractions(kafkaTemplate);
    }

    // o Kafka confirma a gravacao: o future ja vem completo com o resultado
    private void brokerConfirms() {
        Mockito.when(kafkaTemplate.send(Mockito.<ProducerRecord<String, Object>>any()))
                .thenAnswer(invocation -> {
                    ProducerRecord<String, Object> record = invocation.getArgument(0);
                    RecordMetadata metadata = new RecordMetadata(
                            new TopicPartition(record.topic(), 0), 0L, 0, 0L, 0, 0);
                    return CompletableFuture.completedFuture(new SendResult<>(record, metadata));
                });
    }

    @SuppressWarnings("unchecked")
    private ProducerRecord<String, Object> sentRecord() {
        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        Mockito.verify(kafkaTemplate).send(captor.capture());
        return captor.getValue();
    }

    private String headerValue(ProducerRecord<String, Object> record, String name) {
        Header header = record.headers().lastHeader(name);
        return header == null ? null : new String(header.value(), StandardCharsets.UTF_8);
    }
}
