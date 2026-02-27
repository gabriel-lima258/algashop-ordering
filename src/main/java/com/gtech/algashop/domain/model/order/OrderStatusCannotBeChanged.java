package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.BusinessException;

import static com.gtech.algashop.domain.model.ErrorMessages.ERROR_ORDER_STATUS_CANNOT_BE_CHANGED;

public class OrderStatusCannotBeChanged extends BusinessException {

    public OrderStatusCannotBeChanged(OrderId id , OrderStatus status, OrderStatus newStatus) {
        super(String.format(ERROR_ORDER_STATUS_CANNOT_BE_CHANGED, id, status, newStatus));
    }
}
