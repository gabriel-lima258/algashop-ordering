package com.gtech.algashop.core.domain.model.shoppingcart;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.commons.Quantity;
import com.gtech.algashop.core.domain.model.costumer.CustomerAlreadyHaveShoppingCartException;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.core.domain.model.costumer.Customers;
import com.gtech.algashop.core.domain.model.customer.CustomerTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

/**
 * Testes unitários para o ShoppingService.
 *
 * O ShoppingService possui duas dependências de repositório (Customers e ShoppingCarts)
 * que acessam o banco de dados. Por isso usamos Mockito para substituí-las por
 * implementações falsas (mocks) controladas pelo teste — sem necessidade de Spring
 * context ou banco de dados.
 *
 * Padrão de anotações Mockito:
 *   @Mock          → cria um mock da interface (implementação falsa controlada)
 *   @InjectMocks   → instancia o ShoppingService e injeta os mocks via construtor
 *                    (Lombok @RequiredArgsConstructor gera o construtor que o Mockito usa)
 *   @ExtendWith    → ativa o processamento das anotações Mockito no JUnit 5
 */
@ExtendWith(MockitoExtension.class)
class ShoppingServiceTest {

    @Mock
    private Customers customers;

    @Mock
    private ShoppingCarts shoppingCarts;

    // ShoppingService recebe Customers e ShoppingCarts via @RequiredArgsConstructor.
    // O Mockito detecta esse construtor e injeta os mocks automaticamente.
    @InjectMocks
    private ShoppingService shoppingService;

    /**
     * Cenário feliz: cliente existe e não possui carrinho ativo.
     *
     * O serviço deve retornar um ShoppingCart novo, vazio e vinculado ao customerId.
     * Verificamos também que as duas chamadas às dependências ocorreram
     * com os argumentos corretos — garantindo que o serviço não ignora as validações.
     */
    @Test
    void shouldStartShoppingForExistingCustomerWithNoCart() {
        // Arrange
        CustomerId customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

        // Define o comportamento dos mocks para este cenário:
        // - customers.exists() confirma que o cliente existe
        // - shoppingCarts.ofCustomer() indica que não há carrinho ativo para ele
        Mockito.when(customers.exists(customerId)).thenReturn(true);
        Mockito.when(shoppingCarts.ofCustomer(customerId)).thenReturn(Optional.empty());

        // Act
        ShoppingCart cart = shoppingService.startShopping(customerId);

        // Assert — o carrinho retornado deve ser válido e pertencer ao cliente
        Assertions.assertThat(cart).isNotNull();
        Assertions.assertThat(cart.id()).isNotNull();
        Assertions.assertThat(cart.customerId()).isEqualTo(customerId);

        // O carrinho recém-criado deve estar completamente vazio
        Assertions.assertThat(cart.isEmpty()).isTrue();
        Assertions.assertThat(cart.totalItems()).isEqualTo(Quantity.ZERO);
        Assertions.assertThat(cart.totalAmount()).isEqualTo(Money.ZERO);

        // Verifica que as duas validações foram executadas na ordem correta e com o argumento certo.
        // Sem isso, poderíamos ter um serviço que retorna um carrinho sem validar nada.
        Mockito.verify(customers).exists(customerId);
        Mockito.verify(shoppingCarts).ofCustomer(customerId);
    }

    /**
     * Cenário de erro: cliente não encontrado.
     *
     * O serviço deve lançar CustomerNotFoundException imediatamente após confirmar
     * que o cliente não existe. A segunda validação (ofCustomer) nunca deve ser
     * acionada — seria um desperdício de I/O e sinalizaria lógica incorreta.
     */
    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Arrange
        CustomerId customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

        // Simula cliente inexistente
        Mockito.when(customers.exists(customerId)).thenReturn(false);

        // Act + Assert
        Assertions.assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> shoppingService.startShopping(customerId));

        // Verifica que a primeira validação foi consultada...
        Mockito.verify(customers).exists(customerId);

        // ...e que shoppingCarts não foi acessado: sem cliente, não faz sentido
        // verificar carrinho. Usar never() garante que o serviço fez curto-circuito
        // corretamente, evitando chamadas desnecessárias ao repositório.
        Mockito.verify(shoppingCarts, Mockito.never()).ofCustomer(customerId);
    }

    /**
     * Cenário de erro: cliente já possui um carrinho ativo.
     *
     * O sistema não permite dois carrinhos simultâneos por cliente.
     * Quando ofCustomer() retorna um carrinho existente, o serviço deve
     * lançar CustomerAlreadyHaveShoppingCartException sem criar um novo carrinho.
     */
    @Test
    void shouldThrowExceptionWhenCustomerAlreadyHasCart() {
        // Arrange
        CustomerId customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

        // Carrinho existente vinculado ao mesmo cliente — simula o estado do banco
        ShoppingCart existingCart = ShoppingCartTestDataBuilder
                .aShoppingCart()
                .customerId(customerId)
                .build();

        // O cliente existe e já possui um carrinho ativo
        Mockito.when(customers.exists(customerId)).thenReturn(true);
        Mockito.when(shoppingCarts.ofCustomer(customerId)).thenReturn(Optional.of(existingCart));

        // Act + Assert
        Assertions.assertThatExceptionOfType(CustomerAlreadyHaveShoppingCartException.class)
                .isThrownBy(() -> shoppingService.startShopping(customerId));

        // Ambas as validações devem ter sido consultadas:
        // o serviço passou pela primeira (cliente existe) e foi barrado na segunda (já tem carrinho)
        Mockito.verify(customers).exists(customerId);
        Mockito.verify(shoppingCarts).ofCustomer(customerId);
    }
}
