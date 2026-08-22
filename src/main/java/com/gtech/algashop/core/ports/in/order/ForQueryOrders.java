package com.gtech.algashop.core.ports.in.order;

import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.ports.out.order.OrderSummaryOutput;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ForQueryOrders {
    OrderDetailOutput findById(String orderId);
    OrderDetailOutput findByIdAndCustomerId(String orderId, UUID customerId);
    Page<OrderSummaryOutput> filter(OrderFilter filter);
}
