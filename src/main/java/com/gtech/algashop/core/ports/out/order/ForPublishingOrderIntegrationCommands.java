package com.gtech.algashop.core.ports.out.order;

import com.gtech.algashop.core.application.IntegrationCommand;

public interface ForPublishingOrderIntegrationCommands {
    void send(IntegrationCommand event);
}
