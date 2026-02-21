package com.gtech.algashop.domain.model.factory;

import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.PaymentMethod;
import com.gtech.algashop.domain.model.entity.VO.Billing;
import com.gtech.algashop.domain.model.entity.VO.Product;
import com.gtech.algashop.domain.model.entity.VO.Quantity;
import com.gtech.algashop.domain.model.entity.VO.Shipping;
import com.gtech.algashop.domain.model.entity.VO.id.CustomerId;

import java.util.Objects;

public class OrderFactory {

    private OrderFactory() {}

    public static Order filled(
            CustomerId customerId,
            Shipping shipping,
            Billing billing,
            PaymentMethod paymentMethod,
            Product product,
            Quantity productQuantity
    ) {
        Objects.requireNonNull(customerId);
        Objects.requireNonNull(shipping);
        Objects.requireNonNull(billing);
        Objects.requireNonNull(paymentMethod);
        Objects.requireNonNull(product);
        Objects.requireNonNull(productQuantity);

        Order order = Order.draft(customerId);

        order.changeBilling(billing);
        order.changeShipping(shipping);
        order.addItem(product, productQuantity);
        order.changePaymentMethod(paymentMethod);

        return order;
    }
}
