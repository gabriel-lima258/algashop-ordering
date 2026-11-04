package com.gtech.algashop.infrastructure.adapters.out.persistence.order.saga;

import com.gtech.algashop.core.application.order.saga.PlaceOrderSagaStep;
import com.gtech.algashop.infrastructure.adapters.out.persistence.saga.SagaInstancePersistenceEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true, of = "step")
@DiscriminatorValue(PlaceOrderSagaPersistenceEntity.SAGA_TYPE)
public class PlaceOrderSagaPersistenceEntity extends SagaInstancePersistenceEntity {

    public static final String SAGA_TYPE = "PLACE_ORDER";

    @Enumerated(EnumType.STRING)
    private PlaceOrderSagaStep step;
}
