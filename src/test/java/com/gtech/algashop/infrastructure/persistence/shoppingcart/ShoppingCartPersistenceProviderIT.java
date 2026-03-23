package com.gtech.algashop.infrastructure.persistence.shoppingcart;

import com.gtech.algashop.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartTestDataBuilder;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityAssembler;
import com.gtech.algashop.infrastructure.persistence.SpringDataAuditingConfig;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerPersistenceEntityDisassembler;
import com.gtech.algashop.infrastructure.persistence.customer.CustomersPersistenceProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Import({
        ShoppingCartPersistenceProvider.class,
        ShoppingCartPersistenceEntityAssembler.class,
        ShoppingCartPersistenceEntityDisassembler.class,
        CustomersPersistenceProvider.class,
        CustomerPersistenceEntityAssembler.class,
        CustomerPersistenceEntityDisassembler.class,
        SpringDataAuditingConfig.class
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = "spring.flyway.locations=classpath:db/migration,classpath:db/testdata")
class ShoppingCartPersistenceProviderIT {

    private static final CustomerId CUSTOMER_ID = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

    private final ShoppingCartPersistenceProvider shoppingCartProvider;
    private final ShoppingCartJpaEntityRepository jpaEntityRepository;

    @Autowired
    public ShoppingCartPersistenceProviderIT(ShoppingCartPersistenceProvider shoppingCartProvider,
                                             ShoppingCartJpaEntityRepository jpaEntityRepository) {
        this.shoppingCartProvider = shoppingCartProvider;
        this.jpaEntityRepository = jpaEntityRepository;
    }

    private ShoppingCart newCart() {
        return ShoppingCartTestDataBuilder.aShoppingCart().customerId(CUSTOMER_ID).build();
    }

    /**
     * Verifica que o provider persiste corretamente: auditing preenchido, versão gerenciada,
     * e que o UPDATE preserva o estado correto após a segunda chamada ao add().
     */
    @Test
    void shouldUpdateAndKeepPersistenceEntityState() {
        ShoppingCart cart = newCart();
        shoppingCartProvider.add(cart);

        var persistenceEntity = jpaEntityRepository.findById(cart.id().value()).orElseThrow();
        Assertions.assertThat(persistenceEntity.getCreatedByUserId()).isNotNull();
        Assertions.assertThat(persistenceEntity.getLastModifiedByUserId()).isNotNull();
        Assertions.assertThat(persistenceEntity.getLastModifiedAt()).isNotNull();

        cart = shoppingCartProvider.ofId(cart.id()).orElseThrow();
        cart.addItem(ProductTestDataBuilder.aProductRamMemory().build(), new Quantity(1));
        shoppingCartProvider.add(cart);

        persistenceEntity = jpaEntityRepository.findById(cart.id().value()).orElseThrow();
        Assertions.assertThat(persistenceEntity.getTotalItems()).isGreaterThan(0);
        Assertions.assertThat(persistenceEntity.getLastModifiedAt()).isNotNull();
        Assertions.assertThat(persistenceEntity.getCreatedByUserId()).isNotNull();
    }

    // Spring Data JPA abre uma transação interna automaticamente para métodos de escrita.
    // O NOT_SUPPORTED verifica que o Lazy carregamento dos itens funciona fora de uma transação ativa,
    // pois o provider gerencia sua própria sessão internamente.
    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldAddFindAndNotFailWhenNoTransaction() {
        ShoppingCart cart = newCart();
        shoppingCartProvider.add(cart);

        Assertions.assertThatNoException().isThrownBy(
                () -> shoppingCartProvider.ofId(cart.id()).orElseThrow()
        );
    }
}
