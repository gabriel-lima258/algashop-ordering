package com.gtech.algashop.core.application.order.saga;

import com.gtech.algashop.core.application.saga.SagaStatus;
import com.gtech.algashop.core.application.saga.SagaTransitionException;
import com.gtech.algashop.core.domain.model.order.OrderId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PlaceOrderSagaTest {

    private PlaceOrderSaga invoicingSaga() {
        PlaceOrderSaga saga = PlaceOrderSaga.start(new OrderId());
        saga.moveToInvoicing();
        return saga;
    }

    @Test
    void shouldCompensateWhenPaymentIsRefused() {
        PlaceOrderSaga saga = invoicingSaga();

        saga.failWithPaymentRefused();
        saga.compensateOrder();
        saga.endCompensated();

        Assertions.assertWith(saga,
                s -> Assertions.assertThat(s.status()).isEqualTo(SagaStatus.COMPENSATED),
                s -> Assertions.assertThat(s.step()).isEqualTo(PlaceOrderSagaStep.CANCELLING_ORDER),
                s -> Assertions.assertThat(s.failure()).isEqualTo(PlaceOrderSagaFailure.PAYMENT_REFUSED),
                s -> Assertions.assertThat(s.isCompensated()).isTrue());
    }

    @Test
    void shouldNotCompensateOrderWhileStillRunning() {
        PlaceOrderSaga saga = invoicingSaga();

        Assertions.assertThatThrownBy(saga::compensateOrder)
                .isInstanceOf(SagaTransitionException.class);
    }

    @Test
    void shouldSucceedThroughTheHappyPath() {
        PlaceOrderSaga saga = invoicingSaga();

        saga.moveToReservingStock();
        saga.moveToApprovingOrder();
        saga.endSucceeded();

        Assertions.assertWith(saga,
                s -> Assertions.assertThat(s.status()).isEqualTo(SagaStatus.SUCCEEDED),
                s -> Assertions.assertThat(s.step()).isEqualTo(PlaceOrderSagaStep.APPROVING_ORDER),
                s -> Assertions.assertThat(s.failure()).isNull());
    }
}
