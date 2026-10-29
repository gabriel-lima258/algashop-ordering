package com.gtech.algashop.core.application.order.event;

import com.gtech.algashop.core.application.IntegrationEvent;
import com.gtech.algashop.core.application.order.snapshot.OrderItemSnapshot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderCanceledIntegrationEvent implements IntegrationEvent {

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