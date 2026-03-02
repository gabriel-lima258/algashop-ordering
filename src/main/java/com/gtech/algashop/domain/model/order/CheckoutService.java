package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartItem;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartCantProceedToCheckoutException;
import com.gtech.algashop.domain.model.DomainService;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@DomainService
@RequiredArgsConstructor
public class CheckoutService {

    private final CustomerHaveFreeShippingSpecification customerHaveFreeShippingSpecification;

    public Order checkout(Customer customer,
                          ShoppingCart shoppingCart,
                          Billing billing,
                          Shipping shipping,
                          PaymentMethod paymentMethod) {
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

        // regra para ver se possui frete gratis ou não
        if (haveFreeShipping(customer)) {
            Shipping freeShipping = shipping.toBuilder().cost(Money.ZERO).build();
            order.changeShipping(freeShipping);
        } else {
            order.changeShipping(shipping);
        }

        order.markAsPlaced();
        shoppingCart.empty();

        return order;
    }

    /*
     * A função haveFreeShipping determina se um cliente tem direito a frete grátis com base nos seus pontos de fidelidade
     * e histórico de compras. A lógica é:
     * Condição 1 (AND): Cliente tem ≥ 100 pontos de fidelidade E fez ≥ 2 compras no ano atual
     * OU
     * Condição 2: Cliente tem ≥ 2000 pontos de fidelidade (independente de quantas compras fez)
     * Resumindo: frete grátis para clientes fiéis com pelo menos 2 compras no ano, ou para clientes muito fiéis (2000+ pontos) sem restrição.
     */
    private boolean haveFreeShipping(Customer customer) {
        return customerHaveFreeShippingSpecification.isSatisfiedBy(customer);
    }
}
