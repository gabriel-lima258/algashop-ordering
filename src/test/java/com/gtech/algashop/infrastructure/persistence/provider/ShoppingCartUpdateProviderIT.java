package com.gtech.algashop.infrastructure.persistence.provider;

import com.gtech.algashop.domain.model.entity.ShoppingCart;
import com.gtech.algashop.domain.model.entity.ShoppingCartItem;
import com.gtech.algashop.domain.model.entity.VO.Money;
import com.gtech.algashop.domain.model.entity.VO.Product;
import com.gtech.algashop.domain.model.entity.VO.Quantity;
import com.gtech.algashop.domain.model.entity.VO.id.CustomerId;
import com.gtech.algashop.domain.model.entity.VO.id.ProductId;
import com.gtech.algashop.domain.model.entity.factory.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.entity.factory.ProductTestDataBuilder;
import com.gtech.algashop.domain.model.entity.factory.ShoppingCartTestDataBuilder;
import com.gtech.algashop.infrastructure.persistence.assembler.CustomerPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.assembler.ShoppingCartPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.config.SpringDataAuditingConfig;
import com.gtech.algashop.infrastructure.persistence.disassembler.CustomerPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.disassembler.ShoppingCartPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.repository.ShoppingCartJpaEntityRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Testes de integração para o ShoppingCartUpdateProvider.
 *
 * Esta classe valida os casos de uso que envolvem atualizações em massa no carrinho
 * de compras quando eventos externos ocorrem no catálogo de produtos, como mudança
 * de preço ou alteração de disponibilidade em estoque.
 *
 * --- Por que @DataJpaTest? ---
 * Sobe apenas a fatia JPA do contexto Spring (entidades, repositórios, DataSource),
 * sem carregar controllers, services de aplicação ou outros beans desnecessários.
 * O banco de dados utilizado é o H2 em memória, configurado automaticamente pelo Spring.
 *
 * --- Por que @Import? ---
 * @DataJpaTest não registra automaticamente beans que não sejam repositórios JPA.
 * Os providers, assemblers, disassemblers e a configuração de auditoria precisam ser
 * importados explicitamente para que o contexto consiga injetar todas as dependências.
 *
 * --- Por que @DirtiesContext? ---
 * Garante que o contexto Spring (e o banco H2 em memória) seja completamente
 * reiniciado após cada método de teste. Sem isso, dados persistidos em um teste
 * poderiam vazar para o próximo, tornando os testes dependentes entre si.
 */
@DataJpaTest
@Import({
        ShoppingCartUpdateProvider.class,
        ShoppingCartPersistenceProvider.class,
        ShoppingCartPersistenceEntityAssembler.class,
        ShoppingCartPersistenceEntityDisassembler.class,
        CustomersPersistenceProvider.class,
        CustomerPersistenceEntityAssembler.class,
        CustomerPersistenceEntityDisassembler.class,
        SpringDataAuditingConfig.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ShoppingCartUpdateProviderIT {

    // ID fixo reutilizado em todos os testes para garantir consistência na FK de cliente.
    // Usa o mesmo ID padrão do builder de testes para evitar divergência entre o cliente
    // criado no setUp() e o carrinho criado nos testes.
    private static final CustomerId CUSTOMER_ID = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

    // Dependências injetadas via construtor — padrão preferível a @Autowired em campo
    // porque torna as dependências explícitas e facilita identificar o que o teste precisa.
    private final ShoppingCartPersistenceProvider shoppingCartProvider;
    private final CustomersPersistenceProvider customersPersistenceProvider;
    private final ShoppingCartUpdateProvider shoppingCartUpdateProvider;

    @Autowired
    public ShoppingCartUpdateProviderIT(ShoppingCartPersistenceProvider shoppingCartProvider,
                                        CustomersPersistenceProvider customersPersistenceProvider,
                                        ShoppingCartUpdateProvider shoppingCartUpdateProvider) {
        this.shoppingCartProvider = shoppingCartProvider;
        this.customersPersistenceProvider = customersPersistenceProvider;
        this.shoppingCartUpdateProvider = shoppingCartUpdateProvider;
    }

    /**
     * Pré-condição executada antes de cada teste.
     *
     * O carrinho de compras tem uma FK obrigatória para o cliente (NOT NULL na tabela).
     * Portanto, o cliente precisa existir no banco antes de qualquer carrinho ser persistido.
     *
     * A verificação com exists() evita violação de chave única caso o contexto não tenha
     * sido completamente limpo antes da execução (embora @DirtiesContext já cuide disso).
     */
    @BeforeEach
    void setUp() {
        if (!customersPersistenceProvider.exists(CUSTOMER_ID)) {
            customersPersistenceProvider.add(
                    CustomerTestDataBuilder.existingCustomer().build()
            );
        }
    }

    /**
     * Cenário: atualização de preço de produto reflete corretamente no carrinho.
     *
     * Fluxo testado:
     *   1. Monta um carrinho com dois itens (notebook R$2000 x2, memória RAM R$200 x1).
     *   2. Persiste o carrinho no banco.
     *   3. Aciona adjustPrice() para alterar o preço do notebook para R$1500.
     *      Internamente, esse método dispara queries HQL de UPDATE que recalculam:
     *        - o totalAmount de cada item do produto atualizado (preço x quantidade)
     *        - o totalAmount do carrinho inteiro (soma dos totais de todos os itens)
     *   4. Recarrega o carrinho do banco para obter o estado atualizado.
     *   5. Verifica que:
     *        - O total do carrinho é R$3200 (R$1500 x2 + R$200)
     *        - A quantidade total de itens continua 3 (não foi alterada)
     *        - O totalAmount do item do notebook é R$3000 (R$1500 x2)
     *        - O preço unitário do item do notebook é R$1500
     *
     * --- Por que @Transactional(propagation = Propagation.NEVER)? ---
     * O @DataJpaTest envolve cada teste em uma transação que faz rollback ao final.
     * O ShoppingCartUpdateProvider usa queries HQL de UPDATE que precisam ser
     * comitadas para que a leitura posterior (ofId) enxergue os dados atualizados.
     * Com NEVER, impedimos que o framework abra uma transação envolvente, forçando
     * cada operação (add, adjustPrice, ofId) a rodar em sua própria transação separada.
     */
    @Test
    @Transactional(propagation = Propagation.NEVER)
    void shouldUpdateItemPriceAndAmount() {
        // Arrange — monta o carrinho sem itens pré-carregados (withItems(false))
        // e associa ao cliente criado no setUp() para satisfazer a FK
        ShoppingCart shoppingCart = ShoppingCartTestDataBuilder.aShoppingCart().customerId(CUSTOMER_ID).withItems(false).build();

        Product notebook = ProductTestDataBuilder.aProduct().price(new Money("2000")).build();
        Product ramMemory = ProductTestDataBuilder.aProductRamMemory().price(new Money("200")).build();

        // Adiciona 2 unidades do notebook e 1 da memória RAM ao carrinho via método de domínio.
        // O domínio recalcula os totais internamente ao adicionar cada item.
        shoppingCart.addItem(notebook, new Quantity(2));
        shoppingCart.addItem(ramMemory, new Quantity(1));

        // Persiste o estado inicial do carrinho no banco H2
        shoppingCartProvider.add(shoppingCart);

        // Define os valores esperados após a atualização de preço:
        //   novo total do item  = R$1500 x 2 = R$3000
        //   novo total do cart  = R$3000 (notebook) + R$200 (RAM) = R$3200
        ProductId productIdToUpdate = notebook.id();
        Money newProductPrice = new Money("1500");
        Money expectedNewItemTotalPrice = newProductPrice.multiply(new Quantity(2));
        Money expectedNewCartTotalAmount = expectedNewItemTotalPrice.add(new Money("200"));

        // Act — dispara o caso de uso de ajuste de preço.
        // Este método executa queries HQL que atualizam TODOS os carrinhos que
        // contêm o produto, sem precisar carregar as entidades em memória.
        shoppingCartUpdateProvider.adjustPrice(productIdToUpdate, newProductPrice);

        // Recarrega o carrinho do banco para garantir leitura do estado persistido,
        // não de qualquer cache da sessão JPA anterior
        ShoppingCart updatedShoppingCart = shoppingCartProvider.ofId(shoppingCart.id()).orElseThrow();

        // Assert — verifica os totais do carrinho
        Assertions.assertThat(updatedShoppingCart.totalAmount()).isEqualTo(expectedNewCartTotalAmount);
        Assertions.assertThat(updatedShoppingCart.totalItems()).isEqualTo(new Quantity(3));

        // Assert — verifica os valores específicos do item atualizado
        ShoppingCartItem item = updatedShoppingCart.findItem(productIdToUpdate);

        Assertions.assertThat(item.totalAmount()).isEqualTo(expectedNewItemTotalPrice);
        Assertions.assertThat(item.price()).isEqualTo(newProductPrice);
    }

    /**
     * Cenário: mudança de disponibilidade de produto afeta apenas os itens correspondentes.
     *
     * Fluxo testado:
     *   1. Monta um carrinho com dois itens, ambos inicialmente em estoque (inStock=true).
     *   2. Persiste o carrinho no banco.
     *   3. Aciona changeAvailability() marcando o notebook como indisponível (false).
     *      Internamente, esse método dispara uma query HQL de UPDATE que altera
     *      o campo available de TODOS os itens do produto em TODOS os carrinhos.
     *   4. Recarrega o carrinho do banco.
     *   5. Verifica que:
     *        - O item do notebook está marcado como indisponível (available = false)
     *        - O item da memória RAM permanece disponível (available = true)
     *
     * Este teste valida o isolamento da operação: apenas o produto-alvo é afetado,
     * sem efeitos colaterais nos demais itens do mesmo carrinho.
     *
     * --- Por que @Transactional(propagation = Propagation.NEVER)? ---
     * Mesma razão do teste anterior: as queries HQL de UPDATE precisam ser comitadas
     * para que a leitura posterior enxergue os dados atualizados no banco.
     */
    @Test
    @Transactional(propagation = Propagation.NEVER)
    void shouldUpdateItemAvailability() {
        // Arrange — monta o carrinho sem itens pré-carregados, vinculado ao cliente do setUp()
        ShoppingCart shoppingCart = ShoppingCartTestDataBuilder.aShoppingCart().customerId(CUSTOMER_ID).withItems(false).build();

        // Ambos os produtos criados com inStock=true para estabelecer o estado inicial conhecido
        Product notebook = ProductTestDataBuilder.aProduct().price(new Money("2000")).inStock(true).build();
        Product ramMemory = ProductTestDataBuilder.aProductRamMemory().price(new Money("200")).inStock(true).build();

        // Guarda os IDs antes de adicionar ao carrinho para uso nas verificações finais
        ProductId productIdToUpdate = notebook.id();
        ProductId productIdNotUpdated = ramMemory.id();

        shoppingCart.addItem(notebook, new Quantity(2));
        shoppingCart.addItem(ramMemory, new Quantity(1));

        // Persiste o estado inicial do carrinho no banco H2
        shoppingCartProvider.add(shoppingCart);

        // Act — marca o notebook como fora de estoque em todos os carrinhos que o contêm
        shoppingCartUpdateProvider.changeAvailability(notebook.id(), false);

        // Recarrega o carrinho do banco para ler o estado atualizado
        ShoppingCart updatedShoppingCart = shoppingCartProvider.ofId(shoppingCart.id()).orElseThrow();

        // Assert — o item do notebook deve estar indisponível
        ShoppingCartItem item = updatedShoppingCart.findItem(productIdToUpdate);
        Assertions.assertThat(item.available()).isFalse();

        // Assert — o item da memória RAM NÃO deve ter sido afetado pela operação
        ShoppingCartItem item2 = updatedShoppingCart.findItem(productIdNotUpdated);
        Assertions.assertThat(item2.available()).isTrue();
    }
}
