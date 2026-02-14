package com.gtech.algashop.domain.exceptions;

import com.gtech.algashop.domain.entity.VO.id.OrderId;

import static com.gtech.algashop.domain.exceptions.ErrorMessages.ERROR_ORDER_CANNOT_BE_PLACED_WITH_NO_ITEMS;

public class OrderCannotBePlacedException extends RuntimeException {

    public OrderCannotBePlacedException(OrderId id) {
        super(String.format(ERROR_ORDER_CANNOT_BE_PLACED_WITH_NO_ITEMS, id));
    }
}
