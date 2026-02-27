package com.gtech.algashop.domain.model.customer;

import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.commons.Email;
import com.gtech.algashop.domain.model.commons.FullName;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.customer.CustomersPersistenceProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;

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
        CustomersPersistenceProvider.class,
        CustomerPersistenceEntityAssembler.class,
        CustomerPersistenceEntityDisassembler.class
})
class CustomersIT {

    private Customers customers;

    @Autowired
    public CustomersIT(Customers customers) {
        this.customers = customers;
    }

    @Test
    void shouldPersistAndFind() {
        Customer originalCustomer = CustomerTestDataBuilder.brandNewCustomer().build();
        CustomerId customerId = originalCustomer.id();
        customers.add(originalCustomer);

        Optional<Customer> possibleCustomer = customers.ofId(customerId);

        assertThat(possibleCustomer).isPresent();

        Customer savedCustomer = possibleCustomer.get();

        assertThat(savedCustomer).satisfies(
                s -> assertThat(s.id()).isEqualTo(customerId)
        );
    }

    @Test
    void shouldUpdateExistingCustomer() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        customers.add(customer);

        customer = customers.ofId(customer.id()).orElseThrow();
        customer.archive();

        customers.add(customer);

        Customer savedCustomer = customers.ofId(customer.id()).orElseThrow();

        Assertions.assertThat(savedCustomer.archivedAt()).isNotNull();
        Assertions.assertThat(savedCustomer.isArchived()).isTrue();

    }

    @Test
    void shouldNotAllowStaleUpdates() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        customers.add(customer);

        Customer customerT1 = customers.ofId(customer.id()).orElseThrow();
        Customer customerT2 = customers.ofId(customer.id()).orElseThrow();

        customerT1.archive();
        customers.add(customerT1);

        customerT2.changeName(new FullName("Alex","Silva"));

        Assertions.assertThatExceptionOfType(ObjectOptimisticLockingFailureException.class)
                .isThrownBy(()-> customers.add(customerT2));

        Customer savedCustomer = customers.ofId(customer.id()).orElseThrow();

        Assertions.assertThat(savedCustomer.archivedAt()).isNotNull();
        Assertions.assertThat(savedCustomer.isArchived()).isTrue();

    }

    @Test
    void shouldCountExistingOrders() {
        Assertions.assertThat(customers.count()).isZero();

        Customer customer1 = CustomerTestDataBuilder.brandNewCustomer().build();
        customers.add(customer1);

        Customer customer2 = CustomerTestDataBuilder.brandNewCustomer().build();
        customers.add(customer2);

        Assertions.assertThat(customers.count()).isEqualTo(2L);
    }

    @Test
    void shouldReturnValidateIfOrderExists() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        customers.add(customer);

        Assertions.assertThat(customers.exists(customer.id())).isTrue();
        Assertions.assertThat(customers.exists(new CustomerId())).isFalse();
    }

    @Test
    void shouldFindByEmail() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        customers.add(customer);

        Optional<Customer> customerOptional = customers.ofEmail(customer.email());

        Assertions.assertThat(customerOptional).isPresent();
    }

    @Test
    void shouldNotFindByEmailIfNoCustomerExistsWithEmail() {
        Optional<Customer> customerOptional = customers.ofEmail(new Email(UUID.randomUUID() + "@email.com"));
        Assertions.assertThat(customerOptional).isNotPresent();
    }

    @Test
    void shouldReturnIfEmailIsInUse() {
        Customer customer = CustomerTestDataBuilder.brandNewCustomer().build();
        customers.add(customer);

        // verifica se email é valido e unico, caso valido retorna true
        Assertions.assertThat(customers.isEmailUnique(customer.email(), customer.id())).isTrue();
        Assertions.assertThat(customers.isEmailUnique(customer.email(), new CustomerId())).isFalse();
        Assertions.assertThat(customers.isEmailUnique(new Email("gabriel@gmail.com"), new CustomerId())).isTrue();
    }
}
