package com.gtech.algashop.core.application.order.snapshot;

import com.gtech.algashop.core.domain.model.order.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

// Raiz do snapshot: espelho plano do agregado Order em tipos primitivos/JDK (sem value
// objects), pronto para virar JSON. Volta a viajar no comando de emissao de fatura da Saga.
public record OrderSnapshot(
        String orderId,
        UUID customerId,
        OffsetDateTime placedAt,
        OrderStatus status,
        BillingSnapshot billing,
        ShippingSnapshot shipping,
        String paymentMethod,
        UUID creditCardId,
        BigDecimal totalAmount,
        Integer totalItems,
        Set<OrderItemSnapshot> items
) {}
