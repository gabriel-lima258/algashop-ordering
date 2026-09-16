package com.gtech.algashop.core.application.checkout;

import com.gtech.algashop.core.application.checkout.command.ProcessAcceptedCheckoutIntegrationCommand;
import com.gtech.algashop.core.application.checkout.command.OrderSnapshot;
import com.gtech.algashop.core.application.checkout.command.OrderSnapshotAssembler;
import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.domain.model.BusinessException;
import com.gtech.algashop.core.domain.model.commons.Quantity;
import com.gtech.algashop.core.domain.model.commons.ZipCode;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.core.domain.model.costumer.Customers;
import com.gtech.algashop.core.domain.model.order.*;
import com.gtech.algashop.core.domain.model.order.shipping.OriginAddressService;
import com.gtech.algashop.core.domain.model.order.shipping.ShippingCostService;
import com.gtech.algashop.core.domain.model.product.Product;
import com.gtech.algashop.core.domain.model.product.ProductCatalogService;
import com.gtech.algashop.core.domain.model.product.ProductId;
import com.gtech.algashop.core.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.core.ports.in.checkout.BuyNowInput;
import com.gtech.algashop.core.ports.in.checkout.ForBuyingProductAsync;
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
public class BuyNowAsyncApplicationService implements ForBuyingProductAsync {

    // Services
    private final BuyNowService buyNowService;
    private final ProductCatalogService productCatalogService;
    private final ShippingCostService shippingCostService;
    private final OriginAddressService originAddressService;

    // Repositories
    private final Customers customers;

    // Assembler
    private final OrderSnapshotAssembler orderSnapshotAssembler;

    // Disassemblers
    private final ShippingInputDisassembler shippingInputDisassembler;
    private final BillingInputDisassembler billingInputDisassembler;

    // Security
    private final SecurityCheckApplicationService securityCheck;

    // Events
    private final ForPublishingOrderIntegrationCommands forPublishingOrderIntegrationCommands;

    @Transactional
    @Override
    public String buyNow(BuyNowInput input) {
        Objects.requireNonNull(input);

        verifyCanOrderFor(input.getCustomerId());

        // extraindo enum a partir do input String
        PaymentMethod paymentMethod = PaymentMethod.valueOf(input.getPaymentMethod());
        CustomerId customerId = new CustomerId(input.getCustomerId());
        Quantity quantity = new Quantity(input.getQuantity());
        CreditCardId creditCardId = null;

        if (paymentMethod.equals(PaymentMethod.CREDIT_CARD)) {
            if (input.getCreditCardId() == null) {
                throw new BusinessException("Credit card id is required");
            }
            creditCardId = new CreditCardId(input.getCreditCardId());
        }

        Customer customer = customers.ofId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        // procura o produto
        Product product = findProduct(new ProductId(input.getProductId()));

        // calcula frete
        var shippingCalculateResult = calculateShippingCost(input.getShipping());

        // convertendo valor para domain
        Shipping shipping = shippingInputDisassembler.toDomainModel(input.getShipping(),
                shippingCalculateResult);
        Billing billing = billingInputDisassembler.toDomainModel(input.getBilling());

        // construindo pedido instantâneo
        Order order = buyNowService.buyNow(
                product, customer, billing, shipping, quantity, paymentMethod, creditCardId
        );

        // aqui NAO ha orders.add(): a persistencia migrou para o consumidor do comando
        // (ProcessAcceptedCheckoutApplicationService) - listen to yourself

        // transforma dados domain para snapshot a ser enviado no comando
        OrderSnapshot snapshot = orderSnapshotAssembler.toSnapshot(order);

        // envio do COMANDO de processamento (nao e evento: e imperativo, tem um
        // destinatario e uma expectativa de execucao - o proprio ordering, depois)
        ProcessAcceptedCheckoutIntegrationCommand integrationCommand = new ProcessAcceptedCheckoutIntegrationCommand(snapshot);
        forPublishingOrderIntegrationCommands.send(integrationCommand);

        return order.id().toString();
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

    private Product findProduct(ProductId productId) {
        return productCatalogService.ofId(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    // verificação se não for customer e se o usuário autenticado não for o mesmo do customerId
    private void verifyCanOrderFor(@NotNull UUID customerId) {
        if (!(securityCheck.canOrderFor(customerId))) {
            throw new AccessDeniedException("Cannot order for customer " + customerId);
        }
    }
}
