package com.gtech.algashop.core.application.stock.command;

import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Segundo comando da saga: "catalogo, reserve estes itens". So produto e quantidade -
// e tudo que o estoque precisa. Key = orderId, como todo comando da saga.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReserveStockIntegrationCommand
        implements OutboundIntegrationCommand {

    @NotBlank
    private String orderId;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<Item> items = new ArrayList<>();

    public record Item(@NotNull UUID productId, @NotNull @Positive Integer quantity) {
    }

    @Override
    public String getAggregateId() {
        return orderId;
    }
}
