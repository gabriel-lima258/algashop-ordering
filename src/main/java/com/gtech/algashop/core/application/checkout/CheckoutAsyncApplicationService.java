package com.gtech.algashop.core.application.checkout;

import com.gtech.algashop.core.application.checkout.command.ProcessAcceptedCheckoutIntegrationCommand;
import com.gtech.algashop.core.application.order.snapshot.OrderSnapshot;
import com.gtech.algashop.core.application.order.snapshot.OrderSnapshotAssembler;
import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.domain.model.DomainException;
import com.gtech.algashop.core.domain.model.commons.ZipCode;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundExceptionDomain;
import com.gtech.algashop.core.domain.model.costumer.Customers;
import com.gtech.algashop.core.domain.model.order.*;
import com.gtech.algashop.core.domain.model.order.shipping.OriginAddressService;
import com.gtech.algashop.core.domain.model.order.shipping.ShippingCostService;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartNotFoundDomain;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCarts;
import com.gtech.algashop.core.ports.in.checkout.CheckoutInput;
import com.gtech.algashop.core.ports.in.checkout.ForBuyingWithShoppingCartAsync;
import com.gtech.algashop.core.ports.in.checkout.ShippingInput;
import com.gtech.algashop.core.ports.out.order.ForPublishingOrderIntegrationCommands;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

// classe de application refatorado em async para receber o padrão listen to yourself
// removemos a responsabilidade de persistencia de repositorio aqui

@Service
@RequiredArgsConstructor
public class CheckoutAsyncApplicationService implements ForBuyingWithShoppingCartAsync {

    // Repositories
    private final ShoppingCarts shoppingCarts;
    private final Customers customers;

    // Services
    private final ShippingCostService shippingCostService;
    private final OriginAddressService originAddressService;
    private final CheckoutService checkoutService;

    // Assembler
    private final OrderSnapshotAssembler orderSnapshotAssembler;

    // Disassemblers
    private final ShippingInputDisassembler shippingInputDisassembler;
    private final BillingInputDisassembler billingInputDisassembler;

    // security
    private final SecurityCheckApplicationService securityCheck;

    // Events
    private final ForPublishingOrderIntegrationCommands forPublishingOrderIntegrationCommands;

    @Transactional
    @Override
    public String checkout(CheckoutInput input) {
        Objects.requireNonNull(input);

        CustomerId customerId = new CustomerId(input.getCustomerId());

        // extraindo enum a partir do input String
        PaymentMethod paymentMethod = PaymentMethod.valueOf(input.getPaymentMethod());
        CreditCardId creditCardId = null;

        if (paymentMethod.equals(PaymentMethod.CREDIT_CARD)) {
            if (input.getCreditCardId() == null) {
                throw new DomainException("Credit card id is required");
            }
            creditCardId = new CreditCardId(input.getCreditCardId());
        }

        Customer customer = loadCustomer(customerId);
        ShoppingCart shoppingCart = loadShoppingCart(customerId);

        verifyCanOrderFor(shoppingCart.customerId().value());

        // calcula frete
        var shippingCalculateResult = calculateShippingCost(input.getShipping());

        // convertendo valor para domain
        Shipping shipping = shippingInputDisassembler.toDomainModel(input.getShipping(),
                shippingCalculateResult);
        Billing billing = billingInputDisassembler.toDomainModel(input.getBilling());

        // fazer checkout do shoppingCart
        Order checkout = checkoutService.checkout(customer, shoppingCart, billing, shipping, paymentMethod, creditCardId);

        // transforma dados domain para snapshot a ser enviado no comando
        OrderSnapshot snapshot = orderSnapshotAssembler.toSnapshot(checkout, shoppingCart.id().value());

        // envio do COMANDO de processamento (nao e evento: e imperativo, tem um
        // destinatario e uma expectativa de execucao - o proprio ordering, depois)
        ProcessAcceptedCheckoutIntegrationCommand integrationCommand = new ProcessAcceptedCheckoutIntegrationCommand(snapshot);
        forPublishingOrderIntegrationCommands.send(integrationCommand);

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

    // verificação se não for customer e se o usuário autenticado não for o mesmo do customerId em order
    private void verifyCanOrderFor(@NotNull UUID customerId) {
        if (!(securityCheck.canOrderFor(customerId))) {
            throw new AccessDeniedException("Cannot order for customer " + customerId);
        }
    }

    private ShoppingCart loadShoppingCart(CustomerId customerId) {
        return shoppingCarts.ofCustomer(customerId).orElseThrow(() -> ShoppingCartNotFoundDomain.ofCustomer(customerId.value()));
    }

    private Customer loadCustomer(CustomerId customerId) {
        return customers.ofId(customerId).orElseThrow(() -> new CustomerNotFoundExceptionDomain(customerId));
    }
}
