package com.gtech.algashop.application.order.management;

import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.OrderNotFoundException;
import com.gtech.algashop.domain.model.order.Orders;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderManagementApplicationService {

    // repositorio
    private final Orders orders;

    @Transactional
    public void markAsCanceled(String orderId) {
        Order order = getOrder(orderId);
        order.markAsCanceled();
        orders.add(order);
    }

    @Transactional
    public void markAsPaid(String orderId) {
        Order order = getOrder(orderId);
        order.markAsPaid();
        orders.add(order);
    }

    @Transactional
    public void markAsReady(String orderId) {
        Order order = getOrder(orderId);
        order.markAsReady();
        orders.add(order);
    }

    private Order getOrder(String orderId) {
        Objects.requireNonNull(orderId);
        return orders.ofId(new OrderId(orderId))
                .orElseThrow(OrderNotFoundException::new);
    }
}
