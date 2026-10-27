package com.gtech.algashop.core.application.order.event;

import com.gtech.algashop.core.application.order.snapshot.OrderItemSnapshot;
import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Objects;

@Component
public class OrderPaidIntegrationEventAssembler {

    public OrderPaidIntegrationEvent toIntegrationEvent(Order order) {
        Objects.requireNonNull(order);

        return OrderPaidIntegrationEvent.builder()
                .orderId(order.id().toString())
                .customerId(order.customerId().value())
                .paidAt(order.paidAt())
                .items(order.items().stream()
                        .sorted(Comparator.comparing(item -> item.id().toString()))
                        .map(this::toOrderItemSnapshot)
                        .toList())
                .build();
    }

    private OrderItemSnapshot toOrderItemSnapshot(OrderItem item) {
        return new OrderItemSnapshot(
                item.id().toString(),
                item.orderId().toString(),
                item.productId().value(),
                item.productName().name(),
                item.price().money(),
                item.quantity().quantity(),
                item.totalAmount().money());
    }
}