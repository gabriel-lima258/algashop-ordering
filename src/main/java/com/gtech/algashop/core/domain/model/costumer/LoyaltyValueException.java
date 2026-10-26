package com.gtech.algashop.core.domain.model.costumer;
import com.gtech.algashop.core.domain.model.DomainException;

import static com.gtech.algashop.core.domain.model.ErrorMessages.ERROR_LOYALTY_VALUE;

public class LoyaltyValueException extends DomainException {

    public LoyaltyValueException() {
        super(ERROR_LOYALTY_VALUE);
    }

    public LoyaltyValueException(Throwable cause) {
        super(ERROR_LOYALTY_VALUE, cause);
    }
}
