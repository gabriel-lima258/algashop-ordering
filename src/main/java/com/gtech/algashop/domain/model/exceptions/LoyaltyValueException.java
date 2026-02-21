package com.gtech.algashop.domain.model.exceptions;
import static com.gtech.algashop.domain.model.exceptions.ErrorMessages.ERROR_LOYALTY_VALUE;

public class LoyaltyValueException extends BusinessException {

    public LoyaltyValueException() {
        super(ERROR_LOYALTY_VALUE);
    }

    public LoyaltyValueException(Throwable cause) {
        super(ERROR_LOYALTY_VALUE, cause);
    }
}
