package com.gtech.algashop.infrastructure.adapters.out.web.shipping.client.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryCostResponse {
    private String deliveryCost;
    private Long estimatedDaysToDeliver;
}
