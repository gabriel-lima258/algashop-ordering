package com.gtech.algashop.application.shoppingcart.management;

import com.gtech.algashop.domain.model.costumer.CustomerAlreadyHaveShoppingCartException;
import com.gtech.algashop.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.order.OrderCanceledEvent;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import com.gtech.algashop.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.domain.model.product.ProductOutOfStockException;
import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import com.gtech.algashop.domain.model.shoppingcart.*;
import com.gtech.algashop.infrastructure.listerner.order.OrderEventListener;
import com.gtech.algashop.infrastructure.listerner.shoppingcart.ShoppingCartEventListener;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@SpringBootTest
@Transactional
class ShoppingCartManagementApplicationServiceIT {

    @Autowired
    private ShoppingCartManagementApplicationService service;

    @Autowired
    private Customers customers;

    @Autowired
    private ShoppingCarts shoppingCarts;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    @MockitoSpyBean
    private ShoppingCartEventListener shoppingCartEventListener;

    // =========================================================
    // addItem
    // =========================================================

    @Test
    void shouldAddItemToShoppingCart() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCart(customerId, false);
        Product product = ProductTestDataBuilder.aProduct().build();
        Mockito.when(productCatalogService.ofId(product.id())).thenReturn(Optional.of(product));

        ShoppingCartItemInput input = ShoppingCartItemInput.builder()
                .shoppingCartId(cart.id().value())
                .productId(product.id().value())
                .quantity(2)
                .build();

        service.addItem(input);

        ShoppingCart updated = shoppingCarts.ofId(cart.id()).orElseThrow();
        Assertions.assertThat(updated.items()).hasSize(1);
        Assertions.assertThat(updated.totalItems().quantity()).isEqualTo(2);

        Mockito.verify(shoppingCartEventListener, Mockito.times(1))
                .listen(Mockito.any(ShoppingCartItemAddedEvent.class));
    }

    @Test
    void shouldThrowShoppingCartNotFoundWhenAddingItemToNonExistentCart() {
        ShoppingCartItemInput input = ShoppingCartItemInput.builder()
                .shoppingCartId(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        Assertions.assertThatThrownBy(() -> service.addItem(input))
                .isInstanceOf(ShoppingCartNotFound.class);
    }

    @Test
    void shouldThrowProductNotFoundWhenProductDoesNotExist() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCart(customerId, false);
        // mock returns Optional.empty() by default for any un-stubbed call

        ShoppingCartItemInput input = ShoppingCartItemInput.builder()
                .shoppingCartId(cart.id().value())
                .productId(UUID.randomUUID())
                .quantity(1)
                .build();

        Assertions.assertThatThrownBy(() -> service.addItem(input))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    void shouldThrowProductOutOfStockExceptionWhenProductIsOutOfStock() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCart(customerId, false);
        Product outOfStock = ProductTestDataBuilder.aProductUnavailable().build();
        Mockito.when(productCatalogService.ofId(outOfStock.id())).thenReturn(Optional.of(outOfStock));

        ShoppingCartItemInput input = ShoppingCartItemInput.builder()
                .shoppingCartId(cart.id().value())
                .productId(outOfStock.id().value())
                .quantity(1)
                .build();

        Assertions.assertThatThrownBy(() -> service.addItem(input))
                .isInstanceOf(ProductOutOfStockException.class);
    }

    // =========================================================
    // createNew
    // =========================================================

    @Test
    void shouldCreateNewShoppingCart() {
        CustomerId customerId = persistCustomer();

        UUID cartId = service.createNew(customerId.value());

        Assertions.assertThat(cartId).isNotNull();
        Assertions.assertThat(shoppingCarts.ofId(new ShoppingCartId(cartId))).isPresent();

        Mockito.verify(shoppingCartEventListener, Mockito.times(1))
                .listen(Mockito.any(ShoppingCartCreatedEvent.class));
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCreatingCartForNonExistentCustomer() {
        UUID nonExistentCustomerId = UUID.randomUUID();

        Assertions.assertThatThrownBy(() -> service.createNew(nonExistentCustomerId))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void shouldThrowCustomerAlreadyHaveShoppingCartExceptionWhenCustomerAlreadyHasCart() {
        CustomerId customerId = persistCustomer();
        persistCart(customerId, false);

        Assertions.assertThatThrownBy(() -> service.createNew(customerId.value()))
                .isInstanceOf(CustomerAlreadyHaveShoppingCartException.class);
    }

    // =========================================================
    // removeItem
    // =========================================================

    @Test
    void shouldRemoveItemFromShoppingCart() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCart(customerId, true);
        int initialItemCount = cart.items().size();
        ShoppingCartItem itemToRemove = cart.items().iterator().next();

        service.removeItem(cart.id().value(), itemToRemove.id().toString());

        ShoppingCart updated = shoppingCarts.ofId(cart.id()).orElseThrow();
        Assertions.assertThat(updated.items()).hasSize(initialItemCount - 1);

        Mockito.verify(shoppingCartEventListener, Mockito.times(1))
                .listen(Mockito.any(ShoppingCartItemRemovedEvent.class));
    }

    @Test
    void shouldThrowShoppingCartNotFoundWhenRemovingItemFromNonExistentCart() {
        String itemId = new ShoppingCartItemId().toString();

        Assertions.assertThatThrownBy(() ->
                service.removeItem(UUID.randomUUID(), itemId))
                .isInstanceOf(ShoppingCartNotFound.class);
    }

    @Test
    void shouldThrowShoppingCartDoesNotContainItemExceptionWhenItemDoesNotExist() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCart(customerId, false);
        String nonExistentItemId = new ShoppingCartItemId().toString();

        Assertions.assertThatThrownBy(() ->
                service.removeItem(cart.id().value(), nonExistentItemId))
                .isInstanceOf(ShoppingCartDoesNotContainItemException.class);
    }

    // =========================================================
    // empty
    // =========================================================

    @Test
    void shouldEmptyShoppingCart() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCart(customerId, true);
        Assertions.assertThat(cart.items()).isNotEmpty();

        service.empty(cart.id().value());

        ShoppingCart updated = shoppingCarts.ofId(cart.id()).orElseThrow();
        Assertions.assertThat(updated.items()).isEmpty();
        Assertions.assertThat(updated.totalItems().quantity()).isZero();

        Mockito.verify(shoppingCartEventListener, Mockito.times(1))
                .listen(Mockito.any(ShoppingCartEmptiedEvent.class));
    }

    @Test
    void shouldThrowShoppingCartNotFoundWhenEmptyingNonExistentCart() {
        Assertions.assertThatThrownBy(() -> service.empty(UUID.randomUUID()))
                .isInstanceOf(ShoppingCartNotFound.class);
    }

    // =========================================================
    // delete
    // =========================================================

    @Test
    void shouldDeleteShoppingCart() {
        CustomerId customerId = persistCustomer();
        ShoppingCart cart = persistCart(customerId, false);

        service.delete(cart.id().value());

        Assertions.assertThat(shoppingCarts.ofId(cart.id())).isEmpty();
    }

    @Test
    void shouldThrowShoppingCartNotFoundWhenDeletingNonExistentCart() {
        Assertions.assertThatThrownBy(() -> service.delete(UUID.randomUUID()))
                .isInstanceOf(ShoppingCartNotFound.class);
    }

    // =========================================================
    // Helpers
    // =========================================================

    private CustomerId persistCustomer() {
        CustomerId customerId = new CustomerId();
        customers.add(CustomerTestDataBuilder.existingCustomer().id(customerId).build());
        return customerId;
    }

    private ShoppingCart persistCart(CustomerId customerId, boolean withItems) {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart()
                .customerId(customerId)
                .withItems(withItems)
                .build();
        shoppingCarts.add(cart);
        return cart;
    }
}
