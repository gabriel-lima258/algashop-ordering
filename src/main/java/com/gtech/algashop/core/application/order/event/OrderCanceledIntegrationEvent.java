package com.gtech.algashop.core.application.order.event;

import com.gtech.algashop.core.application.OutboundIntegrationEvent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCanceledIntegrationEvent implements OutboundIntegrationEvent {

    @NotBlank
    private String orderId;

    @NotNull
    private UUID customerId;

    @NotNull
    private OffsetDateTime canceledAt;

    @Override
    public String getAggregateId() {
        return orderId;
    }
}