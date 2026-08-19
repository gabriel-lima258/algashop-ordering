package com.gtech.algashop.core.application.order.query;

import com.gtech.algashop.core.application.AbstractIntegrationTest;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.utils.TestAuthentications;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;
import com.gtech.algashop.core.domain.model.costumer.Customers;
import com.gtech.algashop.core.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderStatus;
import com.gtech.algashop.core.domain.model.order.OrderTestDataBuilder;
import com.gtech.algashop.core.domain.model.order.Orders;
import com.gtech.algashop.core.ports.in.order.ForQueryOrders;
import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.ports.in.order.OrderFilter;
import com.gtech.algashop.core.ports.out.order.OrderSummaryOutput;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

class OrderQueryServiceIT extends AbstractIntegrationTest {

    /**
     * Fase 27: OrderQueryService.filter() passou a SOBRESCREVER o customerId do filtro quando
     * quem consulta e um CUSTOMER - ele so enxerga os proprios pedidos. Esta suite testa a
     * consulta ampla (filtrar por status, por cliente arbitrario, ordenar), que e operacao de
     * back-office. Por isso autentica como MANAGER, para quem o filtro vale como escrito.
     *
     * A restricao do CUSTOMER tem prova propria, no fluxo real - ver o documento de RBAC.
     */
    @BeforeEach
    void authenticateAsManager() {
        SecurityContextHolder.getContext().setAuthentication(
                TestAuthentications.manager(UUID.randomUUID()));
    }

    @Autowired
    private ForQueryOrders queryService;

    @Autowired
    private Orders orders;

    @Autowired
    private Customers customers;

    @Test
    void shouldFindById() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        customers.add(customer);

        Order order = OrderTestDataBuilder.anOrder().customerId(customer.id()).build();
        orders.add(order);

        OrderDetailOutput output = queryService.findById(order.id().toString());

        Assertions.assertThat(output)
                .extracting(
                        OrderDetailOutput::getId,
                        OrderDetailOutput::getTotalAmount
                ).containsExactly(
                        order.id().toString(),
                        order.totalAmount().money()
                );
    }

    @Test
    void shouldFilterByPage() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        customers.add(customer);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).customerId(customer.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).customerId(customer.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.READY).customerId(customer.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.CANCELED).customerId(customer.id()).build());

        Page<OrderSummaryOutput> page = queryService.filter(new OrderFilter(3, 0));

        Assertions.assertThat(page.getTotalPages()).isEqualTo(2);
        Assertions.assertThat(page.getTotalElements()).isEqualTo(5);
        Assertions.assertThat(page.getNumberOfElements()).isEqualTo(3);
    }

    @Test
    void shouldFilterByCustomerId() {
        Customer customer1 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer1);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.READY).customerId(customer1.id()).build());

        Customer customer2 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer2);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).customerId(customer2.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer2.id()).build());

        OrderFilter filter = new OrderFilter();
        filter.setCustomerId(customer1.id().value());

        Page<OrderSummaryOutput> page = queryService.filter(filter);

        Assertions.assertThat(page.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(page.getTotalElements()).isEqualTo(4);
        Assertions.assertThat(page.getNumberOfElements()).isEqualTo(4);
    }

    @Test
    void shouldFilterByMultipleParams() {
        Customer customer1 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer1);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).customerId(customer1.id()).build());
        Order order1 = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer1.id()).build();
        orders.add(order1);
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.READY).customerId(customer1.id()).build());

        Customer customer2 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer2);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).customerId(customer2.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer2.id()).build());

        OrderFilter filter = new OrderFilter();
        filter.setCustomerId(customer1.id().value());
        filter.setStatus(OrderStatus.PLACED.toString().toLowerCase());
        filter.setTotalAmountFrom(order1.totalAmount().money());

        Page<OrderSummaryOutput> page = queryService.filter(filter);

        Assertions.assertThat(page.getTotalPages()).isEqualTo(1);
        Assertions.assertThat(page.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(page.getNumberOfElements()).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptyPageWhenOrderIdIsInvalid() {
        Customer customer1 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer1);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).customerId(customer1.id()).build());
        Order order1 = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer1.id()).build();
        orders.add(order1);
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.READY).customerId(customer1.id()).build());

        Customer customer2 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer2);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).customerId(customer2.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer2.id()).build());

        OrderFilter filter = new OrderFilter();
        filter.setOrderId("ABC");

        Page<OrderSummaryOutput> page = queryService.filter(filter);

        Assertions.assertThat(page.getTotalPages()).isZero();
        Assertions.assertThat(page.getTotalElements()).isZero();
        Assertions.assertThat(page.getNumberOfElements()).isZero();
    }

    @Test
    void shouldOrderByStatusAndAscSortDirection() {
        Customer customer1 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer1);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.READY).customerId(customer1.id()).build());

        Customer customer2 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer2);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.CANCELED).customerId(customer2.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer2.id()).build());

        OrderFilter filter = new OrderFilter();
        filter.setSortByProperty(OrderFilter.SortType.STATUS);
        filter.setSortDirection(Sort.Direction.ASC); // retorna o valor ASC -> CANCELED

        Page<OrderSummaryOutput> page = queryService.filter(filter);

        Assertions.assertThat(page.getContent().getFirst().getStatus()).isEqualTo(OrderStatus.CANCELED.toString());
    }

    @Test
    void shouldOrderByStatusAndDescSortDirection() {
        Customer customer1 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer1);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).customerId(customer1.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.READY).customerId(customer1.id()).build());

        Customer customer2 = CustomerTestDataBuilder.existingCustomer().id(new CustomerId()).build();
        customers.add(customer2);

        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.CANCELED).customerId(customer2.id()).build());
        orders.add(OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).customerId(customer2.id()).build());

        OrderFilter filter = new OrderFilter();
        filter.setSortByProperty(OrderFilter.SortType.STATUS);
        filter.setSortDirection(Sort.Direction.DESC); // retorna o valor DESC -> READY

        Page<OrderSummaryOutput> page = queryService.filter(filter);

        Assertions.assertThat(page.getContent().getFirst().getStatus()).isEqualTo(OrderStatus.READY.toString());
    }
}