package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.DomainService;
import lombok.RequiredArgsConstructor;

import java.time.Year;

@DomainService
@RequiredArgsConstructor
public class BuyNowService {

    // Uso do padrão Specification: encapsula a regra de negócio de frete grátis em uma classe própria,
    // permitindo reutilização em outros contextos (ex: CheckoutService), testabilidade isolada
    // e facilidade de manutenção — alterações na regra ficam centralizadas em um único lugar.
    private final CustomerHaveFreeShippingSpecification customerHaveFreeShippingSpecification;

    public Order buyNow(Product product,
                        Customer customer,
                        Billing billing,
                        Shipping shipping,
                        Quantity quantity,
                        PaymentMethod paymentMethod) {

        product.checkOutOfStock();

        Order order = Order.draft(customer.id());
        order.changeBilling(billing);
        order.changePaymentMethod(paymentMethod);

        order.addItem(product, quantity);

        // regra para ver se possui frete gratis ou não
        if (haveFreeShipping(customer)) {
            Shipping freeShipping = shipping.toBuilder().cost(Money.ZERO).build();
            order.changeShipping(freeShipping);
        } else {
            order.changeShipping(shipping);
        }

        order.markAsPlaced();

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
