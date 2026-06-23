package com.gtech.algashop.core.application.checkout;

import com.gtech.algashop.core.domain.model.BusinessException;
import com.gtech.algashop.core.domain.model.commons.ZipCode;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.core.domain.model.costumer.Customers;
import com.gtech.algashop.core.domain.model.order.*;
import com.gtech.algashop.core.domain.model.order.shipping.OriginAddressService;
import com.gtech.algashop.core.domain.model.order.shipping.ShippingCostService;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartId;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartNotFound;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCarts;
import com.gtech.algashop.core.ports.in.checkout.CheckoutInput;
import com.gtech.algashop.core.ports.in.checkout.ForBuyingWithShoppingCart;
import com.gtech.algashop.core.ports.in.checkout.ShippingInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CheckoutApplicationService implements ForBuyingWithShoppingCart {

    private final ShippingCostService shippingCostService;
    private final OriginAddressService originAddressService;
    private final CheckoutService checkoutService;

    // Repositório
    private final Orders orders;
    private final ShoppingCarts shoppingCarts;
    private final Customers customers;

    // Disassemblers
    private final ShippingInputDisassembler shippingInputDisassembler;
    private final BillingInputDisassembler billingInputDisassembler;

    @Transactional
    @Override
    public String checkout(CheckoutInput input) {
        Objects.requireNonNull(input);

        // extraindo enum a partir do input String
        PaymentMethod paymentMethod = PaymentMethod.valueOf(input.getPaymentMethod());
        CreditCardId creditCardId = null;

        if (paymentMethod.equals(PaymentMethod.CREDIT_CARD)) {
            if (input.getCreditCardId() == null) {
                throw new BusinessException("Credit card id is required");
            }
            creditCardId = new CreditCardId(input.getCreditCardId());
        }

        ShoppingCartId shoppingCartId = new ShoppingCartId(input.getShoppingCartId());
        ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingCartId)
                .orElseThrow(ShoppingCartNotFound::new);
        Customer customer = customers.ofId(shoppingCart.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(shoppingCart.customerId()));


        // calcula frete
        var shippingCalculateResult = calculateShippingCost(input.getShipping());

        // convertendo valor para domain
        Shipping shipping = shippingInputDisassembler.toDomainModel(input.getShipping(),
                shippingCalculateResult);
        Billing billing = billingInputDisassembler.toDomainModel(input.getBilling());

        // fazer checkout do shoppingCart
        Order checkout = checkoutService.checkout(customer, shoppingCart, billing, shipping, paymentMethod, creditCardId);

        // persistir o checkout em order e seu shoppingCart
        orders.add(checkout);
        shoppingCarts.add(shoppingCart);

        return checkout.id().toString();
    }

    // metodo responsável por calcular taxa de entrega
    private ShippingCostService.CalculationResult calculateShippingCost(ShippingInput shipping) {
        ZipCode origin = originAddressService.originAddress().zipCode();
        ZipCode destination = new ZipCode(shipping.getAddress().getZipCode());

        return shippingCostService.calculate(new ShippingCostService.CalculationRequest(
                origin,
                destination
        ));
    }
}
