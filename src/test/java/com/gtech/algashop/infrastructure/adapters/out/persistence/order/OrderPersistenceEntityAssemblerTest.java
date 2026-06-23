package com.gtech.algashop.infrastructure.adapters.out.persistence.order;

import com.gtech.algashop.core.domain.model.customer.CustomerPersistenceEntityTestDataBuilder;
import com.gtech.algashop.core.domain.model.order.*;
import com.gtech.algashop.infrastructure.adapters.out.persistence.customer.CustomerJpaEntityRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceEntityAssemblerTest {

    @Mock
    private CustomerJpaEntityRepository customerJpaEntityRepository;

    @InjectMocks
    private OrderPersistenceEntityAssembler assembler;

    @BeforeEach
    void setUp() {
        Mockito.when(customerJpaEntityRepository.getReferenceById(Mockito.any(UUID.class)))
                .then(a -> {
                    UUID customerId = a.getArgument(0, UUID.class);
                    return CustomerPersistenceEntityTestDataBuilder.existingCustomer().id(customerId).build();
                });
    }

    @Test
    void shouldConvertToDomain() {
        Order order = OrderTestDataBuilder.anOrder().build();
        OrderPersistenceEntity orderPersistenceEntity = assembler.fromDomain(order);
        assertThat(orderPersistenceEntity).satisfies(
                p-> assertThat(p.getId()).isEqualTo(order.id().value().toLong()),
                p-> assertThat(p.getCustomerId()).isEqualTo(order.customerId().value()),
                p -> assertThat(p.getTotalAmount()).isEqualTo(order.totalAmount().money()),
                p -> assertThat(p.getTotalItems()).isEqualTo(order.totalQuantity().quantity()),
                p -> assertThat(p.getStatus()).isEqualTo(order.status().name()),
                p -> assertThat(p.getPaymentMethod()).isEqualTo(order.paymentMethod().name()),
                p -> assertThat(p.getPlacedAt()).isEqualTo(order.placedAt()),
                p -> assertThat(p.getPaidAt()).isEqualTo(order.paidAt()),
                p -> assertThat(p.getCanceledAt()).isEqualTo(order.canceledAt()),
                p -> assertThat(p.getReadyAt()).isEqualTo(order.readyAt())
        );
    }

    @Test
    void givenOrderWithNoItemsShouldRemovePersistenceEntityItems() {
        Order order = OrderTestDataBuilder.anOrder().withItems(false).build();
        OrderPersistenceEntity orderPersistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder().build();

        Assertions.assertThat(order.items()).isEmpty();
        Assertions.assertThat(orderPersistenceEntity.getItems()).isNotEmpty();

        assembler.merge(orderPersistenceEntity, order);

        Assertions.assertThat(orderPersistenceEntity.getItems()).isEmpty();
    }

    @Test
    void givenOrderWithItemsShouldAddToPersistenceEntityItems() {
        Order order = OrderTestDataBuilder.anOrder().withItems(true).build();
        OrderPersistenceEntity orderPersistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder().items(new HashSet<>()).build();

        Assertions.assertThat(order.items()).isNotEmpty();
        Assertions.assertThat(orderPersistenceEntity.getItems()).isEmpty();

        assembler.merge(orderPersistenceEntity, order);

        Assertions.assertThat(orderPersistenceEntity.getItems()).isNotEmpty();
        Assertions.assertThat(orderPersistenceEntity.getItems().size()).isEqualTo(order.items().size());
    }

    @Test
    void givenOrderWithItemsWhenMergeShouldRemoveMergeCorrectly() {
        Order order = OrderTestDataBuilder.anOrder().withItems(true).build();

        Assertions.assertThat(order.items().size()).isEqualTo(2);

        Set<OrderItemPersistenceEntity> orderItemPersistenceEntities = order.items().stream()
                .map(i -> assembler.fromDomain(i))
                .collect(Collectors.toSet());

        OrderPersistenceEntity orderPersistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder()
                .items(orderItemPersistenceEntities)
                .build();

        OrderItem orderItem = order.items().iterator().next();
        order.removeItem(orderItem.id());

        assembler.merge(orderPersistenceEntity, order);

        Assertions.assertThat(orderPersistenceEntity.getItems()).isNotEmpty();
        Assertions.assertThat(orderPersistenceEntity.getItems().size()).isEqualTo(order.items().size());
    }

    @Test
    void shouldMapBillingEmbeddable() {
        Order order = OrderTestDataBuilder.anOrder().build();

        OrderPersistenceEntity entity = assembler.fromDomain(order);

        assertThat(entity.getBilling()).isNotNull();
        assertThat(entity.getBilling().getFirstName()).isEqualTo(order.billing().fullName().firstName());
        assertThat(entity.getBilling().getLastName()).isEqualTo(order.billing().fullName().lastName());
        assertThat(entity.getBilling().getEmail()).isEqualTo(order.billing().email().email());
        assertThat(entity.getBilling().getPhone()).isEqualTo(order.billing().phone().phone());
        assertThat(entity.getBilling().getDocument()).isEqualTo(order.billing().document().document());
    }

    @Test
    void shouldMapBillingAddressEmbeddable() {
        Order order = OrderTestDataBuilder.anOrder().build();

        OrderPersistenceEntity entity = assembler.fromDomain(order);

        assertThat(entity.getBilling().getAddress()).isNotNull();
        assertThat(entity.getBilling().getAddress().getStreet()).isEqualTo(order.billing().address().street());
        assertThat(entity.getBilling().getAddress().getNumber()).isEqualTo(order.billing().address().number());
        assertThat(entity.getBilling().getAddress().getComplement()).isEqualTo(order.billing().address().complement());
        assertThat(entity.getBilling().getAddress().getNeighborhood()).isEqualTo(order.billing().address().neighborhood());
        assertThat(entity.getBilling().getAddress().getCity()).isEqualTo(order.billing().address().city());
        assertThat(entity.getBilling().getAddress().getState()).isEqualTo(order.billing().address().state());
        assertThat(entity.getBilling().getAddress().getZipCode()).isEqualTo(order.billing().address().zipCode().zipcode());
    }

    @Test
    void shouldMapShippingEmbeddable() {
        Order order = OrderTestDataBuilder.anOrder().build();

        OrderPersistenceEntity entity = assembler.fromDomain(order);

        assertThat(entity.getShipping()).isNotNull();
        assertThat(entity.getShipping().getCost()).isEqualTo(order.shipping().cost().money());
        assertThat(entity.getShipping().getExpectedDate()).isEqualTo(order.shipping().expectedDate());
    }

    @Test
    void shouldMapShippingRecipientEmbeddable() {
        Order order = OrderTestDataBuilder.anOrder().build();

        OrderPersistenceEntity entity = assembler.fromDomain(order);

        assertThat(entity.getShipping().getRecipient()).isNotNull();
        assertThat(entity.getShipping().getRecipient().getFirstName()).isEqualTo(order.shipping().recipient().fullName().firstName());
        assertThat(entity.getShipping().getRecipient().getLastName()).isEqualTo(order.shipping().recipient().fullName().lastName());
        assertThat(entity.getShipping().getRecipient().getPhone()).isEqualTo(order.shipping().recipient().phone().phone());
        assertThat(entity.getShipping().getRecipient().getDocument()).isEqualTo(order.shipping().recipient().document().document());
    }

    @Test
    void shouldMapShippingAddressEmbeddable() {
        Order order = OrderTestDataBuilder.anOrder().build();

        OrderPersistenceEntity entity = assembler.fromDomain(order);

        assertThat(entity.getShipping().getAddress()).isNotNull();
        assertThat(entity.getShipping().getAddress().getStreet()).isEqualTo(order.shipping().address().street());
        assertThat(entity.getShipping().getAddress().getNumber()).isEqualTo(order.shipping().address().number());
        assertThat(entity.getShipping().getAddress().getCity()).isEqualTo(order.shipping().address().city());
        assertThat(entity.getShipping().getAddress().getZipCode()).isEqualTo(order.shipping().address().zipCode().zipcode());
    }

    @Test
    void shouldMapDateFieldsWhenOrderIsPaid() {
        // PAID: passou por PLACED → PAID, então placedAt e paidAt são preenchidos
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).build();

        OrderPersistenceEntity entity = assembler.fromDomain(order);

        assertThat(entity.getStatus()).isEqualTo("PAID");

        // Datas preenchidas pela máquina de estados do domínio
        assertThat(entity.getPlacedAt()).isNotNull().isEqualTo(order.placedAt());
        assertThat(entity.getPaidAt()).isNotNull().isEqualTo(order.paidAt());

        // Datas que não ocorreram neste fluxo permanecem nulas
        assertThat(entity.getCanceledAt()).isNull();
        assertThat(entity.getReadyAt()).isNull();
    }

    @Test
    void shouldMapCanceledAtWhenOrderIsCanceled() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.CANCELED).build();

        OrderPersistenceEntity entity = assembler.fromDomain(order);

        assertThat(entity.getStatus()).isEqualTo("CANCELED");
        assertThat(entity.getCanceledAt()).isNotNull().isEqualTo(order.canceledAt());

        // Pedido cancelado nunca foi pago ou preparado
        assertThat(entity.getPaidAt()).isNull();
        assertThat(entity.getReadyAt()).isNull();
    }

    @Test
    void shouldMapReadyAtWhenOrderIsReady() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.READY).build();

        OrderPersistenceEntity entity = assembler.fromDomain(order);

        assertThat(entity.getStatus()).isEqualTo("READY");
        assertThat(entity.getPlacedAt()).isNotNull().isEqualTo(order.placedAt());
        assertThat(entity.getPaidAt()).isNotNull().isEqualTo(order.paidAt());
        assertThat(entity.getReadyAt()).isNotNull().isEqualTo(order.readyAt());
        assertThat(entity.getCanceledAt()).isNull();
    }

    @Test
    void shouldReturnSameInstanceOnMerge() {
        // merge() deve modificar e retornar a MESMA instância, não criar uma nova.
        // Isso é fundamental para JPA: o EntityManager rastreia mudanças apenas em objetos
        // que ele gerencia. Se criássemos um novo objeto, o JPA não detectaria as mudanças
        // e não enviaria o UPDATE para o banco.
        OrderPersistenceEntity existingEntity = new OrderPersistenceEntity();
        Order order = OrderTestDataBuilder.anOrder().build();

        OrderPersistenceEntity result = assembler.merge(existingEntity, order);

        // Mesmo objeto na memória — não uma cópia
        assertThat(result).isSameAs(existingEntity);
    }

    @Test
    void shouldOverwriteExistingEntityFieldsOnMerge() {
        // Simula o cenário de UPDATE: uma entidade já carregada do banco tem seus
        // campos sobrescritos com o estado atual do Aggregate.
        Order originalOrder = OrderTestDataBuilder.anOrder().build();
        OrderPersistenceEntity existingEntity = assembler.fromDomain(originalOrder);

        // Agregado avançou de estado (foi pago)
        Order updatedOrder = OrderTestDataBuilder.anOrder().status(OrderStatus.PAID).build();

        assembler.merge(existingEntity, updatedOrder);

        // Campos da entidade agora refletem o novo estado do domínio
        assertThat(existingEntity.getId()).isEqualTo(updatedOrder.id().value().toLong());
        assertThat(existingEntity.getStatus()).isEqualTo("PAID");
        assertThat(existingEntity.getPaidAt()).isNotNull().isEqualTo(updatedOrder.paidAt());
        assertThat(existingEntity.getPlacedAt()).isNotNull().isEqualTo(updatedOrder.placedAt());
    }
}
