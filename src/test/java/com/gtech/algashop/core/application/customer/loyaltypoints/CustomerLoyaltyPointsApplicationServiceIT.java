package com.gtech.algashop.core.application.customer.loyaltypoints;

import com.gtech.algashop.core.application.AbstractIntegrationTest;
import com.gtech.algashop.core.application.customer.management.CustomerInputTestDataBuilder;
import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.ports.in.customer.ForManagingCustomer;
import com.gtech.algashop.core.ports.in.customer.CustomerOutput;
import com.gtech.algashop.core.ports.in.customer.ForQueryCustomers;
import com.gtech.algashop.core.domain.model.costumer.*;
import com.gtech.algashop.core.domain.model.order.*;
import com.gtech.algashop.core.ports.in.customer.ForAddingLoyaltyPoints;
import com.gtech.algashop.infrastructure.adapters.in.listerner.customer.CustomerEventListener;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

class CustomerLoyaltyPointsApplicationServiceIT extends AbstractIntegrationTest {

    @Autowired
    private ForAddingLoyaltyPoints loyaltyPointsApplicationService;

    // bean real (OAuth2SecurityCheckApplicationServiceImpl) lendo a identidade do @WithMockJwt
    @Autowired
    private SecurityCheckApplicationService securityCheck;

    @Autowired
    private ForManagingCustomer customerManagementApplicationService;

    @Autowired
    private Customers customers;

    @Autowired
    private Orders orders;

    @Autowired
    private ForQueryCustomers customerQueryService;

    @MockitoBean
    private CustomerEventListener customerEventListener;

    /**
     * Default READY order total:
     *   1x Notebook Max Pro (R$4500.00) + 1x 16GB RAM (R$200.00) + shipping (R$10.00) = R$4710.00
     * Points: floor(4710 / 1000) * 5 = 4 * 5 = 20 points
     */
    @Test
    void shouldAddLoyaltyPointsSuccessfully() {
        UUID customerId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());

        Order order = OrderTestDataBuilder.anOrder()
                .customerId(new CustomerId(customerId))
                .status(OrderStatus.READY)
                .build();
        orders.add(order);

        loyaltyPointsApplicationService.addLoyaltyPoints(customerId, order.id().toString());

        CustomerOutput output = customerQueryService.findById(customerId);
        Assertions.assertThat(output.getLoyaltyPoints()).isEqualTo(20);
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        // Order must reference a real customer to satisfy the FK constraint,
        // but we query addLoyaltyPoints with a different, non-existent UUID.
        UUID realCustomerId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());

        Order order = OrderTestDataBuilder.anOrder()
                .customerId(new CustomerId(realCustomerId))
                .status(OrderStatus.READY)
                .build();
        orders.add(order);

        UUID nonExistentCustomerId = UUID.randomUUID();

        Assertions.assertThatThrownBy(() ->
                loyaltyPointsApplicationService.addLoyaltyPoints(
                        nonExistentCustomerId, order.id().toString()))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void shouldThrowOrderNotFoundExceptionWhenOrderDoesNotExist() {
        UUID customerId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());

        String nonExistentOrderId = new OrderId().toString();

        Assertions.assertThatThrownBy(() ->
                loyaltyPointsApplicationService.addLoyaltyPoints(customerId, nonExistentOrderId))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldThrowCustomerArchivedExceptionWhenCustomerIsArchived() {
        UUID customerId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());

        Order order = OrderTestDataBuilder.anOrder()
                .customerId(new CustomerId(customerId))
                .status(OrderStatus.READY)
                .build();
        orders.add(order);

        customerManagementApplicationService.archive(customerId);

        Assertions.assertThatThrownBy(() ->
                loyaltyPointsApplicationService.addLoyaltyPoints(
                        customerId, order.id().toString()))
                .isInstanceOf(CustomerArchivedException.class);
    }

    @Test
    void shouldThrowOrderNotBelongsToCustomerExceptionWhenOrderBelongsToAnotherCustomer() {
        UUID customerAId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());
        // o cliente B precisa de outro id: repetir o id do token sobrescreveria o cliente A
        UUID customerBId = customerManagementApplicationService.create(
                UUID.randomUUID(), CustomerInputTestDataBuilder.aCustomer().email("janedoe@email.com").build());

        Order orderForCustomerB = OrderTestDataBuilder.anOrder()
                .customerId(new CustomerId(customerBId))
                .status(OrderStatus.READY)
                .build();
        orders.add(orderForCustomerB);

        Assertions.assertThatThrownBy(() ->
                loyaltyPointsApplicationService.addLoyaltyPoints(
                        customerAId, orderForCustomerB.id().toString()))
                .isInstanceOf(OrderNotBelongsToCustomerException.class);
    }

    @Test
    void shouldThrowCanAddLoyaltyPointsOrderIsNotReadyExceptionWhenOrderIsNotReady() {
        UUID customerId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());

        Order order = OrderTestDataBuilder.anOrder()
                .customerId(new CustomerId(customerId))
                .status(OrderStatus.PLACED)
                .build();
        orders.add(order);

        Assertions.assertThatThrownBy(() ->
                loyaltyPointsApplicationService.addLoyaltyPoints(
                        customerId, order.id().toString()))
                .isInstanceOf(CanAddLoyaltyPointsOrderIsNotReadyException.class);
    }

    @Test
    void shouldNotAddPointsWhenOrderTotalIsBelowThreshold() {
        UUID customerId = customerManagementApplicationService.create(
                securityCheck.getAuthenticatedUserId(), CustomerInputTestDataBuilder.aCustomer().build());

        // withItems(false) → total = R$0.00 (below R$1000 threshold) → 0 points
        Order order = OrderTestDataBuilder.anOrder()
                .customerId(new CustomerId(customerId))
                .withItems(false)
                .status(OrderStatus.READY)
                .build();
        orders.add(order);

        loyaltyPointsApplicationService.addLoyaltyPoints(customerId, order.id().toString());

        CustomerOutput output = customerQueryService.findById(customerId);
        Assertions.assertThat(output.getLoyaltyPoints()).isZero();
    }
}
