package com.gtech.algashop.core.application.product.event;

import com.gtech.algashop.core.application.InboundIntegrationEvent;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSoldOutIntegrationEvent implements InboundIntegrationEvent {
    private UUID productId;
    private OffsetDateTime soldOutAt;
}