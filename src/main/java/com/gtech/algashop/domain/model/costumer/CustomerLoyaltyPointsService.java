package com.gtech.algashop.domain.model.service;

import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.costumer.CanAddLoyaltyPointsOrderIsNotReadyException;
import com.gtech.algashop.domain.model.order.OrderNotBelongsToCustomerException;
import com.gtech.algashop.domain.model.DomainService;

import java.util.Objects;

@DomainService
public class CustomerLoyaltyPointsService {

    private static final LoyaltyPoints basicPoints = new LoyaltyPoints(5);
    private static final Money expectedAmountToGivePoints = new Money("1000");

    public void addPoints(Customer customer, Order order) {
        Objects.requireNonNull(customer);
        Objects.requireNonNull(order);

        if (!customer.id().equals(order.customerId())) {
            throw new OrderNotBelongsToCustomerException();
        }

        if (!order.isReady()) {
            throw new CanAddLoyaltyPointsOrderIsNotReadyException();
        }

        customer.addLoyaltyPoints(calculatePoints(order));
    }

    private LoyaltyPoints calculatePoints(Order order) {
        if (shouldGivePointsByAmount(order.totalAmount())) {
            Money result = order.totalAmount().divide(expectedAmountToGivePoints);
            return new LoyaltyPoints(result.money().intValue() * basicPoints.point());
        }

        return LoyaltyPoints.ZERO;
    }

    private boolean shouldGivePointsByAmount(Money amount) {
        return amount.compareTo(expectedAmountToGivePoints) >= 0;
    }
}
