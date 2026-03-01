package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.AbstractEventSourceEntity;
import com.gtech.algashop.domain.model.AggregateRoot;
import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.product.Product;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Order
        extends AbstractEventSourceEntity
        implements AggregateRoot<OrderId> {

    private OrderId id;
    private CustomerId customerId;
    private Money totalAmount;
    private Quantity totalQuantity;
    private OffsetDateTime placedAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime canceledAt;
    private OffsetDateTime readyAt;
    private Billing billing;
    private Shipping shipping;
    private OrderStatus status;
    private PaymentMethod paymentMethod;

    private Set<OrderItem> items;

    // controle de concorrencia no banco
    private Long version;

    @Builder(builderClassName = "ExistingOrderBuilder", builderMethodName = "existing")
    public Order(OrderId id, Long version, CustomerId customerId, Money totalAmount, Quantity totalQuantity,
                 OffsetDateTime placedAt, OffsetDateTime paidAt, OffsetDateTime canceledAt,
                 OffsetDateTime readyAt, Billing billing, Shipping shipping, OrderStatus status,
                 PaymentMethod paymentMethod,
                 Set<OrderItem> items) {
        this.setId(id);
        this.setVersion(version);
        this.setCustomerId(customerId);
        this.setTotalAmount(totalAmount);
        this.setTotalQuantity(totalQuantity);
        this.setPlacedAt(placedAt);
        this.setPaidAt(paidAt);
        this.setCanceledAt(canceledAt);
        this.setReadyAt(readyAt);
        this.setBilling(billing);
        this.setShipping(shipping);
        this.setStatus(status);
        this.setPaymentMethod(paymentMethod);
        this.setItems(items);
    }

    /////////////////////////////////////
    ///  STATIC FACTORIES - GOF
    ////////////////////////////////////

    // Cria um novo Order no estado inicial (DRAFT)
    // Garante invariantes:
    // - id gerado automaticamente
    // - total zerado
    // - lista de itens vazia
    // - status DRAFT
    public static Order draft(CustomerId customerId) {
        return new Order(
                new OrderId(),
                null,
                customerId,
                Money.ZERO,
                Quantity.ZERO,
                null,
                null,
                null,
                null,
                null,
                null,
                OrderStatus.DRAFT,
                null,
                new HashSet<>()
        );
    }

    /////////////////////////////////////
    ///  METHODS
    ////////////////////////////////////

    public void addItem(Product product, Quantity quantity) {
        Objects.requireNonNull(product);
        Objects.requireNonNull(quantity);
        verifyIsChangeble();

        product.checkOutOfStock();

        OrderItem orderItem = OrderItem.brandNew()
                .orderId(this.id())
                .product(product)
                .quantity(quantity)
                .build();

        if (this.items == null) {
            this.items = new HashSet<>();
        }

        this.items.add(orderItem);

        this.recalculateTotals();
    }

    public void removeItem(OrderItemId orderItemId) {
        Objects.requireNonNull(orderItemId);
        verifyIsChangeble();

        OrderItem orderItem = findOrderItem(orderItemId);
        this.items.remove(orderItem);

        this.recalculateTotals();
    }

    private void recalculateTotals() {
        BigDecimal totalItemAmount = this.items().stream().map(i -> i.totalAmount().money())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer quantityItems = this.items().stream().map(i -> i.quantity().quantity())
                .reduce(0, Integer::sum);

        BigDecimal shippingCostValue;
        if (this.shipping() == null) {
            shippingCostValue = BigDecimal.ZERO;
        } else {
            shippingCostValue = this.shipping.cost().money();
        }

        BigDecimal totalAmountValue = totalItemAmount.add(shippingCostValue);

        this.setTotalAmount(new Money(totalAmountValue));
        this.setTotalQuantity(new Quantity(quantityItems));
    }

    private void verifyIsChangeble() {
        if (!this.isDraft()) {
            throw new OrderCannotBeEditedException(this.id(), this.status());
        }
    }

    /////////////////////////////////////
    ///  MACHINE STATE
    ////////////////////////////////////

    public void markAsPlaced() {
        this.verififyCanChangeToPlace();
        this.changedStatus(OrderStatus.PLACED);
        this.setPlacedAt(OffsetDateTime.now());
        this.publishDomainEvent(new OrderPlacedEvent(this.id(), this.customerId(), this.placedAt()));
    }

    public void markAsPaid() {
        this.changedStatus(OrderStatus.PAID);
        this.setPaidAt(OffsetDateTime.now());
        this.publishDomainEvent(new OrderPaidEvent(this.id(), this.customerId(), this.paidAt()));
    }

    public void markAsReady() {
        this.changedStatus(OrderStatus.READY);
        this.setReadyAt(OffsetDateTime.now());
        this.publishDomainEvent(new OrderReadyEvent(this.id(), this.customerId(), this.readyAt()));
    }

    public void markAsCanceled() {
        this.changedStatus(OrderStatus.CANCELED);
        this.setCanceledAt(OffsetDateTime.now());
        this.publishDomainEvent(new OrderCanceledEvent(this.id(), this.customerId(), this.canceledAt()));
    }

    public boolean isDraft() {
        return OrderStatus.DRAFT.equals(this.status());
    }

    public boolean isPlaced() {
        return OrderStatus.PLACED.equals(this.status());
    }

    public boolean isPaid() {
        return OrderStatus.PAID.equals(this.status());
    }

    public boolean isReady() {
        return OrderStatus.READY.equals(this.status());
    }

    public boolean isCanceled() {
        return OrderStatus.CANCELED.equals(this.status());
    }

    public void changePaymentMethod(PaymentMethod paymentMethod) {
        Objects.requireNonNull(paymentMethod);
        verifyIsChangeble();
        this.setPaymentMethod(paymentMethod);
    }

    // para alterar endereco deve rever o valor custo de entrega e a data de entrega
    public void changeShipping(Shipping newShipping) {
        Objects.requireNonNull(newShipping);
        verifyIsChangeble();

        // data é antes da atual data?
        if (newShipping.expectedDate().isBefore(LocalDate.now())) {
            throw new OrderInvalidShippingDeliveryDateException(this.id());
        }

        this.setShipping(newShipping);
        this.recalculateTotals();
    }

    public void changeBilling(Billing billing) {
        Objects.requireNonNull(billing);
        verifyIsChangeble();
        this.setBilling(billing);
    }

    public void changeItemQuantity(OrderItemId orderItemId, Quantity quantity) {
        Objects.requireNonNull(orderItemId);
        Objects.requireNonNull(quantity);
        verifyIsChangeble();

        OrderItem orderItem = findOrderItem(orderItemId);
        orderItem.changeQuantity(quantity);

        this.recalculateTotals();
    }

    /////////////////////////////////////
    ///  GETTERS
    ////////////////////////////////////

    public OrderId id() {
        return id;
    }

    public Long version() {
        return version;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public Money totalAmount() {
        return totalAmount;
    }

    public Quantity totalQuantity() {
        return totalQuantity;
    }

    public OffsetDateTime placedAt() {
        return placedAt;
    }

    public OffsetDateTime paidAt() {
        return paidAt;
    }

    public OffsetDateTime canceledAt() {
        return canceledAt;
    }

    public OffsetDateTime readyAt() {
        return readyAt;
    }

    public Billing billing() {
        return billing;
    }

    public Shipping shipping() {
        return shipping;
    }

    public OrderStatus status() {
        return status;
    }

    public PaymentMethod paymentMethod() {
        return paymentMethod;
    }

    public Set<OrderItem> items() {
        // garatindo a imutabilidade do set
        return Collections.unmodifiableSet(this.items);
    }

    /////////////////////////////////////
    ///  SETTERS
    ////////////////////////////////////

    private void setVersion(Long version) {
        this.version = version;
    }

    private void setId(OrderId id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    private void setCustomerId(CustomerId customerId) {
        Objects.requireNonNull(customerId);
        this.customerId = customerId;
    }

    private void setTotalAmount(Money totalAmount) {
        Objects.requireNonNull(totalAmount);
        this.totalAmount = totalAmount;
    }

    private void setTotalQuantity(Quantity totalQuantity) {
        Objects.requireNonNull(totalQuantity);
        this.totalQuantity = totalQuantity;
    }

    private void setPlacedAt(OffsetDateTime placedAt) {
        this.placedAt = placedAt;
    }

    private void setPaidAt(OffsetDateTime paidAt) {
        this.paidAt = paidAt;
    }

    private void setCanceledAt(OffsetDateTime canceledAt) {
        this.canceledAt = canceledAt;
    }

    private void setReadyAt(OffsetDateTime readyAt) {
        this.readyAt = readyAt;
    }

    private void setBilling(Billing billing) {
        this.billing = billing;
    }

    private void setShipping(Shipping shipping) {
        this.shipping = shipping;
    }

    private void setStatus(OrderStatus status) {
        Objects.requireNonNull(status);
        this.status = status;
    }

    private void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    private void setItems(Set<OrderItem> items) {
        this.items = items;
    }

    /////////////////////////////////////
    ///  HELPERS
    ////////////////////////////////////

    // exception customizada para cada tipo de null
    private void verififyCanChangeToPlace() {
        if (this.shipping() == null) {
            throw OrderCannotBePlacedException.noShippingInfo(this.id());
        }
        if (this.billing() == null) {
            throw OrderCannotBePlacedException.noBillingInfo(this.id());
        }
        if (this.items() == null) {
            throw OrderCannotBePlacedException.noItems(this.id());
        }
        if (this.paymentMethod() == null) {
            throw OrderCannotBePlacedException.noPaymentMethod(this.id());
        }
    }

    private OrderItem findOrderItem(OrderItemId orderItemId) {
        return this.items().stream()
                .filter(i -> i.id().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new OrderDoesNotContainOrderItemException(this.id(), orderItemId));
    }

    private void changedStatus(OrderStatus newStatus) {
        Objects.requireNonNull(newStatus);
        if (this.status().canNotChangeTo(newStatus)) {
            throw new OrderStatusCannotBeChanged(this.id(), this.status(), newStatus);
        }
        this.setStatus(newStatus);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
