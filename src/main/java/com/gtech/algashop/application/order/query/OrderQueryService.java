package com.gtech.algashop.application.order.query;

import com.gtech.algashop.application.util.PageFilter;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface OrderQueryService {
    OrderDetailOutput findById(String orderId);
    Page<OrderSummaryOutput> filter(OrderFilter filter);
}
