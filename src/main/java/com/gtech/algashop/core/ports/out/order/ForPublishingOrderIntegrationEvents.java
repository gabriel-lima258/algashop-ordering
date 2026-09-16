package com.gtech.algashop.core.ports.out.order;

import com.gtech.algashop.core.application.IntegrationEvent;

public interface ForPublishingOrderIntegrationEvents {
    void send(IntegrationEvent event);
}
