package com.gtech.algashop.core.application.order;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.ports.in.order.ForQueryOrders;
import com.gtech.algashop.core.ports.in.order.OrderFilter;
import com.gtech.algashop.core.ports.out.order.ForObtainingOrder;
import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.ports.out.order.OrderSummaryOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderQueryService implements ForQueryOrders {

    private final ForObtainingOrder forObtainingOrder;
    private final SecurityCheckApplicationService securityCheck;

    @Override
    public OrderDetailOutput findById(String orderId) {
        return forObtainingOrder.findById(orderId);
    }

    @Override
    public OrderDetailOutput findByIdAndCustomerId(String orderId, UUID customerId) {
        return forObtainingOrder.findByIdAndCustomerId(orderId, customerId);
    }

    @Override
    public Page<OrderSummaryOutput> filter(OrderFilter filter) {
        return forObtainingOrder.filter(filter);
    }
}
