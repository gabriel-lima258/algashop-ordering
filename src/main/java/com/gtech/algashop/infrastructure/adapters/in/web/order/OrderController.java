package com.gtech.algashop.infrastructure.adapters.in.web.order;

import com.gtech.algashop.core.ports.in.order.ForQueryOrders;
import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.ports.in.order.OrderFilter;
import com.gtech.algashop.core.ports.out.order.OrderSummaryOutput;
import com.gtech.algashop.infrastructure.adapters.in.web.PageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.*;

// controller administrativo, apenas MANAGER e OPERATOR tem acesso

@RestController
@RequestMapping(path = "/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final ForQueryOrders forQueryOrders;

    @CanReadOrders
    @GetMapping
    public PageModel<OrderSummaryOutput> findAll(OrderFilter filter) {
        return PageModel.of(forQueryOrders.filter(filter));
    }

    @CanReadOrders
    @GetMapping("/{orderId}")
    public OrderDetailOutput findById(@PathVariable String orderId) {
        return forQueryOrders.findById(orderId);
    }

}
