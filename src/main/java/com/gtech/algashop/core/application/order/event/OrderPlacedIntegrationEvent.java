package com.gtech.algashop.core.application.order.event;

import com.gtech.algashop.core.application.IntegrationEvent;
import com.gtech.algashop.core.domain.model.IdGenerator;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderPlacedIntegrationEvent implements IntegrationEvent {
    // @Builder.Default: sem ele o Lombok IGNORA o inicializador no builder e um
    // .builder().build() sairia com idempotencyKey nulo - evento sem header de dedupe,
    // em silencio (o publisher so grava o header quando a chave nao e nula)
    @Builder.Default
    private UUID idempotencyKey = IdGenerator.generateTimeBasedUUID();
    private String orderId;
    private UUID customerId;
    private OffsetDateTime placedAt;

    public OrderPlacedIntegrationEvent(String orderId, UUID customerId, OffsetDateTime placedAt) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.placedAt = placedAt;
    }

    @Override
    public String getAggregateId() {
        if (orderId == null) {
            return null;
        }

        return orderId;
    }

    @Override
    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }
}
