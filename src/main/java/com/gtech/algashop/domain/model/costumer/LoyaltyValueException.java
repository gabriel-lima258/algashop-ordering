package com.gtech.algashop.domain.model.costumer;
import com.gtech.algashop.domain.model.BusinessException;

import static com.gtech.algashop.domain.model.ErrorMessages.ERROR_LOYALTY_VALUE;

public class LoyaltyValueException extends BusinessException {

    public LoyaltyValueException() {
        super(ERROR_LOYALTY_VALUE);
    }

    public LoyaltyValueException(Throwable cause) {
        super(ERROR_LOYALTY_VALUE, cause);
    }
}
