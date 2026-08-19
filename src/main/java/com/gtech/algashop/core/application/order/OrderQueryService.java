package com.gtech.algashop.core.application.order;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.ports.in.order.ForQueryOrders;
import com.gtech.algashop.core.ports.in.order.OrderFilter;
import com.gtech.algashop.core.ports.out.order.ForObtainingOrder;
import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.ports.out.order.OrderSummaryOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderQueryService implements ForQueryOrders {

    private final ForObtainingOrder forObtainingOrder;
    private final SecurityCheckApplicationService securityCheck;

    @Override
    public OrderDetailOutput findById(String orderId) {
        OrderDetailOutput order = forObtainingOrder.findById(orderId);
        if (!canAccess(order)) {
            throw new AccessDeniedException("You don't have permission to access this order");
        }
        return order;
    }

    @Override
    public Page<OrderSummaryOutput> filter(OrderFilter filter) {
        // regra de segurança, se for customer, ele só pode filtrar os seus pedidos
        // caso manager ou operator pode ver todos
        if (securityCheck.isCustomer()) {
            filter.setCustomerId(securityCheck.getAuthenticatedUserId());
        }
        return forObtainingOrder.filter(filter);
    }

    private boolean canAccess(OrderDetailOutput order) {
        // se for manager ou operator pode retornar
        if (!securityCheck.isCustomer() && securityCheck.isAuthenticated()) {
            return true;
        }

        return securityCheck.isCustomer() &&
                securityCheck.getAuthenticatedUserId().equals(order.getCustomer().getId());
    }
}
