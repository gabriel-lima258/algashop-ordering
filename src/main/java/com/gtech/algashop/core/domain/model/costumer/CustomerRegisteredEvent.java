package com.gtech.algashop.core.domain.model.costumer;

import com.gtech.algashop.core.domain.model.commons.Email;
import com.gtech.algashop.core.domain.model.commons.FullName;

import java.time.OffsetDateTime;

public record CustomerRegisteredEvent(CustomerId customerId,
                                      OffsetDateTime registeredAt,
                                      FullName fullName,
                                      Email email) {
}
