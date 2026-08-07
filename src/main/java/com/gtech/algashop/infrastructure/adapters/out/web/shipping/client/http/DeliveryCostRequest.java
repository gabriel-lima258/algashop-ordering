package com.gtech.algashop.infrastructure.adapters.out.web.shipping.client.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// contexto da api em request
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryCostRequest {
    private String originZipCode;
    private String destinationZipCode;
}
