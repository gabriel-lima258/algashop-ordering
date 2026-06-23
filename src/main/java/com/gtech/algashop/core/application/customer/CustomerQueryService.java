package com.gtech.algashop.core.application.customer;

import com.gtech.algashop.core.ports.in.customer.CustomerFilter;
import com.gtech.algashop.core.ports.in.customer.CustomerOutput;
import com.gtech.algashop.core.ports.in.customer.CustomerSummaryOutput;
import com.gtech.algashop.core.ports.in.customer.ForQueryCustomers;
import com.gtech.algashop.core.ports.out.customer.ForObtainingCustomers;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerQueryService implements ForQueryCustomers {

    private final ForObtainingCustomers forObtainingCustomers;

    @Override
    public CustomerOutput findById(UUID customerId) {
        return forObtainingCustomers.findById(customerId);
    }

    @Override
    public Page<CustomerSummaryOutput> filter(CustomerFilter filter) {
        return forObtainingCustomers.filter(filter);
    }
}
