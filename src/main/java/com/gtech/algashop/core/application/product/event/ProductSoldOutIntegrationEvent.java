package com.gtech.algashop.core.application.product.event;

import com.gtech.algashop.core.application.IntegrationEvent;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSoldOutIntegrationEvent implements IntegrationEvent {
    private UUID productId;
    private OffsetDateTime soldOutAt;

    @Override
    public String getAggregateId() {
        return productId.toString();
    }
}