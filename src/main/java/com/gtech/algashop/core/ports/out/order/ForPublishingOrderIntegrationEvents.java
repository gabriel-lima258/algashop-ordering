package com.gtech.algashop.core.ports.out.order;

import com.gtech.algashop.core.application.OutboundIntegrationEvent;

public interface ForPublishingOrderIntegrationEvents {
    void send(OutboundIntegrationEvent event);
}
