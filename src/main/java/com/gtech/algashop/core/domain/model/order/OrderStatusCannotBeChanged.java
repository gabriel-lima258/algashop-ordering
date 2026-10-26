package com.gtech.algashop.core.domain.model.order;

import com.gtech.algashop.core.domain.model.DomainException;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_ORDER_STATUS_CANNOT_BE_CHANGED;

public class OrderStatusCannotBeChanged extends DomainException {

    public OrderStatusCannotBeChanged(OrderId id , OrderStatus status, OrderStatus newStatus) {
        super(String.format(ERROR_ORDER_STATUS_CANNOT_BE_CHANGED, id, status, newStatus));
    }
}
