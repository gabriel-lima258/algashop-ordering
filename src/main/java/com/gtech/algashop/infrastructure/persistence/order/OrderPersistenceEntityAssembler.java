package com.gtech.algashop.infrastructure.persistence.order;

import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.order.OrderItem;
import com.gtech.algashop.domain.model.commons.Address;
import com.gtech.algashop.domain.model.order.Billing;
import com.gtech.algashop.domain.model.order.Recipient;
import com.gtech.algashop.domain.model.order.Shipping;
import com.gtech.algashop.infrastructure.persistence.commons.AddressEmbeddable;
import com.gtech.algashop.infrastructure.persistence.customer.CustomerJpaEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ASSEMBLER responsável por traduzir entre o Aggregate Root Order (domínio)
 * e a OrderPersistenceEntity (infraestrutura/JPA).
 *
 * --- POR QUE ESSE PADRÃO EXISTE? ---
 * O domínio e o banco de dados têm estruturas e tipos diferentes:
 *   - Order usa Value Objects tipados: Money, Quantity, OrderStatus (enum), PaymentMethod (enum)
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
 * @see Order
 */
@Component
@RequiredArgsConstructor
public class OrderPersistenceEntityAssembler {

    private final CustomerJpaEntityRepository customerJpaEntityRepository;

    /**
     * Cria uma nova OrderPersistenceEntity a partir de um Order de domínio.
     * Delega para merge() evitando duplicação de lógica de mapeamento.
     *
     * Usado pelo repositório no fluxo de INSERT:
     *   Order (domínio) → OrderPersistenceEntity (nova) → JPA salva no banco
     */
    public OrderPersistenceEntity fromDomain(Order order) {
        // Passa uma entidade vazia para merge() — evita duplicar a lógica de mapeamento
        return merge(new OrderPersistenceEntity(), order);
    }

    /**
     * Cria uma nova OrderItemPersistenceEntity a partir de um OrderItem de domínio.
     * Delega para merge() evitando duplicação de lógica de mapeamento.
     *
     * Usado internamente em mergeItems() quando um item ainda não existe no banco.
     */
    public OrderItemPersistenceEntity fromDomain(OrderItem orderItem) {
        // Passa uma entidade vazia para merge() — mesma estratégia do fromDomain(Order)
        return merge(new OrderItemPersistenceEntity(), orderItem);
    }

    /**
     * Preenche uma OrderPersistenceEntity existente com os dados de um Order.
     * Retorna a mesma instância recebida (modificada in-place).
     *
     * Usado pelo repositório no fluxo de UPDATE:
     *   JPA carrega entidade gerenciada → merge() atualiza campos → JPA detecta mudança → UPDATE no banco
     */
    public OrderPersistenceEntity merge(OrderPersistenceEntity orderPersistenceEntity, Order order) {
        // Campos escalares: Value Objects do domínio são "desempacotados" para tipos primitivos do banco
        orderPersistenceEntity.setId(order.id().value().toLong());              // OrderId (TSID) → Long
        orderPersistenceEntity.setTotalAmount(order.totalAmount().money());     // Money → BigDecimal
        orderPersistenceEntity.setTotalItems(order.totalQuantity().quantity()); // Quantity → Integer
        orderPersistenceEntity.setStatus(order.status().name());                // Enum → String
        orderPersistenceEntity.setPaymentMethod(order.paymentMethod().name());  // Enum → String

        // Campos de auditoria temporal: mapeados diretamente pois já são OffsetDateTime
        orderPersistenceEntity.setPlacedAt(order.placedAt());
        orderPersistenceEntity.setPaidAt(order.paidAt());
        orderPersistenceEntity.setCanceledAt(order.canceledAt());
        orderPersistenceEntity.setReadyAt(order.readyAt());

        // Value Objects complexos são convertidos para seus respectivos Embeddables JPA
        orderPersistenceEntity.setBilling(convertBillingEmbeddable(order.billing()));
        orderPersistenceEntity.setShipping(convertShippingEmbeddable(order.shipping()));

        // Controle de concorrência otimista: o JPA usa o @Version para detectar conflitos de escrita simultânea
        orderPersistenceEntity.setVersion(order.version());

        if (order.creditCardId() != null) {
            orderPersistenceEntity.setCreditCardId(order.creditCardId().id());
        }

        // Itens são tratados separadamente pois exigem lógica de merge (criar novos ou atualizar existentes)
        Set<OrderItemPersistenceEntity> mergedItems = mergeItems(order, orderPersistenceEntity);
        orderPersistenceEntity.replaceItems(mergedItems);

        var customerReference = customerJpaEntityRepository.getReferenceById(order.customerId().value());
        orderPersistenceEntity.setCustomer(customerReference);

        orderPersistenceEntity.addEvents(order.domainEvents());

        return orderPersistenceEntity;
    }

    /**
     * Preenche uma OrderItemPersistenceEntity existente com os dados de um OrderItem.
     * Retorna a mesma instância recebida (modificada in-place).
     *
     * Chamado por mergeItems() tanto para novos itens (entidade vazia) quanto para
     * itens já existentes (entidade carregada do banco).
     */
    public OrderItemPersistenceEntity merge(OrderItemPersistenceEntity orderItemPersistenceEntity, OrderItem orderItem) {
        // Cada campo desfaz o encapsulamento do Value Object para o tipo primitivo esperado pelo JPA
        orderItemPersistenceEntity.setId(orderItem.id().id().toLong());              // OrderItemId → Long
        orderItemPersistenceEntity.setProductId(orderItem.productId().value());      // ProductId → Long
        orderItemPersistenceEntity.setProductName(orderItem.productName().name());   // ProductName → String
        orderItemPersistenceEntity.setPrice(orderItem.price().money());              // Money → BigDecimal
        orderItemPersistenceEntity.setQuantity(orderItem.quantity().quantity());     // Quantity → Integer
        orderItemPersistenceEntity.setTotalAmount(orderItem.totalAmount().money());  // Money → BigDecimal

        return orderItemPersistenceEntity;
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
    private Set<OrderItemPersistenceEntity> mergeItems(Order order, OrderPersistenceEntity orderPersistenceEntity) {
        Set<OrderItem> updatedItems = order.items();

        // Caso 1: pedido sem itens — retorna coleção vazia (remove todos do banco via replaceItems)
        if (updatedItems == null || updatedItems.isEmpty()) {
            return new HashSet<>();
        }

        Set<OrderItemPersistenceEntity> exisitingItems = orderPersistenceEntity.getItems();

        // Caso 2: entidade ainda não tem itens (INSERT inicial) — converte todos do zero
        if (exisitingItems == null || exisitingItems.isEmpty()) {
            return updatedItems.stream()
                    .map(this::fromDomain)
                    .collect(Collectors.toSet());
        }

        // Caso 3: UPDATE — indexa os itens existentes por ID para busca eficiente O(1)
        Map<Long, OrderItemPersistenceEntity> existingItemMap = exisitingItems.stream()
                .collect(Collectors.toMap(OrderItemPersistenceEntity::getId, item -> item));

        // Para cada item do domínio: reutiliza a entidade JPA existente (se houver) ou cria uma nova.
        // Isso garante que o JPA atualize os registros existentes em vez de tentar inserir duplicatas.
        return updatedItems.stream()
                .map(orderItem -> {
                    OrderItemPersistenceEntity itemPersistence = existingItemMap.getOrDefault(
                            orderItem.id().id().toLong(), new OrderItemPersistenceEntity()
                    );
                    return merge(itemPersistence, orderItem);
                }).collect(Collectors.toSet());
    }

    /**
     * Converte o Value Object Shipping (domínio) para ShippingEmbeddable (JPA).
     * Embeddables são mapeados como colunas da própria tabela ORDER, sem tabela separada.
     */
    private ShippingEmbeddable convertShippingEmbeddable(Shipping shipping) {
        if (shipping == null) return null;

        return ShippingEmbeddable.builder()
                .cost(shipping.cost().money())                               // Money → BigDecimal
                .expectedDate(shipping.expectedDate())                       // LocalDate direto
                .address(convertAddressEmbeddable(shipping.address()))       // Address aninhado
                .recipient(convertRecipientEmbeddable(shipping.recipient())) // Recipient aninhado
                .build();
    }

    /**
     * Converte o Value Object Billing (domínio) para BillingEmbeddable (JPA).
     * Embeddables são mapeados como colunas da própria tabela ORDER, sem tabela separada.
     */
    private BillingEmbeddable convertBillingEmbeddable(Billing billing) {
        if (billing == null) return null;

        return BillingEmbeddable.builder()
                .firstName(billing.fullName().firstName())             // FullName → String
                .lastName(billing.fullName().lastName())               // FullName → String
                .email(billing.email().email())                        // Email VO → String
                .phone(billing.phone().phone())                        // Phone VO → String
                .document(billing.document().document())               // Document VO → String
                .address(convertAddressEmbeddable(billing.address()))  // Address aninhado
                .build();
    }

    /**
     * Converte o Value Object Address (domínio) para AddressEmbeddable (JPA).
     * Reutilizado tanto pelo Billing quanto pelo Shipping, pois ambos possuem endereço.
     */
    private AddressEmbeddable convertAddressEmbeddable(Address address) {
        if (address == null) return null;

        return AddressEmbeddable.builder()
                .street(address.street())
                .number(address.number())
                .complement(address.complement())
                .neighborhood(address.neighborhood())
                .city(address.city())
                .state(address.state())
                .zipCode(address.zipCode().zipcode()) // ZipCode VO → String
                .build();
    }

    /**
     * Converte o Value Object Recipient (domínio) para RecipientEmbeddable (JPA).
     * O Recipient representa quem vai receber o pedido no endereço de entrega.
     */
    private RecipientEmbeddable convertRecipientEmbeddable(Recipient recipient) {
        if (recipient == null) return null;

        return RecipientEmbeddable.builder()
                .firstName(recipient.fullName().firstName())  // FullName → String
                .lastName(recipient.fullName().lastName())    // FullName → String
                .phone(recipient.phone().phone())             // Phone VO → String
                .document(recipient.document().document())    // Document VO → String
                .build();
    }

}
