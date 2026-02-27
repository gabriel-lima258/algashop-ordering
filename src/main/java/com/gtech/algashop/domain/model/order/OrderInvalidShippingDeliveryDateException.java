package com.gtech.algashop.domain.model.exceptions;

import com.gtech.algashop.domain.model.BusinessException;
import com.gtech.algashop.domain.model.entity.VO.id.OrderId;

import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.ERROR_ORDER_DELIVERY_DATE_CANNOT_BE_IN_THE_PAST;

public class OrderInvalidShippingDeliveryDateException extends BusinessException {

    public OrderInvalidShippingDeliveryDateException(OrderId id) {
        super(String.format(ERROR_ORDER_DELIVERY_DATE_CANNOT_BE_IN_THE_PAST, id));
    }
}
