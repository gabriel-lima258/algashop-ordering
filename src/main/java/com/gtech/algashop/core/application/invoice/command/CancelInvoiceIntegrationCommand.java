package com.gtech.algashop.core.application.invoice.command;

import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CancelInvoiceIntegrationCommand implements OutboundIntegrationCommand {
    @NotBlank
    private String orderId;

    @Override
    public String getAggregateId() {
        return orderId;
    }
}