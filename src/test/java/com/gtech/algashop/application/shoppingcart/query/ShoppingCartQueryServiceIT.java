package com.gtech.algashop.application.shoppingcart.query;

import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartNotFound;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartTestDataBuilder;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCarts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest
@Transactional
class ShoppingCartQueryServiceIT {

    @Autowired
    private ShoppingCartQueryService queryService;

    @Autowired
    private ShoppingCarts shoppingCarts;

    @Autowired
    private Customers customers;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    // =====================================================
    //  findById — cenário de sucesso
    // =====================================================

    @Test
    void shouldFindShoppingCartById() {
        Customer customer = persistCustomer();
        ShoppingCart cart = persistCart(customer);

        ShoppingCartOutput output = queryService.findById(cart.id().value());

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(cart.id().value());
        assertThat(output.getCustomerId()).isEqualTo(customer.id().value());
        assertThat(output.getTotalItems()).isEqualTo(cart.totalItems().quantity());
        assertThat(output.getTotalAmount()).isEqualByComparingTo(cart.totalAmount().money());

        assertThat(output.getItems())
                .hasSize(2)
                .extracting(
                        ShoppingCartItemOutput::getName,
                        ShoppingCartItemOutput::getQuantity,
                        ShoppingCartItemOutput::getAvailable
                )
                .containsExactlyInAnyOrder(
                        tuple("Notebook Max Pro", 1, true),
                        tuple("Mouse Pad Gamer", 1, true)
                );
    }

    @Test
    void shouldReturnCorrectItemPricesAndTotals() {
        Customer customer = persistCustomer();
        ShoppingCart cart = persistCart(customer);

        ShoppingCartOutput output = queryService.findById(cart.id().value());

        assertThat(output.getItems())
                .extracting(
                        ShoppingCartItemOutput::getName,
                        ShoppingCartItemOutput::getPrice,
                        ShoppingCartItemOutput::getTotalAmount
                )
                .containsExactlyInAnyOrder(
                        tuple("Notebook Max Pro", new BigDecimal("4500.00"), new BigDecimal("4500.00")),
                        tuple("Mouse Pad Gamer", new BigDecimal("120.00"), new BigDecimal("120.00"))
                );

        assertThat(output.getTotalAmount())
                .isEqualByComparingTo(new BigDecimal("4620.00"));
    }

    // =====================================================
    //  findById — cenário de falha
    // =====================================================

    @Test
    void shouldThrowExceptionWhenFindByIdWithNonExistentId() {
        UUID nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() -> queryService.findById(nonExistentId))
                .isInstanceOf(ShoppingCartNotFound.class);
    }

    // =====================================================
    //  findByCustomerId — cenário de sucesso
    // =====================================================

    @Test
    void shouldFindShoppingCartByCustomerId() {
        Customer customer = persistCustomer();
        ShoppingCart cart = persistCart(customer);

        ShoppingCartOutput output = queryService.findByCustomerId(customer.id().value());

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(cart.id().value());
        assertThat(output.getCustomerId()).isEqualTo(customer.id().value());
        assertThat(output.getTotalItems()).isEqualTo(cart.totalItems().quantity());
        assertThat(output.getTotalAmount()).isEqualByComparingTo(cart.totalAmount().money());
        assertThat(output.getItems()).hasSize(2);
    }

    @Test
    void shouldReturnCorrectCartForSpecificCustomer() {
        Customer customer1 = persistCustomer();
        Customer customer2 = persistCustomer();
        ShoppingCart cart1 = persistCart(customer1);
        ShoppingCart cart2 = persistCartWithoutItems(customer2);

        ShoppingCartOutput output = queryService.findByCustomerId(customer1.id().value());

        assertThat(output.getId()).isEqualTo(cart1.id().value());
        assertThat(output.getCustomerId()).isEqualTo(customer1.id().value());
        assertThat(output.getItems()).hasSize(2);
    }

    // =====================================================
    //  findByCustomerId — cenário de falha
    // =====================================================

    @Test
    void shouldThrowExceptionWhenFindByCustomerIdWithNonExistentCustomer() {
        UUID nonExistentCustomerId = UUID.randomUUID();

        assertThatThrownBy(() -> queryService.findByCustomerId(nonExistentCustomerId))
                .isInstanceOf(ShoppingCartNotFound.class);
    }

    // =====================================================
    //  Cenário com carrinho vazio (sem itens)
    // =====================================================

    @Test
    void shouldFindEmptyShoppingCartById() {
        Customer customer = persistCustomer();
        ShoppingCart cart = persistCartWithoutItems(customer);

        ShoppingCartOutput output = queryService.findById(cart.id().value());

        assertThat(output).isNotNull();
        assertThat(output.getId()).isEqualTo(cart.id().value());
        assertThat(output.getTotalItems()).isZero();
        assertThat(output.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(output.getItems()).isEmpty();
    }

    // =====================================================
    //  Helpers
    // =====================================================

    private Customer persistCustomer() {
        Customer customer = CustomerTestDataBuilder.existingCustomer()
                .id(new CustomerId())
                .build();
        customers.add(customer);
        return customer;
    }

    private ShoppingCart persistCart(Customer customer) {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart()
                .customerId(customer.id())
                .withItems(true)
                .build();
        shoppingCarts.add(cart);
        return cart;
    }

    private ShoppingCart persistCartWithoutItems(Customer customer) {
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart()
                .customerId(customer.id())
                .withItems(false)
                .build();
        shoppingCarts.add(cart);
        return cart;
    }
}
