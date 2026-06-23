package com.gtech.algashop.core.domain.model.order;

import com.gtech.algashop.core.domain.model.BusinessException;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_ORDER_DELIVERY_DATE_CANNOT_BE_IN_THE_PAST;

public class OrderInvalidShippingDeliveryDateException extends BusinessException {

    public OrderInvalidShippingDeliveryDateException(OrderId id) {
        super(String.format(ERROR_ORDER_DELIVERY_DATE_CANNOT_BE_IN_THE_PAST, id));
    }
}
