package com.gtech.algashop.core.ports.out.order;

import com.gtech.algashop.core.ports.in.order.OrderFilter;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ForObtainingOrder {
    OrderDetailOutput findById(String orderId);
    Page<OrderSummaryOutput> filter(OrderFilter filter);
    OrderDetailOutput findByIdAndCustomerId(String orderId, UUID customerId);
}
