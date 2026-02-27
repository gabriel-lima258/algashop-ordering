package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.BusinessException;

import static com.gtech.algashop.domain.model.ErrorMessages.ERROR_ORDER_CANNOT_BE_EDITED;

public class OrderCannotBeEditedException extends BusinessException {
    public OrderCannotBeEditedException(OrderId orderId, OrderStatus status) {
        super(String.format(ERROR_ORDER_CANNOT_BE_EDITED, orderId, status));
    }
}
