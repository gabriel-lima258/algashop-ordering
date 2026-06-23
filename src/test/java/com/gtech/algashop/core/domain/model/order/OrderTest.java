package com.gtech.algashop.core.domain.model.order;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.commons.Quantity;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.core.domain.model.product.*;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

class OrderTest {

    @Test
    void shouldGenerate() {
        CustomerId customerId = new CustomerId();
        Order order = Order.draft(customerId);

        Assertions.assertWith(order,
                o -> Assertions.assertThat(o.id()).isNotNull(),
                o -> Assertions.assertThat(o.customerId()).isEqualTo(customerId),
                o -> Assertions.assertThat(o.totalAmount()).isEqualTo(Money.ZERO),
                o -> Assertions.assertThat(o.totalQuantity()).isEqualTo(Quantity.ZERO),
                o -> Assertions.assertThat(o.isDraft()).isTrue(),
                o -> Assertions.assertThat(o.items()).isEmpty(),

                o -> Assertions.assertThat(o.placedAt()).isNull(),
                o -> Assertions.assertThat(o.paidAt()).isNull(),
                o -> Assertions.assertThat(o.canceledAt()).isNull(),
                o -> Assertions.assertThat(o.readyAt()).isNull(),
                o -> Assertions.assertThat(o.billing()).isNull(),
                o -> Assertions.assertThat(o.shipping()).isNull(),
                o -> Assertions.assertThat(o.paymentMethod()).isNull()
                );


    }

    @Test
    void shouldAddItem() {
        Order order = Order.draft(new CustomerId());
        Product product = ProductTestDataBuilder.aProductMousePad().build();
        ProductId productId = product.id();

        order.addItem(product, new Quantity(1));

        Assertions.assertThat(order.items()).hasSize(1);

        OrderItem orderItem = order.items().iterator().next();

        Assertions.assertWith(orderItem,
                i -> Assertions.assertThat(i.id()).isNotNull(),
                i -> Assertions.assertThat(i.productId()).isEqualTo(productId),
                i -> Assertions.assertThat(i.productName()).isEqualTo(new ProductName("Mouse Pad Gamer")),
                i -> Assertions.assertThat(i.price()).isEqualTo(new Money("120")),
                i -> Assertions.assertThat(i.quantity()).isEqualTo(new Quantity(1))
                );
    }

    @Test
    void shouldNotAllowAddItemWhenOrderIsNotDraft() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        Product product = ProductTestDataBuilder.aProductRamMemory().build();

        Assertions.assertThatExceptionOfType(OrderCannotBeEditedException.class)
                .isThrownBy(() -> order.addItem(product, new Quantity(1)));
    }

    @Test
    void shouldThrowExceptionWhenTryToRemoveOrderItemIdIsNull() {
        Order order = Order.draft(new CustomerId());

        Assertions.assertThatExceptionOfType(NullPointerException.class)
                .isThrownBy(() -> order.removeItem(null));
    }

    @Test
    void shouldRemoveItemSuccessfullyAndRecalculateTotals() {
        Order order = Order.draft(new CustomerId());

        Product notebook = ProductTestDataBuilder.aProduct().build();
        Product mousePad = ProductTestDataBuilder.aProductMousePad().build();

        order.addItem(notebook, new Quantity(1));
        order.addItem(mousePad, new Quantity(1));

        Assertions.assertThat(order.items()).hasSize(2);
        Assertions.assertThat(order.totalQuantity()).isEqualTo(new Quantity(2));
        Assertions.assertThat(order.totalAmount()).isEqualTo(new Money("4620"));

        OrderItemId itemIdToRemove = order.items().stream()
                .filter(i -> i.productId().equals(mousePad.id()))
                .findFirst()
                .orElseThrow()
                .id();

        order.removeItem(itemIdToRemove);

        Assertions.assertThat(order.items()).hasSize(1);
        Assertions.assertThat(order.totalQuantity()).isEqualTo(new Quantity(1));
        Assertions.assertThat(order.totalAmount()).isEqualTo(new Money("4500"));
    }

    @Test
    void shouldThrowWhenRemovingItemThatDoesNotExist() {
        Order order = Order.draft(new CustomerId());
        OrderItemId nonExistingItem = new OrderItemId();

        Assertions.assertThatExceptionOfType(OrderDoesNotContainOrderItemException.class)
                .isThrownBy(() -> order.removeItem(nonExistingItem));
    }

    @Test
    void shouldNotAllowRemoveItemWhenOrderIsNotDraft() {
        Order order = OrderTestDataBuilder.anOrder().build();
        order.markAsPlaced();

        OrderItemId itemId = order.items().iterator().next().id();

        Assertions.assertThatExceptionOfType(OrderCannotBeEditedException.class)
                .isThrownBy(() -> order.removeItem(itemId));
    }

    @Test
    void shouldRecalculateItemsTotals() {
        Order order = Order.draft(new CustomerId());
        Product mousePad = ProductTestDataBuilder.aProductMousePad().build();
        Product ramMemory = ProductTestDataBuilder.aProductRamMemory().build();

        order.addItem(mousePad, new Quantity(1));
        order.addItem(ramMemory, new Quantity(2));

        Assertions.assertThat(order.totalAmount()).isEqualTo(new Money("520"));
        Assertions.assertThat(order.totalQuantity()).isEqualTo(new Quantity(3));
    }

    @Test
    void shouldThrowExceptionWhenTryToChangeItemSet() {
        Order order = Order.draft(new CustomerId());
        Product product = ProductTestDataBuilder.aProductMousePad().build();

        order.addItem(product, new Quantity(1));

        Set<OrderItem> items = order.items();

        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(items::clear);
    }

    @Test
    void givenDraftOrderWhenPlacedShouldChangeToPlaced() {
        Order order = OrderTestDataBuilder.anOrder().build();
        order.markAsPlaced();

        Assertions.assertThat(order.isPlaced()).isTrue();
    }

    @Test
    void givenPlacedOrderWhenTryToPlacedShouldThrowException() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();

        Assertions.assertThatExceptionOfType(OrderStatusCannotBeChanged.class)
                .isThrownBy(order::markAsPlaced);
    }

    @Test
    void givenPlacedOrderWhenMarkAsPaidShouldChangeAsPaid() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        order.markAsPaid();

        Assertions.assertThat(order.isPaid()).isTrue();
        Assertions.assertThat(order.paidAt()).isNotNull();
    }

    @Test
    void givenPaidOrderWhenMarkAsReadyShouldChangeAsReady() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).build();
        order.markAsReady();

        Assertions.assertThat(order.isReady()).isTrue();
        Assertions.assertThat(order.readyAt()).isNotNull();
    }

    @Test
    void givenDraftOrderWhenMarkAsReadyShouldThrowException() {
        Order order = OrderTestDataBuilder.anOrder().build();

        Assertions.assertThatExceptionOfType(OrderStatusCannotBeChanged.class)
                .isThrownBy(order::markAsReady);
    }

    @Test
    void givenPlacedOrderWhenMarkAsReadyShouldThrowException() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();

        Assertions.assertThatExceptionOfType(OrderStatusCannotBeChanged.class)
                .isThrownBy(order::markAsReady);
    }

    @Test
    void givenCanceledOrderWhenMarkAsReadyShouldThrowException() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.CANCELED).build();

        Assertions.assertThatExceptionOfType(OrderStatusCannotBeChanged.class)
                .isThrownBy(order::markAsReady);
    }


    @Test
    void givenDraftOrderWhenMarkAsCanceledShouldChangeAsCanceled() {
        Order order = OrderTestDataBuilder.anOrder().build();
        order.markAsCanceled();

        Assertions.assertThat(order.isCanceled()).isTrue();
        Assertions.assertThat(order.canceledAt()).isNotNull();
    }

    @Test
    void givenPlacedtOrderWhenMarkAsCanceledShouldChangeAsCanceled() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        order.markAsCanceled();

        Assertions.assertThat(order.isCanceled()).isTrue();
        Assertions.assertThat(order.canceledAt()).isNotNull();
    }

    @Test
    void givenPaidOrderWhenMarkAsCanceledShouldChangeAsCanceled() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).build();
        order.markAsCanceled();

        Assertions.assertThat(order.isCanceled()).isTrue();
        Assertions.assertThat(order.canceledAt()).isNotNull();
    }

    @Test
    void givenReadytOrderWhenMarkAsCanceledShouldChangeAsCanceled() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.READY).build();
        order.markAsCanceled();

        Assertions.assertThat(order.isCanceled()).isTrue();
        Assertions.assertThat(order.canceledAt()).isNotNull();
    }

    @Test
    void givenCanceledOrderWhenMarkAsCanceledShouldThrowException() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.CANCELED).build();

        Assertions.assertThatExceptionOfType(OrderStatusCannotBeChanged.class)
                .isThrownBy(order::markAsCanceled);
    }

    @Test
    void draftOrderWhenChangePaymentMethodShouldAllowChange() {
        Order order = Order.draft(new CustomerId());
        order.changePaymentMethod(PaymentMethod.CREDIT_CARD, new CreditCardId());
        Assertions.assertWith(order.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    @Test
    void shouldNotChangePaymentMethodWhenNotDraft() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        Assertions.assertThatExceptionOfType(OrderCannotBeEditedException.class)
                .isThrownBy(() -> order.changePaymentMethod(PaymentMethod.CREDIT_CARD, new CreditCardId()));
    }

    @Test
    void shouldNotChangeShippingWhenNotDraft() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        Shipping shipping = OrderTestDataBuilder.aShipping();

        Assertions.assertThatExceptionOfType(OrderCannotBeEditedException.class)
                .isThrownBy(() -> order.changeShipping(shipping));
    }

    @Test
    void shouldNotChangeBillingWhenNotDraft() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        Billing billing = OrderTestDataBuilder.aBilling();

        Assertions.assertThatExceptionOfType(OrderCannotBeEditedException.class)
                .isThrownBy(() -> order.changeBilling(billing));
    }

    @Test
    void shouldNotChangeItemQuantityWhenNotDraft() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        OrderItem orderItem = order.items().iterator().next();

        Assertions.assertThatExceptionOfType(OrderCannotBeEditedException.class)
                .isThrownBy(() -> order.changeItemQuantity(orderItem.id(), new Quantity(5)));
    }

    @Test
    void draftOrderWhenChangeBillingInfoShouldAllowChange() {
        Billing billing = OrderTestDataBuilder.aBilling();

        Order order = Order.draft(new CustomerId());
        order.changeBilling(billing);

        Assertions.assertThat(order.billing()).isEqualTo(billing);
    }

    @Test
    void draftOrderWhenChangeShippingInfoShouldAllowChange() {
        Shipping shipping = OrderTestDataBuilder.aShipping();
        Order order = Order.draft(new CustomerId());
        Money expectedTotalAmount = order.totalAmount().add(shipping.cost());

        order.changeShipping(shipping);

        Assertions.assertWith(order,
                o -> Assertions.assertThat(o.shipping()).isEqualTo(shipping),
                o -> Assertions.assertThat(o.totalAmount()).isEqualTo(expectedTotalAmount)
                );
    }

    @Test
    void draftOrderWhenChangeShippingInThePastShouldThrowException() {
        LocalDate expectedDeliveryDate = LocalDate.now().minusDays(2);

        Shipping shipping = OrderTestDataBuilder.aShipping().toBuilder()
                .expectedDate(expectedDeliveryDate)
                .build();

        Order order = Order.draft(new CustomerId());

        Assertions.assertThatExceptionOfType(OrderInvalidShippingDeliveryDateException.class)
                .isThrownBy(() -> order.changeShipping(shipping));
    }

    @Test
    void givenDraftOrderWhenChangeItemShouldRecalculate() {
        Order order = Order.draft(new CustomerId());
        Product notebook = ProductTestDataBuilder.aProduct().build();

        order.addItem(notebook, new Quantity(1));

        OrderItem orderItem = order.items().iterator().next();

        order.changeItemQuantity(orderItem.id(), new Quantity(2));

        Assertions.assertWith(order,
                o -> Assertions.assertThat(o.totalAmount()).isEqualTo(new Money("9000")),
                o -> Assertions.assertThat(o.totalQuantity()).isEqualTo(new Quantity(2))
                );
    }

    @Test
    void givenOutOfStockProductWhenTryToAddToAnOrderShouldThrowException() {
        Order order = Order.draft(new CustomerId());

        ThrowableAssert.ThrowingCallable addItemTask = () -> order.addItem(
                ProductTestDataBuilder.aProductUnavailable().build(),
                new Quantity(1)
        );

        Assertions.assertThatExceptionOfType(ProductOutOfStockException.class)
                .isThrownBy(addItemTask);
    }

    @Test
    void whenOrderIsPlacedShouldGenerateEvent() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        Order order = OrderTestDataBuilder.anOrder().customerId(customer.id()).status(OrderStatus.DRAFT).build();
        order.markAsPlaced();

        OrderPlacedEvent event = new OrderPlacedEvent(
                order.id(),
                customer.id(),
                order.placedAt()
        );
        Assertions.assertThat(order.domainEvents()).contains(event);
    }

    @Test
    void whenOrderIsPaidShouldGenerateEvent() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        Order order = OrderTestDataBuilder.anOrder().customerId(customer.id()).status(OrderStatus.PLACED).build();
        order.markAsPaid();

        OrderPaidEvent event = new OrderPaidEvent(
                order.id(),
                customer.id(),
                order.paidAt()
        );
        Assertions.assertThat(order.domainEvents()).contains(event);
    }

    @Test
    void whenOrderIsReadyShouldGenerateEvent() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        Order order = OrderTestDataBuilder.anOrder().customerId(customer.id()).status(OrderStatus.PAID).build();
        order.markAsReady();

        OrderReadyEvent event = new OrderReadyEvent(
                order.id(),
                customer.id(),
                order.readyAt()
        );
        Assertions.assertThat(order.domainEvents()).contains(event);
    }

    @Test
    void whenOrderIsCanceledShouldGenerateEvent() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        Order order = OrderTestDataBuilder.anOrder().customerId(customer.id()).status(OrderStatus.READY).build();
        order.markAsCanceled();

        OrderCanceledEvent event = new OrderCanceledEvent(
                order.id(),
                customer.id(),
                order.canceledAt()
        );
        Assertions.assertThat(order.domainEvents()).contains(event);
    }

}