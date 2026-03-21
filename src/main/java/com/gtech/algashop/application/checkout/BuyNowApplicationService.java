package com.gtech.algashop.application.checkout;

import com.gtech.algashop.domain.model.commons.Quantity;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BuyNowApplicationService {

    // Services
    private final BuyNowService buyNowService;
    private final ProductCatalogService productCatalogService;
    private final ShippingCostService shippingCostService;
    private final OriginAddressService originAddressService;

    // Repositório
    private final Orders orders;
    private final Customers customers;

    // Disassemblers
    private final ShippingInputDisassembler shippingInputDisassembler;
    private final BillingInputDisassembler billingInputDisassembler;

    @Transactional
    public String buyNow(BuyNowInput input) {
        Objects.requireNonNull(input);

        // extraindo enum a partir do input String
        PaymentMethod paymentMethod = PaymentMethod.valueOf(input.getPaymentMethod());
        CustomerId customerId = new CustomerId(input.getCustomerId());
        Quantity quantity = new Quantity(input.getQuantity());

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
                product, customer, billing, shipping, quantity, paymentMethod
        );

        // persistindo order
        orders.add(order);

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
}
