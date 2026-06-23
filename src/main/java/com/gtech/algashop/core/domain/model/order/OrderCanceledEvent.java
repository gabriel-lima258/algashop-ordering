package com.gtech.algashop.core.domain.model.order;

import com.gtech.algashop.core.domain.model.costumer.CustomerId;

import java.time.OffsetDateTime;

public record OrderCanceledEvent(OrderId orderId, CustomerId customerId, OffsetDateTime canceledAt) {
}
