package com.gtech.algashop.core.application.checkout.command;

import java.math.BigDecimal;
import java.util.UUID;

// Item do OrderSnapshot: produto, preco e quantidade ja fechados no checkout.
// O orderId nao vem aqui - e reinjetado pelo assembler a partir do snapshot pai.
public record OrderItemSnapshot(
        String orderItemId,
        UUID productId,
        String productName,
        BigDecimal price,
        Integer quantity,
        BigDecimal totalAmount
) {}
