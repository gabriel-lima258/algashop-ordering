package com.gtech.algashop.infrastructure.adapters.in.messaging.kafka.stock;

import com.gtech.algashop.core.application.stock.event.StockReservationConfirmedIntegrationEvent;
import com.gtech.algashop.core.ports.in.order.ForManagingOrders;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;


@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(
        id = "#{algaShopMessagingKafkaProperties.stockEventsConsumerGroup}",
        concurrency = "3", // threads em paralelo para cada partição
        topics = "#{algaShopMessagingKafkaProperties.stockEventTopicName}"
)
public class KafkaStockIntegrationEventListener {

    private final ForManagingOrders forManagingOrders;

    @KafkaHandler
    public void handle(
            @Payload @Valid StockReservationConfirmedIntegrationEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
            @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
            @Header(value = KafkaHeaders.OFFSET, required = false) Long offset) {
        logReceived(event, messageKey, partition, offset);
        forManagingOrders.markAsReady(event.getOrderId());
    }

    @KafkaHandler(isDefault = true)
    public void handle(
            @Payload Object event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
            @Header(value = KafkaHeaders.OFFSET, required = false) Long offset) {
        log.info("Event ignored: type={} key={} offset={}",
                event.getClass().getSimpleName(), messageKey, offset);
    }

    private void logReceived(Object event, String messageKey, Integer partition, Long offset) {
        log.info("Received {} | key={} | partition={} | offset={} | thread={}",
                event.getClass().getSimpleName(), messageKey, partition, offset,
                Thread.currentThread().getName());
    }
}
