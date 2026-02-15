package com.gtech.algashop.domain.entity;

import com.gtech.algashop.domain.entity.VO.*;
import com.gtech.algashop.domain.entity.VO.id.CustomerId;
import com.gtech.algashop.domain.entity.VO.id.ProductId;
import com.gtech.algashop.domain.entity.factory.OrderTestDataBuilder;
import com.gtech.algashop.domain.entity.factory.ProductTestDataBuilder;
import com.gtech.algashop.domain.exceptions.OrderInvalidShippingDeliveryDateException;
import com.gtech.algashop.domain.exceptions.OrderStatusCannotBeChanged;
import com.gtech.algashop.domain.exceptions.ProductOutOfStockException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

class OrderTest {

    @Test
    void shouldGenerate() {
        Order order = Order.draft(new CustomerId());
        Assertions.assertThat(order).isNotNull();
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
    void givenOrderWhenMarkAsCanceledShouldChangeAsCanceled() {
        Order order = OrderTestDataBuilder.anOrder().build();
        order.markAsCanceled();

        Assertions.assertThat(order.isCanceled()).isTrue();
        Assertions.assertThat(order.canceledAt()).isNotNull();
    }

    @Test
    void draftOrderWhenChangePaymentMethodShouldAllowChange() {
        Order order = Order.draft(new CustomerId());
        order.changePaymentMethod(PaymentMethod.CREDIT_CARD);
        Assertions.assertWith(order.paymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
    }

    @Test
    void draftOrderWhenChangeBillingInfoShouldAllowChange() {
        Address address = Address.builder()
                .street("Bourbon Street")
                .number("1134")
                .neighborhood("North Ville")
                .city("York")
                .state("South California")
                .zipCode(new ZipCode("12345"))
                .complement("Apt. 114")
                .build();

        BillingInfo billingInfo = BillingInfo.builder()
                .address(address)
                .fullName(new FullName("John", "Doe"))
                .phone(new Phone("123-111-9911"))
                .document(new Document("255-09-1992"))
                .build();

        Order order = Order.draft(new CustomerId());
        order.changeBillingInfo(billingInfo);

        Assertions.assertThat(order.billing()).isEqualTo(billingInfo);
    }

    @Test
    void draftOrderWhenChangeShippingInfoShouldAllowChange() {
        Shipping shipping = OrderTestDataBuilder.aShipping();

        Order order = Order.draft(new CustomerId());

        order.changeShipping(shipping);

        Assertions.assertWith(order,
                o -> Assertions.assertThat(o.shipping()).isEqualTo(shipping)
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
}