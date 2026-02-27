package com.gtech.algashop.domain.model.exceptions;

import com.gtech.algashop.domain.model.BusinessException;
import com.gtech.algashop.domain.model.order.OrderStatus;
import com.gtech.algashop.domain.model.entity.VO.id.OrderId;

import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.ERROR_ORDER_STATUS_CANNOT_BE_CHANGED;

public class OrderStatusCannotBeChanged extends BusinessException {

    public OrderStatusCannotBeChanged(OrderId id , OrderStatus status, OrderStatus newStatus) {
        super(String.format(ERROR_ORDER_STATUS_CANNOT_BE_CHANGED, id, status, newStatus));
    }
}
