package com.gtech.algashop.infrastructure.persistence.order;

import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.order.OrderStatus;
import com.gtech.algashop.domain.model.order.PaymentMethod;
import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.OrderPersistenceEntityTestDataBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OrderPersistenceEntityDisassemblerTest {

    private final OrderPersistenceEntityDisassembler disassembler = new OrderPersistenceEntityDisassembler();

    @Test
    void shouldMapScalarFields() {
        OrderPersistenceEntity persistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder().build();

        Order order = disassembler.toDomainEntity(persistenceEntity);

        assertThat(order.id()).isEqualTo(new OrderId(persistenceEntity.getId()));
        assertThat(order.customerId()).isEqualTo(new CustomerId(persistenceEntity.getCustomerId()));
        assertThat(order.totalAmount()).isEqualTo(new Money(persistenceEntity.getTotalAmount()));
        assertThat(order.totalQuantity()).isEqualTo(new Quantity(persistenceEntity.getTotalItems()));
        assertThat(order.status()).isEqualTo(OrderStatus.valueOf(persistenceEntity.getStatus()));
        assertThat(order.paymentMethod()).isEqualTo(PaymentMethod.valueOf(persistenceEntity.getPaymentMethod()));
        assertThat(order.placedAt()).isEqualTo(persistenceEntity.getPlacedAt());
        assertThat(order.paidAt()).isEqualTo(persistenceEntity.getPaidAt());
        assertThat(order.canceledAt()).isEqualTo(persistenceEntity.getCanceledAt());
        assertThat(order.readyAt()).isEqualTo(persistenceEntity.getReadyAt());
        assertThat(order.items().size()).isEqualTo(persistenceEntity.getItems().size());
    }

    @Test
    void shouldMapBillingValueObject() {
        OrderPersistenceEntity persistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder().build();

        Order order = disassembler.toDomainEntity(persistenceEntity);

        assertThat(order.billing()).isNotNull();
        assertThat(order.billing().fullName().firstName()).isEqualTo(persistenceEntity.getBilling().getFirstName());
        assertThat(order.billing().fullName().lastName()).isEqualTo(persistenceEntity.getBilling().getLastName());
        assertThat(order.billing().email().email()).isEqualTo(persistenceEntity.getBilling().getEmail());
        assertThat(order.billing().phone().phone()).isEqualTo(persistenceEntity.getBilling().getPhone());
        assertThat(order.billing().document().document()).isEqualTo(persistenceEntity.getBilling().getDocument());
    }

    @Test
    void shouldMapBillingAddressValueObject() {
        OrderPersistenceEntity persistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder().build();

        Order order = disassembler.toDomainEntity(persistenceEntity);

        assertThat(order.billing().address()).isNotNull();
        assertThat(order.billing().address().street()).isEqualTo(persistenceEntity.getBilling().getAddress().getStreet());
        assertThat(order.billing().address().number()).isEqualTo(persistenceEntity.getBilling().getAddress().getNumber());
        assertThat(order.billing().address().complement()).isEqualTo(persistenceEntity.getBilling().getAddress().getComplement());
        assertThat(order.billing().address().neighborhood()).isEqualTo(persistenceEntity.getBilling().getAddress().getNeighborhood());
        assertThat(order.billing().address().city()).isEqualTo(persistenceEntity.getBilling().getAddress().getCity());
        assertThat(order.billing().address().state()).isEqualTo(persistenceEntity.getBilling().getAddress().getState());
        assertThat(order.billing().address().zipCode().zipcode()).isEqualTo(persistenceEntity.getBilling().getAddress().getZipCode());
    }

    @Test
    void shouldMapShippingValueObject() {
        OrderPersistenceEntity persistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder().build();

        Order order = disassembler.toDomainEntity(persistenceEntity);

        assertThat(order.shipping()).isNotNull();
        assertThat(order.shipping().cost()).isEqualTo(new Money(persistenceEntity.getShipping().getCost()));
        assertThat(order.shipping().expectedDate()).isEqualTo(persistenceEntity.getShipping().getExpectedDate());
    }

    @Test
    void shouldMapShippingRecipientValueObject() {
        OrderPersistenceEntity persistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder().build();

        Order order = disassembler.toDomainEntity(persistenceEntity);

        assertThat(order.shipping().recipient()).isNotNull();
        assertThat(order.shipping().recipient().fullName().firstName()).isEqualTo(persistenceEntity.getShipping().getRecipient().getFirstName());
        assertThat(order.shipping().recipient().fullName().lastName()).isEqualTo(persistenceEntity.getShipping().getRecipient().getLastName());
        assertThat(order.shipping().recipient().phone().phone()).isEqualTo(persistenceEntity.getShipping().getRecipient().getPhone());
        assertThat(order.shipping().recipient().document().document()).isEqualTo(persistenceEntity.getShipping().getRecipient().getDocument());
    }

    @Test
    void shouldMapShippingAddressValueObject() {
        OrderPersistenceEntity persistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder().build();

        Order order = disassembler.toDomainEntity(persistenceEntity);

        assertThat(order.shipping().address()).isNotNull();
        assertThat(order.shipping().address().street()).isEqualTo(persistenceEntity.getShipping().getAddress().getStreet());
        assertThat(order.shipping().address().number()).isEqualTo(persistenceEntity.getShipping().getAddress().getNumber());
        assertThat(order.shipping().address().complement()).isEqualTo(persistenceEntity.getShipping().getAddress().getComplement());
        assertThat(order.shipping().address().neighborhood()).isEqualTo(persistenceEntity.getShipping().getAddress().getNeighborhood());
        assertThat(order.shipping().address().city()).isEqualTo(persistenceEntity.getShipping().getAddress().getCity());
        assertThat(order.shipping().address().state()).isEqualTo(persistenceEntity.getShipping().getAddress().getState());
        assertThat(order.shipping().address().zipCode().zipcode()).isEqualTo(persistenceEntity.getShipping().getAddress().getZipCode());
    }

    @Test
    void shouldMapNullBillingAndShippingWhenAbsent() {
        OrderPersistenceEntity persistenceEntity = OrderPersistenceEntityTestDataBuilder.existingOrder()
                .billing(null)
                .shipping(null)
                .build();

        Order order = disassembler.toDomainEntity(persistenceEntity);

        assertThat(order.billing()).isNull();
        assertThat(order.shipping()).isNull();
    }
}
