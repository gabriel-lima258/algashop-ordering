package com.gtech.algashop.infrastructure.shipping.client.rapidex;

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
