package com.gtech.algashop.core.application.stock.event;

import com.gtech.algashop.core.application.IntegrationEvent;
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
public class StockReservationConfirmedIntegrationEvent implements IntegrationEvent {

    @NotNull
    private UUID reservationId;

    @NotBlank
    private String orderId;

    @NotNull
    private OffsetDateTime confirmedAt;

    @Override
    public String getAggregateId() {
        return orderId;
    }

}