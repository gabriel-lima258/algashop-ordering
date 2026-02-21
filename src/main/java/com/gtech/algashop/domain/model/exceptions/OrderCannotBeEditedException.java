package com.gtech.algashop.domain.model.exceptions;

import com.gtech.algashop.domain.model.entity.OrderStatus;
import com.gtech.algashop.domain.model.entity.VO.id.OrderId;

import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.ERROR_ORDER_CANNOT_BE_EDITED;

public class OrderCannotBeEditedException extends BusinessException {
    public OrderCannotBeEditedException(OrderId orderId, OrderStatus status) {
        super(String.format(ERROR_ORDER_CANNOT_BE_EDITED, orderId, status));
    }
}
