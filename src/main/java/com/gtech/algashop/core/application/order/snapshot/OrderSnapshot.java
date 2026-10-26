package com.gtech.algashop.core.application.order.snapshot;

import com.gtech.algashop.core.domain.model.order.OrderStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.UUID;

// Raiz do snapshot: espelho plano do agregado Order em tipos primitivos/JDK (sem value
// objects), pronto para virar JSON no ProcessAcceptedCheckoutIntegrationCommand. O shoppingCartId
// nao pertence ao Order - viaja aqui para o consumidor saber qual carrinho esvaziar.
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
        Set<OrderItemSnapshot> items,
        UUID shoppingCartId
) {}
