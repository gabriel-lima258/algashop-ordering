package com.gtech.algashop.domain.model.service;

import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.PaymentMethod;
import com.gtech.algashop.domain.model.entity.ShoppingCart;
import com.gtech.algashop.domain.model.entity.ShoppingCartItem;
import com.gtech.algashop.domain.model.entity.VO.Billing;
import com.gtech.algashop.domain.model.entity.VO.Product;
import com.gtech.algashop.domain.model.entity.VO.Shipping;
import com.gtech.algashop.domain.model.exceptions.ShoppingCartCantProceedToCheckoutException;
import com.gtech.algashop.domain.model.util.DomainService;

import java.util.Set;

@DomainService
public class CheckoutService {

    public Order checkout(ShoppingCart shoppingCart, Billing billing,
                          Shipping shipping, PaymentMethod paymentMethod) {
        if (shoppingCart.containsUnavailableItems()) {
            throw new ShoppingCartCantProceedToCheckoutException();
        }

        if (shoppingCart.isEmpty()) {
            throw new ShoppingCartCantProceedToCheckoutException();
        }

        Set<ShoppingCartItem> items = shoppingCart.items();

        Order order = Order.draft(shoppingCart.customerId());
        order.changeBilling(billing);
        order.changeShipping(shipping);
        order.changePaymentMethod(paymentMethod);

        for (ShoppingCartItem item: items) {
            order.addItem(new Product(
                    item.productId(),
                    item.name(),
                    item.price(),
                    item.available()),
                    item.quantity()
            );
        }

        order.markAsPlaced();
        shoppingCart.empty();

        return order;
    }
}
