package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartItem;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartCantProceedToCheckoutException;
import com.gtech.algashop.domain.model.DomainService;

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
