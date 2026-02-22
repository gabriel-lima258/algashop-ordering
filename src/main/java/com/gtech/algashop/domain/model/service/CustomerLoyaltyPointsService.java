package com.gtech.algashop.domain.model.service;

import com.gtech.algashop.domain.model.entity.Customer;
import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.VO.LoyaltyPoints;
import com.gtech.algashop.domain.model.entity.VO.Money;
import com.gtech.algashop.domain.model.exceptions.CanAddLoyaltyPointsOrderIsNotReadyException;
import com.gtech.algashop.domain.model.exceptions.OrderNotBelongsToCustomerException;

import java.util.Objects;

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
