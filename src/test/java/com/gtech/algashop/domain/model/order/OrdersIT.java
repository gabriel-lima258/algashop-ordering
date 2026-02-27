package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.order.OrderPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.order.OrderPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.customer.CustomersPersistenceProvider;
import com.gtech.algashop.infrastructure.persistence.order.OrderPersistenceProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE DE INTEGRAÇÃO do repositório de domínio Orders.
 *
 * --- DIFERENÇA FUNDAMENTAL EM RELAÇÃO AO OrderJpaEntityRepositoryIT ---
 *
 * OrderJpaEntityRepositoryIT testa a INFRAESTRUTURA isolada:
 *   → usa OrderJpaEntityRepository (JPA direto) com OrderPersistenceEntity
 *   → valida que o mapeamento banco ↔ entidade JPA funciona
 *
 * OrdersIT testa o CONTRATO DO DOMÍNIO com infraestrutura real:
 *   → usa a interface Orders (repositório de domínio) com Order (Aggregate Root)
 *   → valida que o ciclo completo domínio → infraestrutura → banco → infraestrutura → domínio funciona
 *   → é o teste mais próximo de como a aplicação real vai usar o repositório
 *
 * --- @DataJpaTest ---
 * Sobe apenas o contexto JPA. Sem este slice, precisaríamos do contexto completo do Spring,
 * que é muito mais lento (sobe todos os beans, conexões, etc.).
 *
 * --- @Import(OrderPersistenceProvider.class) ---
 * @DataJpaTest não registra @Component automaticamente (apenas JPA beans).
 * Por isso precisamos importar explicitamente o OrderPersistenceProvider, que é o
 * adapter que implementa a interface Orders usando JPA.
 * O Spring então consegue injetar Orders via construtor, pois OrderPersistenceProvider
 * é o único bean que implementa a interface Orders no contexto de teste.
 */
@DataJpaTest
@Import({
        OrderPersistenceProvider.class,
        OrderPersistenceEntityAssembler.class,
        OrderPersistenceEntityDisassembler.class,
        CustomersPersistenceProvider.class,
        CustomerPersistenceEntityAssembler.class,
        CustomerPersistenceEntityDisassembler.class
})
class OrdersIT {

    // Injetado como Orders (interface de domínio), não como OrderPersistenceProvider.
    // Isso replica exatamente como a aplicação real usaria o repositório.
    // Substituir por um fake seria trivial — basta trocar o @Import.
    private Orders orders;
    private Customers customers;

    @Autowired
    public OrdersIT(Orders orders, Customers customers) {
        this.orders = orders;
        this.customers = customers;
    }

    @BeforeEach
    void setUp() {
        if (!customers.exists(CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID)) {
            customers.add(
                    CustomerTestDataBuilder.existingCustomer().build()
            );
        }
    }

    /**
     * Verifica o ciclo completo: persistir um Aggregate e recuperá-lo de volta.
     *
     * Este teste valida toda a cadeia:
     *   1. Order (domínio) → add() → OrderPersistenceProvider mapeia → JPA salva no banco
     *   2. ofId() → JPA busca no banco → OrderPersistenceProvider mapeia de volta → Order (domínio)
     *   3. O Order recuperado tem os mesmos dados que o Order original
     *
     * Usamos OrderTestDataBuilder para criar um Order válido sem repetir código de setup.
     * O assertThat(...).satisfies() verifica múltiplos campos de uma vez de forma legível.
     *
     * NOTA: Este teste vai falhar enquanto ofId() retornar Optional.empty(),
     * pois o mapeamento de volta (PersistenceEntity → Order) ainda não está implementado
     * no OrderPersistenceProvider.
     */
    @Test
    void shouldPersistAndFind() {
        Order originalOrder = OrderTestDataBuilder.anOrder().build();
        OrderId orderId = originalOrder.id();

        orders.add(originalOrder);

        Optional<Order> possibleOrder = orders.ofId(orderId);

        assertThat(possibleOrder).isPresent();

        Order savedOrder = possibleOrder.get();

        // Verifica que todos os campos do domínio foram preservados após a ida e volta ao banco
        assertThat(savedOrder).satisfies(
                s -> assertThat(s.id()).isEqualTo(orderId),
                s -> assertThat(s.customerId()).isEqualTo(originalOrder.customerId()),
                s -> assertThat(s.totalAmount()).isEqualTo(originalOrder.totalAmount()),
                s -> assertThat(s.totalQuantity()).isEqualTo(originalOrder.totalQuantity()),
                s -> assertThat(s.placedAt()).isEqualTo(originalOrder.placedAt()),
                s -> assertThat(s.paidAt()).isEqualTo(originalOrder.paidAt()),
                s -> assertThat(s.canceledAt()).isEqualTo(originalOrder.canceledAt()),
                s -> assertThat(s.readyAt()).isEqualTo(originalOrder.readyAt()),
                s -> assertThat(s.status()).isEqualTo(originalOrder.status()),
                s -> assertThat(s.paymentMethod()).isEqualTo(originalOrder.paymentMethod())
        );
    }

    @Test
    void shouldUpdateExisitingOrder() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        orders.add(order);

        order = orders.ofId(order.id()).orElseThrow();
        order.markAsPaid();

        orders.add(order);

        order = orders.ofId(order.id()).orElseThrow();

        Assertions.assertThat(order.isPaid()).isTrue();
    }

    // teste de problemas de concorrencia quando uma persistencia compete com a outra
    @Test
    void shouldNotAllowStaleUpdates() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        // persiste no banco
        orders.add(order);

        Order order1 = orders.ofId(order.id()).orElseThrow();
        Order order2 = orders.ofId(order.id()).orElseThrow();

        order1.markAsPaid();
        orders.add(order1);

        order2.markAsCanceled();

        Assertions.assertThatExceptionOfType(ObjectOptimisticLockingFailureException.class)
                .isThrownBy(() -> orders.add(order2));

        Order savedOrder = orders.ofId(order.id()).orElseThrow();

        Assertions.assertThat(savedOrder.canceledAt()).isNull();
        Assertions.assertThat(savedOrder.paidAt()).isNotNull();
    }

    @Test
    void shouldCountExistingOrders() {
        Assertions.assertThat(orders.count()).isZero();

        Order order1 = OrderTestDataBuilder.anOrder().build();
        Order order2 = OrderTestDataBuilder.anOrder().build();

        orders.add(order1);
        orders.add(order2);

        Assertions.assertThat(orders.count()).isEqualTo(2L);
    }

    @Test
    void shouldReturnIfOrderExists() {
        Order order = OrderTestDataBuilder.anOrder().build();
        orders.add(order);

        Assertions.assertThat(orders.exists(order.id())).isTrue();
        Assertions.assertThat(orders.exists(new OrderId())).isFalse();
    }

    @Test
    void shouldListExistingOrdersByYear() {
        orders.add(
                OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build()
        );
        orders.add(
                OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build()
        );
        orders.add(
                OrderTestDataBuilder.anOrder().status(OrderStatus.CANCELED).build()
        );
        orders.add(
                OrderTestDataBuilder.anOrder().status(OrderStatus.DRAFT).build()
        );

        CustomerId customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

        List<Order> listedOrders = this.orders.placedByCustomerInYear(customerId, Year.now());
        Assertions.assertThat(listedOrders).isNotEmpty();
        Assertions.assertThat(listedOrders.size()).isEqualTo(2);

        listedOrders = orders.placedByCustomerInYear(customerId, Year.now().minusYears(1));
        Assertions.assertThat(listedOrders).isEmpty();

        listedOrders = orders.placedByCustomerInYear(new CustomerId(), Year.now());
        Assertions.assertThat(listedOrders).isEmpty();
    }

    @Test
    void shouldReturnTotalSoldByCustomer() {
        Order order1 = OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).build();
        Order order2 = OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).build();

        orders.add(order1);
        orders.add(order2);

        // valores que devem ser ignorados
        orders.add(
                OrderTestDataBuilder.anOrder().status(OrderStatus.CANCELED).build()
        );
        orders.add(
                OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build()
        );

        // valor esperado
        Money expectedTotalAmount = order1.totalAmount().add(order2.totalAmount());
        CustomerId customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

        Assertions.assertThat(orders.totalSoldForCustomer(customerId)).isEqualTo(expectedTotalAmount);

        // invalidos
        Assertions.assertThat(orders.totalSoldForCustomer(new CustomerId())).isEqualTo(Money.ZERO);
    }

    @Test
    void shouldReturnSalesQuantityByCustomer() {
        Order order1 = OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).build();
        Order order2 = OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).build();

        orders.add(order1);
        orders.add(order2);

        // valores que devem ser ignorados
        orders.add(
                OrderTestDataBuilder.anOrder().status(OrderStatus.CANCELED).build()
        );
        orders.add(
                OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build()
        );

        CustomerId customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

        Assertions.assertThat(orders.salesQuantityByCustomerInYear(customerId, Year.now())).isEqualTo(2L);
        Assertions.assertThat(orders.salesQuantityByCustomerInYear(customerId, Year.now().minusYears(1))).isZero();
    }
}
