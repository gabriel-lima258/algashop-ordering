package com.gtech.algashop.core.ports.in.checkout;

import com.gtech.algashop.core.application.checkout.command.ProcessAcceptedCheckoutIntegrationCommand;

public interface ForProcessingCheckoutAccepted {
    void process(ProcessAcceptedCheckoutIntegrationCommand integrationCommand);
}
