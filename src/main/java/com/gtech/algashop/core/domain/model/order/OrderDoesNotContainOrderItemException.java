package com.gtech.algashop.core.domain.model.order;

import com.gtech.algashop.core.domain.model.BusinessException;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_ORDER_DOES_NOT_CONTAIN_ITEM;

public class OrderDoesNotContainOrderItemException extends BusinessException {
    public OrderDoesNotContainOrderItemException(OrderId id, OrderItemId orderItemId) {
        super(String.format(ERROR_ORDER_DOES_NOT_CONTAIN_ITEM, id, orderItemId));
    }
}
