package com.gtech.algashop.infrastructure.adapters.in.web.shipping.client.http;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.PostExchange;

// uma interface declarativa → o Spring gera o client HTTP.
public interface RapiDexAPICLient {

    @PostExchange("/api/delivery-cost")
    DeliveryCostResponse calculate(@RequestBody  DeliveryCostRequest request);
}
