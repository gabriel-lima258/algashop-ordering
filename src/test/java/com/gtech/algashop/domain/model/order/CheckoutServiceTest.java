package com.gtech.algashop.domain.model.service;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.order.*;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.product.ProductId;
import com.gtech.algashop.domain.model.entity.factory.OrderTestDataBuilder;
import com.gtech.algashop.domain.model.entity.factory.ProductTestDataBuilder;
import com.gtech.algashop.domain.model.entity.factory.ShoppingCartTestDataBuilder;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartCantProceedToCheckoutException;
import com.gtech.algashop.domain.model.product.Product;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Testes unitários para o CheckoutService.
 *
 * O CheckoutService é um Domain Service puro (sem dependências de infraestrutura),
 * por isso não precisamos de Spring context, Mockito ou banco de dados.
 * Instanciamos o serviço diretamente no setUp() para máxima simplicidade e velocidade.
 *
 * Responsabilidade do CheckoutService:
 *   1. Validar que o carrinho não contém itens indisponíveis
 *   2. Validar que o carrinho não está vazio
 *   3. Criar um Order em estado PLACED com todos os dados do carrinho
 *   4. Esvaziar o carrinho após o checkout bem-sucedido
 */
class CheckoutServiceTest {

    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        // CheckoutService não possui dependências externas (repositórios, adapters etc.),
        // portanto é instanciado diretamente — sem @InjectMocks ou contexto Spring.
        checkoutService = new CheckoutService();
    }

    /**
     * Cenário feliz: checkout completo com carrinho válido.
     *
     * Fluxo:
     *   - Carrinho com 2 produtos diferentes (notebook x2, mousepad x1)
     *   - checkout() é chamado com billing, shipping e método de pagamento
     *   - Deve retornar um Order PLACED com todos os dados do carrinho
     *   - O carrinho deve estar vazio ao final
     *
     * Valores calculados manualmente para facilitar a leitura:
     *   notebook total  = R$4500 x 2 = R$9000
     *   mousepad total  = R$120  x 1 = R$120
     *   frete           =             R$10  (OrderTestDataBuilder.aShipping())
     *   order total     =             R$9130
     */
    @Test
    void shouldCheckoutValidCart() {
        // Arrange — monta carrinho sem itens pré-carregados e adiciona produtos manualmente
        // para termos controle total sobre preços e quantidades nas asserções
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().withItems(false).build();

        Product notebook = ProductTestDataBuilder.aProduct().price(new Money("4500")).build();
        Product mousepad = ProductTestDataBuilder.aProductMousePad().price(new Money("120")).build();

        cart.addItem(notebook, new Quantity(2));
        cart.addItem(mousepad, new Quantity(1));

        // Salva o estado do carrinho ANTES do checkout, pois cart.empty() será chamado
        // internamente pelo serviço e perdemos acesso ao customerId via cart.items() depois
        CustomerId cartCustomerId = cart.customerId();
        Set<ProductId> cartProductIds = cart.items().stream()
                .map(i -> i.productId())
                .collect(Collectors.toSet());

        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping();
        PaymentMethod paymentMethod = PaymentMethod.CREDIT_CARD;

        // Act
        Order order = checkoutService.checkout(cart, billing, shipping, paymentMethod);

        // Assert — metadados do pedido
        Assertions.assertThat(order).isNotNull();
        Assertions.assertThat(order.id()).isNotNull();

        // O pedido deve pertencer ao mesmo cliente do carrinho
        Assertions.assertThat(order.customerId()).isEqualTo(cartCustomerId);

        // O pedido deve carregar exatamente o billing e shipping fornecidos
        Assertions.assertThat(order.billing()).isEqualTo(billing);
        Assertions.assertThat(order.shipping()).isEqualTo(shipping);

        // O método de pagamento deve ser preservado
        Assertions.assertThat(order.paymentMethod()).isEqualTo(paymentMethod);

        // O pedido deve sair do checkout já no estado PLACED
        Assertions.assertThat(order.isPlaced()).isTrue();

        // Assert — itens transferidos do carrinho para o pedido
        // A quantidade de OrderItems deve corresponder à quantidade de ShoppingCartItems
        Assertions.assertThat(order.items()).hasSize(2);

        // totalQuantity = soma das quantidades de todos os itens (2 notebooks + 1 mousepad)
        Assertions.assertThat(order.totalQuantity()).isEqualTo(new Quantity(3));

        // Os IDs dos produtos nos itens do pedido devem ser os mesmos do carrinho
        Set<ProductId> orderProductIds = order.items().stream()
                .map(OrderItem::productId)
                .collect(Collectors.toSet());
        Assertions.assertThat(orderProductIds).isEqualTo(cartProductIds);

        // Verifica preço unitário, quantidade e total do item notebook individualmente
        // (garante que a transferência de dados foi item a item, não apenas os totais)
        OrderItem notebookOrderItem = order.items().stream()
                .filter(i -> i.productId().equals(notebook.id()))
                .findFirst()
                .orElseThrow();
        Assertions.assertThat(notebookOrderItem.price()).isEqualTo(new Money("4500"));
        Assertions.assertThat(notebookOrderItem.quantity()).isEqualTo(new Quantity(2));
        Assertions.assertThat(notebookOrderItem.totalAmount()).isEqualTo(new Money("4500").multiply(new Quantity(2)));

        // Assert — total do pedido usando Money para evitar comparações com BigDecimal bruto
        // total = (R$4500 x 2) + R$120 + R$10 (frete) = R$9130
        Money expectedTotal = new Money("4500").multiply(new Quantity(2))
                .add(new Money("120"))
                .add(new Money("10"));
        Assertions.assertThat(order.totalAmount()).isEqualTo(expectedTotal);

        // Assert — carrinho deve estar completamente vazio após o checkout bem-sucedido
        Assertions.assertThat(cart.isEmpty()).isTrue();
        Assertions.assertThat(cart.totalItems()).isEqualTo(Quantity.ZERO);
        Assertions.assertThat(cart.totalAmount()).isEqualTo(Money.ZERO);
    }

    /**
     * Cenário de erro: checkout bloqueado quando há itens indisponíveis.
     *
     * O carrinho começa com dois itens disponíveis. Em seguida, simulamos
     * a saída de estoque do notebook usando refreshItem() com inStock=false
     * — exatamente como aconteceria quando o catálogo notifica o carrinho de
     * uma mudança de disponibilidade.
     *
     * Comportamento esperado:
     *   - ShoppingCartCantProceedToCheckoutException é lançada
     *   - O carrinho NÃO é esvaziado (cart.empty() nunca foi chamado)
     *   - Os itens permanecem intactos no carrinho
     */
    @Test
    void shouldThrowExceptionWhenCartHasUnavailableItems() {
        // Arrange — dois itens disponíveis adicionados ao carrinho
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().withItems(false).build();

        Product notebook = ProductTestDataBuilder.aProduct().inStock(true).build();
        Product mousepad = ProductTestDataBuilder.aProductMousePad().inStock(true).build();

        cart.addItem(notebook, new Quantity(1));
        cart.addItem(mousepad, new Quantity(1));

        // Simula produto saindo de estoque: cria uma versão do mesmo produto (mesmo ID)
        // com inStock=false e atualiza o item no carrinho via refreshItem().
        // Isso reflete o fluxo real onde o catálogo publica um evento de indisponibilidade.
        Product unavailableNotebook = Product.builder()
                .id(notebook.id())                  // mesmo produto
                .productName(notebook.productName()) // dados preservados
                .price(notebook.price())
                .inStock(false)                     // agora indisponível
                .build();
        cart.refreshItem(unavailableNotebook);

        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping();

        // Act + Assert — a exceção deve ser lançada antes de qualquer modificação no carrinho
        Assertions.assertThatExceptionOfType(ShoppingCartCantProceedToCheckoutException.class)
                .isThrownBy(() -> checkoutService.checkout(cart, billing, shipping, PaymentMethod.GATEWAY_BALANCE));

        // O carrinho deve permanecer com seus itens — cart.empty() NÃO foi chamado
        Assertions.assertThat(cart.isEmpty()).isFalse();
        Assertions.assertThat(cart.totalItems()).isEqualTo(new Quantity(2)); // notebook x1 + mousepad x1
    }

    /**
     * Cenário de erro: checkout bloqueado quando o carrinho está vazio.
     *
     * Um carrinho vazio não possui itens para transferir ao pedido.
     * O serviço deve rejeitar o checkout antes de criar qualquer Order.
     *
     * Comportamento esperado:
     *   - ShoppingCartCantProceedToCheckoutException é lançada
     *   - O carrinho permanece vazio e inalterado
     */
    @Test
    void shouldThrowExceptionWhenCartIsEmpty() {
        // Arrange — carrinho criado sem itens (withItems(false))
        ShoppingCart cart = ShoppingCartTestDataBuilder.aShoppingCart().withItems(false).build();

        Billing billing = OrderTestDataBuilder.aBilling();
        Shipping shipping = OrderTestDataBuilder.aShipping();

        // Act + Assert
        Assertions.assertThatExceptionOfType(ShoppingCartCantProceedToCheckoutException.class)
                .isThrownBy(() -> checkoutService.checkout(cart, billing, shipping, PaymentMethod.GATEWAY_BALANCE));

        // O carrinho já estava vazio e deve continuar vazio — nenhuma alteração de estado
        Assertions.assertThat(cart.isEmpty()).isTrue();
        Assertions.assertThat(cart.totalItems()).isEqualTo(Quantity.ZERO);
        Assertions.assertThat(cart.totalAmount()).isEqualTo(Money.ZERO);
    }
}
