package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.costumer.CustomerId;

import java.time.OffsetDateTime;

public record OrderReadyEvent(OrderId orderId, CustomerId customerId,  OffsetDateTime readyAt) {
}
