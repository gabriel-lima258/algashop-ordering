package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.product.ProductOutOfStockException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Year;

/**
 * Testes unitários para o BuyNowService.
 *
 * O BuyNowService representa o caso de uso "Comprar Agora": cria um pedido
 * diretamente a partir de um único produto, sem passar pelo fluxo de carrinho.
 * Por não ter dependências de infraestrutura, é testado como POJO puro —
 * sem Spring context, sem Mockito, sem banco de dados.
 *
 * Fluxo interno do buyNow():
 *   1. product.checkOutOfStock()           → rejeita produto fora de estoque
 *   2. Order.draft(customerId)             → cria pedido em estado DRAFT
 *   3. order.changeBilling/Shipping/...    → configura dados do pedido
 *   4. order.addItem(product, quantity)    → adiciona item; valida estoque e quantidade
 *   5. order.markAsPlaced()               → transiciona para PLACED
 */
@ExtendWith(MockitoExtension.class)
class BuyNowServiceTest {

    private BuyNowService buyNowService;

    // injeta a dependencia em service
    @Mock
    private Orders orders;

    // Construção manual no @BeforeEach em vez de @InjectMocks porque:
    // 1. BuyNowService depende de CustomerHaveFreeShippingSpecification, e não diretamente de Orders.
    //    O @InjectMocks não consegue resolver essa cadeia indireta de dependências.
    // 2. A Specification precisa de parâmetros de configuração (minPoints, salesQuantity) que não são mocks —
    //    são valores concretos que definem as regras de negócio para o contexto de teste.
    // 3. Ao construir manualmente, controlamos exatamente os limiares usados nos testes (100 pontos, 2 vendas, 2000 pontos),
    //    garantindo que os cenários de frete grátis sejam determinísticos e independentes de configuração externa.
    @BeforeEach
    void setUp() {
        // aqui definidos valores independentes dos valores em produção, então isolamos o contexto mesmo com valores diferentes
        var specification = new CustomerHaveFreeShippingSpecification(
                orders,
                new LoyaltyPoints(100),
                2L,
                new LoyaltyPoints(2000)
        );
        buyNowService = new BuyNowService(specification);
    }

    /**
     * Cenário feliz: compra de um produto em estoque com quantidade válida.
     *
     * Valores usados para facilitar a verificação dos cálculos:
     *   produto     = notebook a R$4500
     *   quantidade  = 2
     *   item total  = R$4500 x 2 = R$9000
     *   frete       = R$10 (OrderTestDataBuilder.aShipping())
     *   order total = R$9000 + R$10 = R$9010
     */
    @Test
    void shouldCreatePlacedOrderForValidProduct() {
        // Arrange
        Product notebook = ProductTestDataBuilder.aProduct().price(new Money("4500")).build();
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping(); // custo de frete: R$10
        Quantity quantity = new Quantity(2);
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        // Act
        Order order = buyNowService.buyNow(notebook, customer, billing, shipping, quantity, paymentMethod, new CreditCardId());

        // Assert — metadados do pedido
        Assertions.assertThat(order).isNotNull();
        Assertions.assertThat(order.id()).isNotNull();
        Assertions.assertThat(order.customerId()).isEqualTo(customer.id());
        Assertions.assertThat(order.billing()).isEqualTo(billing);
        Assertions.assertThat(order.shipping()).isEqualTo(shipping);
        Assertions.assertThat(order.paymentMethod()).isEqualTo(paymentMethod);

        // O pedido deve sair do buyNow já no estado PLACED
        Assertions.assertThat(order.isPlaced()).isTrue();

        // Assert — produto e quantidade adicionados corretamente
        // buyNow adiciona exatamente 1 tipo de produto, logo deve haver somente 1 OrderItem
        Assertions.assertThat(order.items()).hasSize(1);

        OrderItem orderItem = order.items().iterator().next();
        Assertions.assertThat(orderItem.productId()).isEqualTo(notebook.id());
        Assertions.assertThat(orderItem.price()).isEqualTo(new Money("4500"));
        Assertions.assertThat(orderItem.quantity()).isEqualTo(new Quantity(2));
        Assertions.assertThat(orderItem.totalAmount()).isEqualTo(new Money("4500").multiply(new Quantity(2)));

        // Assert — totais do pedido usando Money (sem BigDecimal bruto)
        // totalQuantity reflete a quantidade do único item adicionado
        Assertions.assertThat(order.totalQuantity()).isEqualTo(new Quantity(2));

        // totalAmount = item total + frete
        Money expectedTotal = new Money("4500").multiply(new Quantity(2)).add(new Money("10"));
        Assertions.assertThat(order.totalAmount()).isEqualTo(expectedTotal);
    }

    /**
     * Cenário de erro: compra bloqueada quando o produto está fora de estoque.
     *
     * product.checkOutOfStock() é a primeira operação do buyNow(), garantindo
     * que nenhum Order seja criado parcialmente antes da exceção ser lançada.
     */
    @Test
    void shouldThrowExceptionWhenProductIsOutOfStock() {
        // Arrange — produto criado explicitamente com inStock=false
        Product unavailableProduct = ProductTestDataBuilder.aProductUnavailable().build();
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping();

        // Act + Assert
        Assertions.assertThatExceptionOfType(ProductOutOfStockException.class)
                .isThrownBy(() -> buyNowService.buyNow(
                        unavailableProduct, customer, billing, shipping,
                        new Quantity(1), PaymentMethod.GATEWAY_BALANCE,
                        new CreditCardId()
                ));
    }

    /**
     * Cenário de erro: compra bloqueada quando a quantidade é zero.
     *
     * Order.addItem() delega para OrderItem.brandNew(), que internamente chama
     * Money.multiply(Quantity), o qual rejeita qualquer quantity < 1.
     * Isso garante a invariante de que um item sempre deve ter ao menos 1 unidade.
     */
    @Test
    void shouldThrowExceptionWhenQuantityIsZero() {
        // Arrange — produto válido, mas quantidade inválida
        Product notebook = ProductTestDataBuilder.aProduct().build();
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping();
        Quantity zeroQuantity = new Quantity(0);

        // Act + Assert
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> buyNowService.buyNow(
                        notebook, customer, billing, shipping,
                        zeroQuantity, PaymentMethod.GATEWAY_BALANCE,
                        new CreditCardId()
                ));
    }

    @Test
    void givenCustomerWithFreeShippingWhenBuyNowShouldReturnPlacedOrderWithFreeShipping() {
        // condição de frete gratis para 2 compras no ano
        Mockito.when(orders.salesQuantityByCustomerInYear(
                Mockito.any(CustomerId.class),
                Mockito.any(Year.class)
        )).thenReturn(2L);

        Product notebook = ProductTestDataBuilder.aProduct().price(new Money("4500")).build();
        Customer customer = CustomerTestDataBuilder.existingCustomer().loyaltyPoints(new LoyaltyPoints(100)).build();
        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping(); // custo de frete: R$10
        Quantity quantity = new Quantity(2);
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        // Act
        Order order = buyNowService.buyNow(notebook, customer, billing, shipping, quantity, paymentMethod, new CreditCardId());

        // Assert — metadados do pedido
        Assertions.assertThat(order).isNotNull();
        Assertions.assertThat(order.id()).isNotNull();
        Assertions.assertThat(order.customerId()).isEqualTo(customer.id());
        Assertions.assertThat(order.billing()).isEqualTo(billing);
        Assertions.assertThat(order.shipping()).isEqualTo(shipping.toBuilder().cost(Money.ZERO).build());
        Assertions.assertThat(order.paymentMethod()).isEqualTo(paymentMethod);

        // O pedido deve sair do buyNow já no estado PLACED
        Assertions.assertThat(order.isPlaced()).isTrue();

        // Assert — produto e quantidade adicionados corretamente
        // buyNow adiciona exatamente 1 tipo de produto, logo deve haver somente 1 OrderItem
        Assertions.assertThat(order.items()).hasSize(1);

        OrderItem orderItem = order.items().iterator().next();
        Assertions.assertThat(orderItem.productId()).isEqualTo(notebook.id());
        Assertions.assertThat(orderItem.price()).isEqualTo(new Money("4500"));
        Assertions.assertThat(orderItem.quantity()).isEqualTo(new Quantity(2));
        Assertions.assertThat(orderItem.totalAmount()).isEqualTo(new Money("4500").multiply(new Quantity(2)));

        // Assert — totais do pedido usando Money (sem BigDecimal bruto)
        // totalQuantity reflete a quantidade do único item adicionado
        Assertions.assertThat(order.totalQuantity()).isEqualTo(new Quantity(2));

        // totalAmount = item total + frete
        Money expectedTotal = new Money("4500").multiply(new Quantity(2));
        Assertions.assertThat(order.totalAmount()).isEqualTo(expectedTotal);
    }
}
