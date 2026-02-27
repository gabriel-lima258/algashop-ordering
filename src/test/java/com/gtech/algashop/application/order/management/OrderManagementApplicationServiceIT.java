package com.gtech.algashop.application.order.management;

import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.OrderNotFoundException;
import com.gtech.algashop.domain.model.order.OrderStatus;
import com.gtech.algashop.domain.model.order.OrderStatusCannotBeChanged;
import com.gtech.algashop.domain.model.order.OrderTestDataBuilder;
import com.gtech.algashop.domain.model.order.Orders;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderManagementApplicationServiceIT {

    @Autowired
    private OrderManagementApplicationService service;

    @Autowired
    private Orders orders;

    @Autowired
    private Customers customers;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    @BeforeEach
    void setUp() {
        if (!customers.exists(CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID)) {
            customers.add(CustomerTestDataBuilder.existingCustomer().build());
        }
    }

    // =========================================================
    // markAsCanceled
    // =========================================================

    @Test
    void shouldCancelOrder() {
        Order order = persistOrder(OrderStatus.PLACED);

        service.markAsCanceled(order.id().toString());

        Order updated = orders.ofId(order.id()).orElseThrow();
        Assertions.assertThat(updated.status()).isEqualTo(OrderStatus.CANCELED);
        Assertions.assertThat(updated.canceledAt()).isNotNull();
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenCancelingNonExistentOrder() {
        String nonExistentId = new OrderId().toString();

        Assertions.assertThatThrownBy(() -> service.markAsCanceled(nonExistentId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowOrderStatusCannotBeChangedWhenCancelingAlreadyCanceledOrder() {
        Order order = persistOrder(OrderStatus.CANCELED);

        Assertions.assertThatThrownBy(() -> service.markAsCanceled(order.id().toString()))
                .isInstanceOf(OrderStatusCannotBeChanged.class);
    }

    // =========================================================
    // markAsPaid
    // =========================================================

    @Test
    void shouldMarkOrderAsPaid() {
        Order order = persistOrder(OrderStatus.PLACED);

        service.markAsPaid(order.id().toString());

        Order updated = orders.ofId(order.id()).orElseThrow();
        Assertions.assertThat(updated.status()).isEqualTo(OrderStatus.PAID);
        Assertions.assertThat(updated.paidAt()).isNotNull();
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenMarkingNonExistentOrderAsPaid() {
        String nonExistentId = new OrderId().toString();

        Assertions.assertThatThrownBy(() -> service.markAsPaid(nonExistentId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowOrderStatusCannotBeChangedWhenMarkingCanceledOrderAsPaid() {
        // PAID.previousStatus = [PLACED] → CANCELED cannot transition to PAID
        Order order = persistOrder(OrderStatus.CANCELED);

        Assertions.assertThatThrownBy(() -> service.markAsPaid(order.id().toString()))
                .isInstanceOf(OrderStatusCannotBeChanged.class);
    }

    // =========================================================
    // markAsReady
    // =========================================================

    @Test
    void shouldMarkOrderAsReady() {
        Order order = persistOrder(OrderStatus.PAID);

        service.markAsReady(order.id().toString());

        Order updated = orders.ofId(order.id()).orElseThrow();
        Assertions.assertThat(updated.status()).isEqualTo(OrderStatus.READY);
        Assertions.assertThat(updated.readyAt()).isNotNull();
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenMarkingNonExistentOrderAsReady() {
        String nonExistentId = new OrderId().toString();

        Assertions.assertThatThrownBy(() -> service.markAsReady(nonExistentId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowOrderStatusCannotBeChangedWhenMarkingPlacedOrderAsReady() {
        // READY.previousStatus = [PAID] → PLACED cannot transition to READY
        Order order = persistOrder(OrderStatus.PLACED);

        Assertions.assertThatThrownBy(() -> service.markAsReady(order.id().toString()))
                .isInstanceOf(OrderStatusCannotBeChanged.class);
    }

    // =========================================================
    // Helper
    // =========================================================

    private Order persistOrder(OrderStatus status) {
        Order order = OrderTestDataBuilder.anOrder().status(status).build();
        orders.add(order);
        return order;
    }
}
