package com.gtech.algashop.core.application.stock.reply;

import com.gtech.algashop.core.application.InboundIntegrationReply;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockReservationConfirmedIntegrationReply
        implements InboundIntegrationReply {

    @NotNull
    private UUID reservationId;

    @NotBlank
    private String orderId;

    @NotNull
    private OffsetDateTime confirmedAt;
}

