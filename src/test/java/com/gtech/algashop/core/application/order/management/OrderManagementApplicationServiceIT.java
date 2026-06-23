package com.gtech.algashop.core.application.order.management;

import com.gtech.algashop.core.application.AbstractIntegrationTest;
import com.gtech.algashop.core.application.order.OrderManagementApplicationService;
import com.gtech.algashop.core.ports.in.customer.ForAddingLoyaltyPoints;
import com.gtech.algashop.core.domain.model.costumer.Customers;
import com.gtech.algashop.core.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.core.domain.model.order.*;
import com.gtech.algashop.infrastructure.adapters.in.listerner.order.OrderEventListener;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.UUID;

@Import(OrderEventListener.class)
class OrderManagementApplicationServiceIT extends AbstractIntegrationTest {

    @Autowired
    private OrderManagementApplicationService service;

    @Autowired
    private Orders orders;

    @Autowired
    private Customers customers;

    // verifica se esta sendo chamado
    @MockitoSpyBean
    private OrderEventListener orderEventListener;

    @MockitoSpyBean
    private ForAddingLoyaltyPoints loyaltyPointsApplicationService;


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

        Mockito.verify(orderEventListener, Mockito.times(1))
                .listen(Mockito.any(OrderCanceledEvent.class));
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

        Mockito.verify(orderEventListener, Mockito.times(1))
                .listen(Mockito.any(OrderPaidEvent.class));
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

        Mockito.verify(orderEventListener, Mockito.times(1))
                .listen(Mockito.any(OrderReadyEvent.class));
        Mockito.verify(loyaltyPointsApplicationService, Mockito.times(1))
                .addLoyaltyPoints(
                        Mockito.any(UUID.class),
                        Mockito.any(String.class)
                );
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
