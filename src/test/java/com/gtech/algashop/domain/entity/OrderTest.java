package com.gtech.algashop.domain.entity;

import com.gtech.algashop.domain.entity.VO.*;
import com.gtech.algashop.domain.entity.VO.id.CustomerId;
import com.gtech.algashop.domain.entity.VO.id.OrderId;
import com.gtech.algashop.domain.entity.VO.id.ProductId;
import com.gtech.algashop.domain.exceptions.OrderInvalidShippingDeliveryDateException;
import com.gtech.algashop.domain.exceptions.OrderStatusCannotBeChanged;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Or;

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
        ProductId productId = new ProductId();

        order.addItem(
                productId,
                new ProductName("Mouse pad"),
                new Money("100"),
                new Quantity(1)
        );

        Assertions.assertThat(order.items()).hasSize(1);

        OrderItem orderItem = order.items().iterator().next();

        Assertions.assertWith(orderItem,
                i -> Assertions.assertThat(i.id()).isNotNull(),
                i -> Assertions.assertThat(i.productId()).isEqualTo(productId),
                i -> Assertions.assertThat(i.productName()).isEqualTo(new ProductName("Mouse pad")),
                i -> Assertions.assertThat(i.price()).isEqualTo(new Money("100")),
                i -> Assertions.assertThat(i.quantity()).isEqualTo(new Quantity(1))
                );
    }

    @Test
    void shouldRecalculateItemsTotals() {
        Order order = Order.draft(new CustomerId());
        ProductId productId = new ProductId();

        order.addItem(
                productId,
                new ProductName("Mouse pad"),
                new Money("100"),
                new Quantity(1)
        );

        order.addItem(
                productId,
                new ProductName("Code's Book"),
                new Money("120"),
                new Quantity(2)
        );

        Assertions.assertThat(order.totalAmount()).isEqualTo(new Money("340"));
        Assertions.assertThat(order.totalQuantity()).isEqualTo(new Quantity(3));
    }

    @Test
    void shouldThrowExceptionWhenTryToChangeItemSet() {
        Order order = Order.draft(new CustomerId());
        ProductId productId = new ProductId();

        order.addItem(
                productId,
                new ProductName("Mouse pad"),
                new Money("100"),
                new Quantity(1)
        );

        Set<OrderItem> items = order.items();

        Assertions.assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(items::clear);
    }

    @Test
    void givenDraftOrderWhenPlacedShouldChangeToPlaced() {
        Order order = Order.draft(new CustomerId());
        order.place();

        Assertions.assertThat(order.isPlaced()).isTrue();
    }

    @Test
    void givenPlacedOrderWhenTryToPlacedShouldThrowException() {
        Order order = Order.draft(new CustomerId());
        order.place();

        Assertions.assertThatExceptionOfType(OrderStatusCannotBeChanged.class)
                .isThrownBy(order::place);
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
        Address address = Address.builder()
                .street("Bourbon Street")
                .number("1134")
                .neighborhood("North Ville")
                .city("York")
                .state("South California")
                .zipCode(new ZipCode("12345"))
                .complement("Apt. 114")
                .build();

        ShippingInfo shippingInfo = ShippingInfo.builder()
                .address(address)
                .fullName(new FullName("John", "Doe"))
                .phone(new Phone("123-111-9911"))
                .document(new Document("255-09-1992"))
                .build();

        Order order = Order.draft(new CustomerId());
        Money shippingCost = Money.ZERO;
        LocalDate expectedDeliveryDate = LocalDate.now().plusDays(2);

        order.changeShipping(shippingInfo, shippingCost, expectedDeliveryDate);

        Assertions.assertWith(order,
                o -> Assertions.assertThat(o.shipping()).isEqualTo(shippingInfo),
                o -> Assertions.assertThat(o.shippingCost()).isEqualTo(shippingCost),
                o -> Assertions.assertThat(o.expectedDeliveryDate()).isEqualTo(expectedDeliveryDate)
                );
    }

    @Test
    void draftOrderWhenChangeShippingInThePastShouldThrowException() {
        Address address = Address.builder()
                .street("Bourbon Street")
                .number("1134")
                .neighborhood("North Ville")
                .city("York")
                .state("South California")
                .zipCode(new ZipCode("12345"))
                .complement("Apt. 114")
                .build();

        ShippingInfo shippingInfo = ShippingInfo.builder()
                .address(address)
                .fullName(new FullName("John", "Doe"))
                .phone(new Phone("123-111-9911"))
                .document(new Document("255-09-1992"))
                .build();

        Order order = Order.draft(new CustomerId());
        Money shippingCost = Money.ZERO;
        LocalDate expectedDeliveryDate = LocalDate.now().minusDays(2);

        Assertions.assertThatExceptionOfType(OrderInvalidShippingDeliveryDateException.class)
                .isThrownBy(() -> order.changeShipping(shippingInfo, shippingCost, expectedDeliveryDate));
    }
}