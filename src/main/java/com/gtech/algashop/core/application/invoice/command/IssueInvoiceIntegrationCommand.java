package com.gtech.algashop.core.application.invoice.command;

import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import com.gtech.algashop.core.application.order.snapshot.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

// Primeiro comando da saga: "billing, emita a fatura deste pedido". Comando, e nao
// evento: tem um destinatario (o billing) e espera uma resposta (fatura paga ou cancelada).
// Carrega o pedido inteiro congelado (ECST): itens, frete, cobranca e forma de pagamento.
// Assim o billing fatura sem chamar o ordering de volta. Key = orderId.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IssueInvoiceIntegrationCommand implements OutboundIntegrationCommand {

    @NotBlank
    private String orderId;

    @NotNull
    private UUID customerId;

    @NotNull
    private OffsetDateTime placedAt;

    @NotNull
    @Positive
    private BigDecimal totalAmount;

    @Builder.Default
    @NotNull
    @Size(min = 1)
    @Valid
    private List<OrderItemSnapshot> items = new ArrayList<>();

    @NotNull
    @Valid
    private PaymentSnapshot payment;

    @NotNull
    @Valid
    private ShippingSnapshot shipping;

    @NotNull
    @Valid
    private BillingSnapshot billing;

    // monta o comando a partir do snapshot do pedido; itens ordenados por id deixam o
    // JSON deterministico (o mesmo pedido gera sempre o mesmo payload)
    public static IssueInvoiceIntegrationCommand from(OrderSnapshot order) {
        return IssueInvoiceIntegrationCommand.builder()
                .orderId(order.orderId())
                .customerId(order.customerId())
                .placedAt(order.placedAt())
                .totalAmount(order.totalAmount())
                .items(order.items().stream()
                        .sorted(Comparator.comparing(OrderItemSnapshot::id))
                        .toList())
                .payment(new PaymentSnapshot(order.paymentMethod(), order.creditCardId()))
                .shipping(order.shipping())
                .billing(order.billing())
                .build();
    }

    @Override
    public String getAggregateId() {
        return orderId;
    }
}