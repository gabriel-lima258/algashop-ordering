package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.customer.CustomersPersistenceProvider;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TESTE DE INTEGRAÇÃO do repositório de domínio ShoppingCarts.
 *
 * Testa o CONTRATO DO DOMÍNIO com infraestrutura real:
 *   → usa a interface ShoppingCarts (repositório de domínio) com ShoppingCart (Aggregate Root)
 *   → valida o ciclo completo: domínio → infraestrutura → banco → infraestrutura → domínio
 *
 * O Spring injeta ShoppingCartPersistenceProvider (único bean que implementa ShoppingCarts)
 * mas os testes dependem apenas da interface ShoppingCarts, garantindo isolamento.
 */
@DataJpaTest
@Import({
        ShoppingCartPersistenceProvider.class,
        ShoppingCartPersistenceEntityAssembler.class,
        ShoppingCartPersistenceEntityDisassembler.class,
        CustomersPersistenceProvider.class,
        CustomerPersistenceEntityAssembler.class,
        CustomerPersistenceEntityDisassembler.class
})
class ShoppingCartsIT {

    // customerId fixo — o mesmo cliente é registrado em @BeforeEach para todos os testes
    private static final CustomerId CUSTOMER_ID = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

    // Injetado como ShoppingCarts (interface de domínio), não como ShoppingCartPersistenceProvider.
    private final ShoppingCarts shoppingCarts;
    private final Customers customers;

    @Autowired
    public ShoppingCartsIT(ShoppingCarts shoppingCarts, Customers customers) {
        this.shoppingCarts = shoppingCarts;
        this.customers = customers;
    }

    @BeforeEach
    void setUp() {
        if (!customers.exists(CUSTOMER_ID)) {
            customers.add(CustomerTestDataBuilder.existingCustomer().build());
        }
    }

    private ShoppingCart newCart() {
        return ShoppingCartTestDataBuilder.aShoppingCart().customerId(CUSTOMER_ID).build();
    }

    @Test
    void shouldPersistAndFind() {
        ShoppingCart original = newCart();
        ShoppingCartId cartId = original.id();

        shoppingCarts.add(original);

        Optional<ShoppingCart> found = shoppingCarts.ofId(cartId);

        assertThat(found).isPresent();

        ShoppingCart saved = found.get();
        assertThat(saved).satisfies(
                s -> assertThat(s.id()).isEqualTo(cartId),
                s -> assertThat(s.customerId()).isEqualTo(CUSTOMER_ID),
                s -> assertThat(s.totalAmount()).isEqualTo(original.totalAmount()),
                s -> assertThat(s.totalItems()).isEqualTo(original.totalItems()),
                s -> assertThat(s.items()).hasSize(original.items().size())
        );
    }

    @Test
    void shouldFindByCustomerId() {
        ShoppingCart cart = newCart();

        shoppingCarts.add(cart);

        Optional<ShoppingCart> found = shoppingCarts.ofCustomer(CUSTOMER_ID);

        assertThat(found).isPresent();
        assertThat(found.get().customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(found.get().items()).isNotEmpty();
    }

    @Test
    void shouldReturnEmptyWhenCartNotFoundByCustomerId() {
        Optional<ShoppingCart> found = shoppingCarts.ofCustomer(CUSTOMER_ID);

        assertThat(found).isEmpty();
    }

    @Test
    void shouldUpdateExistingCart() {
        ShoppingCart cart = newCart();
        shoppingCarts.add(cart);

        cart = shoppingCarts.ofId(cart.id()).orElseThrow();
        int itemsBefore = cart.items().size();

        cart.addItem(ProductTestDataBuilder.aProductRamMemory().build(), new Quantity(1));
        shoppingCarts.add(cart);

        ShoppingCart updated = shoppingCarts.ofId(cart.id()).orElseThrow();
        assertThat(updated.items()).hasSize(itemsBefore + 1);
    }

    @Test
    void shouldNotAllowStaleUpdates() {
        ShoppingCart cart = newCart();
        shoppingCarts.add(cart);

        ShoppingCart cart1 = shoppingCarts.ofId(cart.id()).orElseThrow();
        ShoppingCart cart2 = shoppingCarts.ofId(cart.id()).orElseThrow();

        // cart1 atualiza primeiro
        cart1.addItem(ProductTestDataBuilder.aProductRamMemory().build(), new Quantity(1));
        shoppingCarts.add(cart1);

        // cart2 tenta atualizar com versão desatualizada — deve lançar exceção
        cart2.addItem(ProductTestDataBuilder.aProductRamMemory().build(), new Quantity(2));

        Assertions.assertThatExceptionOfType(ObjectOptimisticLockingFailureException.class)
                .isThrownBy(() -> shoppingCarts.add(cart2));
    }

    @Test
    void shouldRemoveCartByEntity() {
        ShoppingCart cart = newCart();
        shoppingCarts.add(cart);

        assertThat(shoppingCarts.exists(cart.id())).isTrue();

        shoppingCarts.remove(cart);

        assertThat(shoppingCarts.exists(cart.id())).isFalse();
        assertThat(shoppingCarts.ofId(cart.id())).isEmpty();
    }

    @Test
    void shouldRemoveCartById() {
        ShoppingCart cart = newCart();
        shoppingCarts.add(cart);

        assertThat(shoppingCarts.exists(cart.id())).isTrue();

        shoppingCarts.remove(cart.id());

        assertThat(shoppingCarts.exists(cart.id())).isFalse();
    }

    @Test
    void shouldCountExistingCarts() {
        assertThat(shoppingCarts.count()).isZero();

        shoppingCarts.add(newCart());

        assertThat(shoppingCarts.count()).isEqualTo(1L);
    }

    @Test
    void shouldReturnIfCartExists() {
        ShoppingCart cart = newCart();

        assertThat(shoppingCarts.exists(cart.id())).isFalse();

        shoppingCarts.add(cart);

        assertThat(shoppingCarts.exists(cart.id())).isTrue();
        assertThat(shoppingCarts.exists(new ShoppingCartId())).isFalse();
    }
}
