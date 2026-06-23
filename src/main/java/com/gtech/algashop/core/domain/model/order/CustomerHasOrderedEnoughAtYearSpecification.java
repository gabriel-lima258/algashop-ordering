package com.gtech.algashop.core.domain.model.order;

import com.gtech.algashop.core.domain.model.Specification;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import lombok.RequiredArgsConstructor;

import java.time.Year;

@RequiredArgsConstructor
public class CustomerHasOrderedEnoughAtYearSpecification
        implements Specification<Customer> {

    private final Orders orders;
    private final long expectedOrderCount;

    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return orders.salesQuantityByCustomerInYear(
                customer.id(),
                Year.now()
        ) >= expectedOrderCount;
    }
}
