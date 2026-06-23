package com.gtech.algashop.core.domain.model.order;

import com.gtech.algashop.core.domain.model.Specification;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.costumer.LoyaltyPoints;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomerHasEnoughLoyaltyPointsSpecification
        implements Specification<Customer> {

    private final LoyaltyPoints expectedLoyaltyPoints;

    // Spefication para verificar a quantidade de pontos de um cliente,
    // uma consulta simples e dinamica
    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return customer.loyaltyPoints().compareTo(expectedLoyaltyPoints) >= 0;
    }
}
