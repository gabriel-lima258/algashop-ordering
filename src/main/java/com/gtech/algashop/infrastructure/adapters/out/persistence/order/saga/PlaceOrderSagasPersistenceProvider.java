package com.gtech.algashop.infrastructure.adapters.out.persistence.order.saga;

import com.gtech.algashop.core.application.order.saga.PlaceOrderSaga;
import com.gtech.algashop.core.domain.model.order.OrderId;
import com.gtech.algashop.core.ports.out.order.saga.ForStoringPlaceOrderSagas;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceOrderSagasPersistenceProvider implements ForStoringPlaceOrderSagas {

    private final PlaceOrderSagaPersistenceEntityRepository repository;

    @Override
    public Optional<PlaceOrderSaga> ofId(UUID sagaId) {
        return repository.findById(sagaId).map(this::toSaga);
    }

    @Override
    @Transactional(readOnly = false)
    public void add(PlaceOrderSaga saga) {
        repository.saveAndFlush(toEntity(saga));
    }

    private PlaceOrderSagaPersistenceEntity toEntity(PlaceOrderSaga saga) {
        PlaceOrderSagaPersistenceEntity entity = new PlaceOrderSagaPersistenceEntity();
        entity.setSagaId(saga.sagaId());
        entity.setAggregateId(saga.orderId().toString());
        entity.setStatus(saga.status());
        entity.setStep(saga.step());
        entity.setVersion(saga.version());
        return entity;
    }

    private PlaceOrderSaga toSaga(PlaceOrderSagaPersistenceEntity entity) {
        return PlaceOrderSaga.existing(
                entity.getSagaId(),
                new OrderId(entity.getAggregateId()),
                entity.getStatus(),
                entity.getStep(),
                entity.getVersion()
        );
    }
}
