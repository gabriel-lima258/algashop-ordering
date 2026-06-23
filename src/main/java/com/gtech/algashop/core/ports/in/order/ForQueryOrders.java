package com.gtech.algashop.core.ports.in.order;

import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.ports.out.order.OrderSummaryOutput;
import org.springframework.data.domain.Page;

public interface ForQueryOrders {
    OrderDetailOutput findById(String orderId);
    Page<OrderSummaryOutput> filter(OrderFilter filter);
}
