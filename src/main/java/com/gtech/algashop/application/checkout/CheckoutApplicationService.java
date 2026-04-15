package com.gtech.algashop.application.checkout;

import com.gtech.algashop.domain.model.BusinessException;
import com.gtech.algashop.domain.model.commons.ZipCode;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.order.*;
import com.gtech.algashop.domain.model.order.shipping.OriginAddressService;
import com.gtech.algashop.domain.model.order.shipping.ShippingCostService;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import com.gtech.algashop.domain.model.product.ProductId;
import com.gtech.algashop.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartId;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartNotFound;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCarts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutApplicationService {

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
