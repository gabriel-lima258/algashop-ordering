package com.gtech.algashop.domain.model.exceptions;

import com.gtech.algashop.domain.model.entity.VO.id.OrderId;
import com.gtech.algashop.domain.model.entity.VO.id.OrderItemId;

import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.ERROR_ORDER_DOES_NOT_CONTAIN_ITEM;

public class OrderDoesNotContainOrderItemException extends BusinessException {
    public OrderDoesNotContainOrderItemException(OrderId id, OrderItemId orderItemId) {
        super(String.format(ERROR_ORDER_DOES_NOT_CONTAIN_ITEM, id, orderItemId));
    }
}
