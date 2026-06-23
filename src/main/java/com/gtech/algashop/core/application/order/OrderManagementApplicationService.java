package com.gtech.algashop.core.application.order;

import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderId;
import com.gtech.algashop.core.domain.model.order.OrderNotFoundException;
import com.gtech.algashop.core.domain.model.order.Orders;
import com.gtech.algashop.core.ports.in.order.ForManagingOrders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderManagementApplicationService implements ForManagingOrders {

    // repositorio
    private final Orders orders;

    @Transactional
    @Override
    public void markAsCanceled(String orderId) {
        Order order = getOrder(orderId);
        order.markAsCanceled();
        orders.add(order);
    }

    @Transactional
    @Override
    public void markAsPaid(String orderId) {
        Order order = getOrder(orderId);
        order.markAsPaid();
        orders.add(order);
    }

    @Transactional
    @Override
    public void markAsReady(String orderId) {
        Order order = getOrder(orderId);
        order.markAsReady();
        orders.add(order);
    }

    private Order getOrder(String rawOrderId) {
        OrderId orderId = new OrderId(rawOrderId);
        return orders.ofId(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }

}
