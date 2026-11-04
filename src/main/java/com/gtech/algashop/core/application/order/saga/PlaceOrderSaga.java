package com.gtech.algashop.core.application.order.saga;

import com.gtech.algashop.core.application.saga.SagaStatus;
import com.gtech.algashop.core.application.saga.SagaTransitionException;
import com.gtech.algashop.core.domain.model.IdGenerator;
import com.gtech.algashop.core.domain.model.order.OrderId;

import java.util.Objects;
import java.util.UUID;

public class PlaceOrderSaga {

    private final UUID sagaId;
    private final OrderId orderId;
    private SagaStatus status;
    private PlaceOrderSagaStep step;
    private long version;

    private PlaceOrderSaga(UUID sagaId, OrderId orderId, SagaStatus status, PlaceOrderSagaStep step, long version) {
        this.sagaId = Objects.requireNonNull(sagaId);
        this.orderId = Objects.requireNonNull(orderId);
        this.status = Objects.requireNonNull(status);
        this.step = Objects.requireNonNull(step);
        this.version = version;
    }

    public static PlaceOrderSaga start(OrderId orderId) {
        return new PlaceOrderSaga(
                IdGenerator.generateTimeBasedUUID(),
                orderId,
                SagaStatus.RUNNING,
                PlaceOrderSagaStep.PLACING_ORDER,
                0L
        );
    }

    public static PlaceOrderSaga existing(UUID sagaId, OrderId orderId, SagaStatus status, PlaceOrderSagaStep step, long version) {
        return new PlaceOrderSaga(
                sagaId,
                orderId,
                status,
                step,
                version
        );
    }

    public boolean isRunningAt(PlaceOrderSagaStep step) {
        return this.status == SagaStatus.RUNNING && this.step == step;
    }

    public boolean isCompensatingAt(PlaceOrderSagaStep step) {
        return this.status == SagaStatus.COMPENSATING && this.step == step;
    }

    public boolean isActive() {
        return this.status == SagaStatus.RUNNING || this.status == SagaStatus.COMPENSATING;
    }

    public UUID sagaId() {
        return sagaId;
    }

    public OrderId orderId() {
        return orderId;
    }

    public SagaStatus status() {
        return status;
    }

    public PlaceOrderSagaStep step() {
        return step;
    }

    public long version() {
        return version;
    }

    private void require(boolean precondition) {
        if (!precondition) {
            throw SagaTransitionException.notAllowed(orderId(), status(), step());
        }
    }

    private void changeStatusTo(SagaStatus newStatus) {
        if (this.status().canNotChangeTo(newStatus)) {
            throw SagaTransitionException.statusChange(orderId(), status(), newStatus);
        }
        this.status = newStatus;
    }

    private void changeStepTo(PlaceOrderSagaStep newStep) {
        if (this.step().canNotChangeTo(newStep)) {
            throw SagaTransitionException.stepChange(orderId(), step(), newStep);
        }
        this.step = newStep;
    }

    @Override
    public String toString() {
        return "PlaceOrderSaga{" +
                "sagaId=" + sagaId +
                ", orderId=" + orderId +
                ", status=" + status +
                ", step=" + step +
                ", version=" + version +
                '}';
    }
}
