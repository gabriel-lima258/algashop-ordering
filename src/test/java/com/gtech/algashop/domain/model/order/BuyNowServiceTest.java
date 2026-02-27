package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.product.ProductOutOfStockException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
class BuyNowServiceTest {

    private BuyNowService buyNowService;

    @BeforeEach
    void setUp() {
        // BuyNowService não possui dependências externas — instanciado diretamente
        buyNowService = new BuyNowService();
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
        CustomerId customerId = new CustomerId();
        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping(); // custo de frete: R$10
        Quantity quantity = new Quantity(2);
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        // Act
        Order order = buyNowService.buyNow(notebook, customerId, billing, shipping, quantity, paymentMethod);

        // Assert — metadados do pedido
        Assertions.assertThat(order).isNotNull();
        Assertions.assertThat(order.id()).isNotNull();
        Assertions.assertThat(order.customerId()).isEqualTo(customerId);
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
        CustomerId customerId = new CustomerId();
        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping();

        // Act + Assert
        Assertions.assertThatExceptionOfType(ProductOutOfStockException.class)
                .isThrownBy(() -> buyNowService.buyNow(
                        unavailableProduct, customerId, billing, shipping,
                        new Quantity(1), PaymentMethod.GATEWAY_BALANCE
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
        CustomerId customerId = new CustomerId();
        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping();
        Quantity zeroQuantity = new Quantity(0);

        // Act + Assert
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> buyNowService.buyNow(
                        notebook, customerId, billing, shipping,
                        zeroQuantity, PaymentMethod.GATEWAY_BALANCE
                ));
    }
}
