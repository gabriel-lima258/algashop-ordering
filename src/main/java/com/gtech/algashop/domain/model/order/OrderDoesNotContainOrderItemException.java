package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.BusinessException;

import static com.gtech.algashop.domain.model.ErrorMessages.ERROR_ORDER_DOES_NOT_CONTAIN_ITEM;

public class OrderDoesNotContainOrderItemException extends BusinessException {
    public OrderDoesNotContainOrderItemException(OrderId id, OrderItemId orderItemId) {
        super(String.format(ERROR_ORDER_DOES_NOT_CONTAIN_ITEM, id, orderItemId));
    }
}
