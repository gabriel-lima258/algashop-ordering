package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.costumer.CustomerId;

import java.time.OffsetDateTime;

public record OrderPaidEvent(OrderId orderId, CustomerId customerId,  OffsetDateTime paidAt) {
}
