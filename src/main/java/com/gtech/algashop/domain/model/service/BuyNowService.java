package com.gtech.algashop.domain.model.service;

import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.PaymentMethod;
import com.gtech.algashop.domain.model.entity.ShoppingCart;
import com.gtech.algashop.domain.model.entity.ShoppingCartItem;
import com.gtech.algashop.domain.model.entity.VO.Billing;
import com.gtech.algashop.domain.model.entity.VO.Product;
import com.gtech.algashop.domain.model.entity.VO.Quantity;
import com.gtech.algashop.domain.model.entity.VO.Shipping;
import com.gtech.algashop.domain.model.entity.VO.id.CustomerId;
import com.gtech.algashop.domain.model.exceptions.ShoppingCartCantProceedToCheckoutException;
import com.gtech.algashop.domain.model.util.DomainService;

import java.util.Set;

@DomainService
public class BuyNowService {

    public Order buyNow(Product product,
                        CustomerId customerId,
                        Billing billing,
                        Shipping shipping,
                        Quantity quantity,
                        PaymentMethod paymentMethod) {

        product.checkOutOfStock();

        Order order = Order.draft(customerId);
        order.changeBilling(billing);
        order.changeShipping(shipping);
        order.changePaymentMethod(paymentMethod);

        order.addItem(product, quantity);

        order.markAsPlaced();

        return order;
    }
}
