package com.gtech.algashop.core.ports.in.customer;

import java.util.UUID;

public interface ForManagingCustomer {
    UUID create(CustomerInput input);
    void update(UUID customerId, CustomerUpdateInput input);
    void archive(UUID customerId);
    void changeEmail(UUID customerId, String newEmail);
}
