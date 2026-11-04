package com.gtech.algashop.core.application.order.saga;

import com.gtech.algashop.core.domain.model.order.OrderId;
import com.gtech.algashop.core.ports.in.order.saga.ForCoordinatingPlaceOrderSaga;
import com.gtech.algashop.core.ports.out.order.saga.ForStoringPlaceOrderSagas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlaceOrderSagaCoordinator implements ForCoordinatingPlaceOrderSaga {

    private final ForStoringPlaceOrderSagas sagas;

    @Override
    @Transactional
    public void start(String rawOrderId) {
        OrderId orderId = new OrderId(rawOrderId);
        PlaceOrderSaga saga = PlaceOrderSaga.start(orderId);
        sagas.add(saga);

        log.info("Place order saga started: saga={} order={}", saga.sagaId(), saga.orderId());
    }
}
