package com.gtech.algashop.core.application.customer;

import com.gtech.algashop.core.ports.in.customer.CustomerOutput;
import com.gtech.algashop.core.ports.in.customer.ForConfirmCustomerRegistration;
import com.gtech.algashop.core.ports.out.customer.ForNotifyingCustomers;
import com.gtech.algashop.core.ports.out.customer.ForObtainingCustomers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerRegistrationConfirmationApplicationService implements ForConfirmCustomerRegistration {

    private final ForNotifyingCustomers forNotifyingCustomers;
    private final ForObtainingCustomers forObtainingCustomers;

    @Override
    public void confirm(UUID customerId) {
        CustomerOutput customerOutput = forObtainingCustomers.findById(customerId);
        var input = new ForNotifyingCustomers.NotifyNewRegistrationInput(
                customerOutput.getId(),
                customerOutput.getFirstName(),
                customerOutput.getEmail()
        );
        forNotifyingCustomers.notificateNewRegistration(input);
    }
}
