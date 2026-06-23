package com.gtech.algashop.core.application.order;

import com.gtech.algashop.core.ports.in.order.ForQueryOrders;
import com.gtech.algashop.core.ports.in.order.OrderFilter;
import com.gtech.algashop.core.ports.out.order.ForObtainingOrder;
import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.ports.out.order.OrderSummaryOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryService implements ForQueryOrders {

    private final ForObtainingOrder forObtainingOrder;

    @Override
    public OrderDetailOutput findById(String orderId) {
        return forObtainingOrder.findById(orderId);
    }

    @Override
    public Page<OrderSummaryOutput> filter(OrderFilter filter) {
        return forObtainingOrder.filter(filter);
    }
}
