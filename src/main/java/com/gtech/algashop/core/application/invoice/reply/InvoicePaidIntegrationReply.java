package com.gtech.algashop.core.application.invoice.reply;

import com.gtech.algashop.core.application.InboundIntegrationReply;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

// Copia local da resposta do billing ao IssueInvoice: a fatura do pedido foi paga.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoicePaidIntegrationReply implements InboundIntegrationReply {

    @NotNull
    private UUID invoiceId;

    @NotBlank
    private String orderId;

    @NotNull
    private OffsetDateTime paidAt;
}
