package com.gtech.algashop.core.application.customer;

import com.gtech.algashop.core.domain.model.costumer.*;
import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderId;
import com.gtech.algashop.core.domain.model.order.OrderNotFoundException;
import com.gtech.algashop.core.domain.model.order.Orders;
import com.gtech.algashop.core.ports.in.customer.ForAddingLoyaltyPoints;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerLoyaltyPointsApplicationService implements ForAddingLoyaltyPoints {

    private final CustomerLoyaltyPointsService customerLoyaltyPointsService;

    // repositorios
    private final Customers customers;
    private final Orders orders;

    @Transactional
    @Override
    public void addLoyaltyPoints(UUID customerId, String orderId) {
        Objects.requireNonNull(customerId);
        Objects.requireNonNull(orderId);

        Customer customer = customers.ofId(new CustomerId(customerId))
                .orElseThrow(CustomerNotFoundException::new);

        Order order = orders.ofId(new OrderId(orderId))
                .orElseThrow(OrderNotFoundException::new);

        customerLoyaltyPointsService.addPoints(customer, order);
        customers.add(customer);
    }
}
