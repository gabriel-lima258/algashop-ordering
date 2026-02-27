package com.gtech.algashop.application.customer.management;

import com.gtech.algashop.domain.model.costumer.*;
import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.OrderNotFoundException;
import com.gtech.algashop.domain.model.order.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerLoyaltyPointsApplicationService {

    private final CustomerLoyaltyPointsService customerLoyaltyPointsService;

    // repositorios
    private final Customers customers;
    private final Orders orders;

    @Transactional
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
