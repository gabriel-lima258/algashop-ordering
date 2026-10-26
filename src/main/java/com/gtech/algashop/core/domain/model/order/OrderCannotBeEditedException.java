package com.gtech.algashop.core.domain.model.order;

import com.gtech.algashop.core.domain.model.DomainException;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_ORDER_CANNOT_BE_EDITED;

public class OrderCannotBeEditedException extends DomainException {
    public OrderCannotBeEditedException(OrderId orderId, OrderStatus status) {
        super(String.format(ERROR_ORDER_CANNOT_BE_EDITED, orderId, status));
    }
}
