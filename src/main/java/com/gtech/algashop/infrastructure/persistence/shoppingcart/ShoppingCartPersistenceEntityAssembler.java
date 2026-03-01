package com.gtech.algashop.infrastructure.persistence.shoppingcart;

import com.gtech.algashop.domain.model.shoppingcart.ShoppingCart;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartItem;
import com.gtech.algashop.infrastructure.persistence.order.OrderPersistenceEntity;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerJpaEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ASSEMBLER responsável por traduzir entre o Aggregate Root ShoppingCart (domínio)
 * e a OrderPersistenceEntity (infraestrutura/JPA).
 *
 * --- POR QUE ESSE PADRÃO EXISTE? ---
 * O domínio e o banco de dados têm estruturas e tipos diferentes:
 *   - ShoppingCart usa Value Objects tipados: Money, Quantity, OrderStatus (enum), PaymentMethod (enum)
 *   - OrderPersistenceEntity usa tipos primitivos do banco: BigDecimal, Integer, String
 *
 * --- DOIS MÉTODOS, DOIS CASOS DE USO ---
 *
 * fromDomain → usado em INSERT (adicionar novo registro)
 *   Cria uma nova entidade JPA do zero a partir de um Aggregate.
 *
 * merge → usado em UPDATE (atualizar registro existente)
 *   Preenche uma entidade JPA já existente (gerenciada pelo JPA/EntityManager)
 *   com os dados atuais do Aggregate.
 *   Retornar a mesma instância é crucial: o JPA rastreia alterações em objetos
 *   gerenciados pelo EntityManager. Se criássemos um novo objeto, o JPA não
 *   detectaria as mudanças e não geraria o UPDATE no banco.
 *
 * @see OrderPersistenceEntity
 * @see ShoppingCart
 */
@Component
@RequiredArgsConstructor
public class ShoppingCartPersistenceEntityAssembler {

    private final CustomerJpaEntityRepository customerJpaEntityRepository;

    /**
     * Cria uma nova com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntity a partir de um ShoppingCart de domínio.
     * Delega para merge() evitando duplicação de lógica de mapeamento.
     *
     * Usado pelo repositório no fluxo de INSERT:
     *   ShoppingCart (domínio) → com.gtech.algashop.infrastructure.persistence.shoppingcart.ShoppingCartPersistenceEntity (nova) → JPA salva no banco
     */
    public ShoppingCartPersistenceEntity fromDomain(ShoppingCart shoppingCart) {
        // Passa uma entidade vazia para merge() — evita duplicar a lógica de mapeamento
        return merge(new ShoppingCartPersistenceEntity(), shoppingCart);
    }

    /**
     * Cria uma nova ShoppingCartItemPersistenceEntity a partir de um ShoppingCartItem de domínio.
     * Delega para merge() evitando duplicação de lógica de mapeamento.
     *
     * Usado internamente em mergeItems() quando um item ainda não existe no banco.
     */
    public ShoppingCartItemPersistenceEntity fromDomain(ShoppingCartItem shoppingCartItem) {
        // Passa uma entidade vazia para merge() — mesma estratégia do fromDomain(ShoppingCart)
        return merge(new ShoppingCartItemPersistenceEntity(), shoppingCartItem);
    }

    /**
     * Preenche uma ShoppingCartPersistenceEntity existente com os dados de um ShoppingCart.
     * Retorna a mesma instância recebida (modificada in-place).
     *
     * Usado pelo repositório no fluxo de UPDATE:
     *   JPA carrega entidade gerenciada → merge() atualiza campos → JPA detecta mudança → UPDATE no banco
     */
    public ShoppingCartPersistenceEntity merge(ShoppingCartPersistenceEntity shoppingPersistenceEntity, ShoppingCart shoppingCart) {
        // Campos escalares: Value Objects do domínio são "desempacotados" para tipos primitivos do banco
        shoppingPersistenceEntity.setId(shoppingCart.id().value());              // OrderId UUID → Long
        shoppingPersistenceEntity.setTotalAmount(shoppingCart.totalAmount().money());     // Money → BigDecimal
        shoppingPersistenceEntity.setTotalItems(shoppingCart.totalItems().quantity()); // Quantity → Integer
        shoppingPersistenceEntity.setCreatedAt(shoppingCart.createdAt());

        // Controle de concorrência otimista: o JPA usa o @Version para detectar conflitos de escrita simultânea
        shoppingPersistenceEntity.setVersion(shoppingCart.version());

        // Itens são tratados separadamente pois exigem lógica de merge (criar novos ou atualizar existentes)
        Set<ShoppingCartItemPersistenceEntity> mergedItems = mergeItems(shoppingCart, shoppingPersistenceEntity);
        shoppingPersistenceEntity.replaceItems(mergedItems);

        var customerReference = customerJpaEntityRepository.getReferenceById(shoppingCart.customerId().value());
        shoppingPersistenceEntity.setCustomer(customerReference);

        shoppingPersistenceEntity.addEvents(shoppingCart.domainEvents());

        return shoppingPersistenceEntity;
    }

    /**
     * Preenche uma ShoppingCartItemPersistenceEntity existente com os dados de um OrderItem.
     * Retorna a mesma instância recebida (modificada in-place).
     *
     * Chamado por mergeItems() tanto para novos itens (entidade vazia) quanto para
     * itens já existentes (entidade carregada do banco).
     */
    public ShoppingCartItemPersistenceEntity merge(ShoppingCartItemPersistenceEntity shoppingCartItemEntity, ShoppingCartItem shoppingCartItem) {
        // Cada campo desfaz o encapsulamento do Value Object para o tipo primitivo esperado pelo JPA
        shoppingCartItemEntity.setId(shoppingCartItem.id().id().toLong());              // OrderItemId → Long
        shoppingCartItemEntity.setProductId(shoppingCartItem.productId().value());      // ProductId → Long
        shoppingCartItemEntity.setProductName(shoppingCartItem.name().name());   // ProductName → String
        shoppingCartItemEntity.setPrice(shoppingCartItem.price().money());              // Money → BigDecimal
        shoppingCartItemEntity.setQuantity(shoppingCartItem.quantity().quantity());     // Quantity → Integer
        shoppingCartItemEntity.setTotalAmount(shoppingCartItem.totalAmount().money());  // Money → BigDecimal
        shoppingCartItemEntity.setAvailable(shoppingCartItem.available());

        return shoppingCartItemEntity;
    }

    /**
     * Sincroniza a coleção de itens do domínio com a coleção de entidades JPA.
     *
     * O desafio aqui é o merge inteligente: para cada item do domínio, precisamos
     * decidir se ele já existe no banco (UPDATE) ou é novo (INSERT).
     *
     * Se criássemos sempre novas entidades JPA para todos os itens, o JPA tentaria
     * fazer INSERT em todos eles, causando violação de chave primária nos itens existentes.
     */
    private Set<ShoppingCartItemPersistenceEntity> mergeItems(ShoppingCart shoppingCart, ShoppingCartPersistenceEntity shoppingPersistenceEntity) {
        Set<ShoppingCartItem> updatedItems = shoppingCart.items();

        // Caso 1: pedido sem itens — retorna coleção vazia (remove todos do banco via replaceItems)
        if (updatedItems == null || updatedItems.isEmpty()) {
            return new HashSet<>();
        }

        Set<ShoppingCartItemPersistenceEntity> exisitingItems = shoppingPersistenceEntity.getItems();

        // Caso 2: entidade ainda não tem itens (INSERT inicial) — converte todos do zero
        if (exisitingItems == null || exisitingItems.isEmpty()) {
            return updatedItems.stream()
                    .map(this::fromDomain)
                    .collect(Collectors.toSet());
        }

        // Caso 3: UPDATE — indexa os itens existentes por ID para busca eficiente O(1)
        Map<Long, ShoppingCartItemPersistenceEntity> existingItemMap = exisitingItems.stream()
                .collect(Collectors.toMap(ShoppingCartItemPersistenceEntity::getId, item -> item));

        // Para cada item do domínio: reutiliza a entidade JPA existente (se houver) ou cria uma nova.
        // Isso garante que o JPA atualize os registros existentes em vez de tentar inserir duplicatas.
        return updatedItems.stream()
                .map(shoppingCartItem -> {
                    ShoppingCartItemPersistenceEntity itemPersistence = existingItemMap.getOrDefault(
                            shoppingCartItem.id().id().toLong(), new ShoppingCartItemPersistenceEntity()
                    );
                    return merge(itemPersistence, shoppingCartItem);
                }).collect(Collectors.toSet());
    }

}
