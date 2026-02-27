package com.gtech.algashop.infrastructure.persistence.assembler;

import com.gtech.algashop.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartItem;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartItemTestDataBuilder;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartTestDataBuilder;
import com.gtech.algashop.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartPersistenceEntityTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartItemPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerJpaEntityRepository;
import com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntityAssembler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ShoppingCartPersistenceEntityAssemblerTest {

    @Mock
    private CustomerJpaEntityRepository customerJpaEntityRepository;

    @InjectMocks
    private ShoppingCartPersistenceEntityAssembler assembler;

    @BeforeEach
    void setUp() {
        // lenient: alguns testes testam fromDomain(ShoppingCartItem) que não chama o repositório de cliente
        Mockito.lenient().when(customerJpaEntityRepository.getReferenceById(Mockito.any(UUID.class)))
                .then(a -> {
                    UUID customerId = a.getArgument(0, UUID.class);
                    return CustomerPersistenceEntityTestDataBuilder.existingCustomer().id(customerId).build();
                });
    }

    @Test
    void shouldConvertFromDomain() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().build();

        ShoppingCartPersistenceEntity entity = assembler.fromDomain(cart);

        assertThat(entity.getId()).isEqualTo(cart.id().value());
        assertThat(entity.getCustomerId()).isEqualTo(cart.customerId().value());
        assertThat(entity.getTotalAmount()).isEqualTo(cart.totalAmount().money());
        assertThat(entity.getTotalItems()).isEqualTo(cart.totalItems().quantity());
        assertThat(entity.getCreatedAt()).isEqualTo(cart.createdAt());
        assertThat(entity.getVersion()).isEqualTo(cart.version());
    }

    @Test
    void shouldReturnSameInstanceOnMerge() {
        // merge() deve modificar e retornar a MESMA instância, não criar uma nova.
        // Crítico para JPA: o EntityManager rastreia mudanças apenas em objetos gerenciados.
        ShoppingCartPersistenceEntity existing = new ShoppingCartPersistenceEntity();
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().build();

        ShoppingCartPersistenceEntity result = assembler.merge(existing, cart);

        assertThat(result).isSameAs(existing);
    }

    @Test
    void shouldOverwriteExistingEntityFieldsOnMerge() {
        ShoppingCart originalCart = ShoppingCartTestDataBuilder.aShoppingCart().build();
        ShoppingCartPersistenceEntity existing = assembler.fromDomain(originalCart);

        ShoppingCart updatedCart = ShoppingCartTestDataBuilder.aShoppingCart()
                .customerId(CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID)
                .build();

        assembler.merge(existing, updatedCart);

        assertThat(existing.getId()).isEqualTo(updatedCart.id().value());
        assertThat(existing.getTotalAmount()).isEqualTo(updatedCart.totalAmount().money());
        assertThat(existing.getTotalItems()).isEqualTo(updatedCart.totalItems().quantity());
    }

    @Test
    void givenCartWithItemsShouldMapItems() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().withItems(true).build();

        ShoppingCartPersistenceEntity entity = assembler.fromDomain(cart);

        assertThat(entity.getItems()).isNotEmpty();
        assertThat(entity.getItems()).hasSize(cart.items().size());
    }

    @Test
    void givenCartWithNoItemsShouldProduceEmptyItems() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().withItems(false).build();

        ShoppingCartPersistenceEntity entity = assembler.fromDomain(cart);

        assertThat(entity.getItems()).isEmpty();
    }

    @Test
    void givenCartWithNoItemsShouldClearExistingPersistenceItems() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().withItems(false).build();
        ShoppingCartPersistenceEntity existingEntity = ShoppingCartPersistenceEntityTestDataBuilder.existingCart().build();

        Assertions.assertThat(cart.items()).isEmpty();
        Assertions.assertThat(existingEntity.getItems()).isNotEmpty();

        assembler.merge(existingEntity, cart);

        assertThat(existingEntity.getItems()).isEmpty();
    }

    @Test
    void givenCartWithItemsWhenMergingEmptyEntityShouldAddItems() {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().withItems(true).build();
        ShoppingCartPersistenceEntity emptyEntity = ShoppingCartPersistenceEntityTestDataBuilder
                .existingCart()
                .items(new HashSet<>())
                .build();

        Assertions.assertThat(cart.items()).isNotEmpty();
        Assertions.assertThat(emptyEntity.getItems()).isEmpty();

        assembler.merge(emptyEntity, cart);

        assertThat(emptyEntity.getItems()).hasSize(cart.items().size());
    }

    @Test
    void givenCartWithItemsWhenMergingShouldReuseExistingItemEntities() {
        // Garante que o merge reutiliza entidades JPA existentes (evita violação de PK)
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().withItems(true).build();

        Set<ShoppingCartItemPersistenceEntity> existingItemEntities = cart.items().stream()
                .map(assembler::fromDomain)
                .collect(Collectors.toSet());

        ShoppingCartPersistenceEntity existingEntity = ShoppingCartPersistenceEntityTestDataBuilder
                .existingCart()
                .items(existingItemEntities)
                .build();

        ShoppingCartItem itemToRemove = cart.items().iterator().next();
        cart.removeItem(itemToRemove.id());

        assembler.merge(existingEntity, cart);

        assertThat(existingEntity.getItems()).hasSize(cart.items().size());
    }

    @Test
    void shouldMapItemScalarFields() {
        ShoppingCartItem item = ShoppingCartItemTestDataBuilder.anItem().build();
        ShoppingCartItemPersistenceEntity itemEntity = assembler.fromDomain(item);

        assertThat(itemEntity.getId()).isEqualTo(item.id().id().toLong());
        assertThat(itemEntity.getProductId()).isEqualTo(item.productId().value());
        assertThat(itemEntity.getProductName()).isEqualTo(item.name().name());
        assertThat(itemEntity.getPrice()).isEqualTo(item.price().money());
        assertThat(itemEntity.getQuantity()).isEqualTo(item.quantity().quantity());
        assertThat(itemEntity.getTotalAmount()).isEqualTo(item.totalAmount().money());
        assertThat(itemEntity.getAvailable()).isEqualTo(item.available());
    }

    @Test
    void shouldSetParentReferenceOnItemsAfterMerge() {
        // Os itens devem ter a referência ao carrinho pai após o merge (para o @ManyToOne)
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().withItems(true).build();
        ShoppingCartPersistenceEntity entity = assembler.fromDomain(cart);

        assertThat(entity.getItems()).allSatisfy(item ->
                assertThat(item.getShoppingCart()).isSameAs(entity)
        );
    }
}
