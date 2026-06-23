package com.gtech.algashop.core.application.checkout;

import com.gtech.algashop.core.application.AbstractIntegrationTest;
import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.commons.Quantity;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.costumer.Customers;
import com.gtech.algashop.core.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.core.domain.model.order.OrderId;
import com.gtech.algashop.core.domain.model.order.OrderPlacedEvent;
import com.gtech.algashop.core.domain.model.order.Orders;
import com.gtech.algashop.core.domain.model.order.shipping.ShippingCostService;
import com.gtech.algashop.core.domain.model.product.ProductId;
import com.gtech.algashop.core.domain.model.product.ProductName;
import com.gtech.algashop.core.domain.model.shoppingcart.*;
import com.gtech.algashop.core.ports.in.checkout.CheckoutInput;
import com.gtech.algashop.infrastructure.adapters.in.listerner.order.OrderEventListener;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

class CheckoutApplicationServiceIT extends AbstractIntegrationTest {

    @Autowired
    private CheckoutApplicationService service;

    @Autowired
    private Orders orders;

    @Autowired
    private ShoppingCarts shoppingCarts;

    @Autowired
    private Customers customers;

    @MockitoBean
    private ShippingCostService shippingCostService;

    @MockitoSpyBean
    private OrderEventListener orderEventListener;

    @BeforeEach
    void setUp() {
        Mockito.when(shippingCostService.calculate(Mockito.any(ShippingCostService.CalculationRequest.class)))
                .thenReturn(new ShippingCostService.CalculationResult(
                        new Money("10.00"),
                        LocalDate.now().plusDays(3)
                ));
    }

    // =========================================================
    // checkout
    // =========================================================

    @Test
    void shouldCheckout() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCart(customerId, true);

        CheckoutInput input = CheckoutInputTestDataBuilder.aCheckoutInput(cart.id().value()).build();

        String orderId = service.checkout(input);

        Assertions.assertThat(orderId).isNotBlank();
        Assertions.assertThat(orders.exists(new OrderId(orderId))).isTrue();
        ShoppingCart updatedCart = shoppingCarts.ofId(cart.id()).orElseThrow();
        Assertions.assertThat(updatedCart.isEmpty()).isTrue();

        Mockito.verify(orderEventListener, Mockito.times(1))
                .listen(Mockito.any(OrderPlacedEvent.class));
    }

    @Test
    void shouldThrowShoppingCartNotFoundWhenCartDoesNotExist() {
        CheckoutInput input = CheckoutInputTestDataBuilder.aCheckoutInput(UUID.randomUUID()).build();

        Assertions.assertThatThrownBy(() -> service.checkout(input))
                .isInstanceOf(ShoppingCartNotFound.class);
    }

    @Test
    void shouldThrowShoppingCartCantProceedToCheckoutExceptionWhenCartIsEmpty() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCart(customerId, false);

        CheckoutInput input = CheckoutInputTestDataBuilder.aCheckoutInput(cart.id().value()).build();

        Assertions.assertThatThrownBy(() -> service.checkout(input))
                .isInstanceOf(ShoppingCartCantProceedToCheckoutException.class);
    }

    @Test
    void shouldThrowShoppingCartCantProceedToCheckoutExceptionWhenCartHasUnavailableItems() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCartWithUnavailableItems(customerId);

        CheckoutInput input = CheckoutInputTestDataBuilder.aCheckoutInput(cart.id().value()).build();

        Assertions.assertThatThrownBy(() -> service.checkout(input))
                .isInstanceOf(ShoppingCartCantProceedToCheckoutException.class);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private CustomerId persistCustomer() {
        CustomerId customerId = new CustomerId();
        customers.add(CustomerTestDataBuilder.existingCustomer().id(customerId).build());
        return customerId;
    }

    private ShoppingCart persistCart(CustomerId customerId, boolean withItems) {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart()
                .customerId(customerId)
                .withItems(withItems)
                .build();
        shoppingCarts.add(cart);
        return cart;
    }

    private ShoppingCart persistCartWithUnavailableItems(CustomerId customerId) {
        ShoppingCartId cartId = new ShoppingCartId();
        ShoppingCartItem unavailableItem = ShoppingCartItem.existing()
                .id(new ShoppingCartItemId())
                .shoppingCartId(cartId)
                .productId(new ProductId())
                .name(new ProductName("Unavailable Product"))
                .price(new Money("100.00"))
                .quantity(new Quantity(1))
                .totalAmount(new Money("100.00"))
                .available(false)
                .build();
        ShoppingCart cart = ShoppingCart.existing()
                .id(cartId)
                .version(null)
                .customerId(customerId)
                .totalAmount(new Money("100.00"))
                .totalItems(new Quantity(1))
                .createdAt(OffsetDateTime.now())
                .items(new HashSet<>(Set.of(unavailableItem)))
                .build();
        shoppingCarts.add(cart);
        return cart;
    }
}
