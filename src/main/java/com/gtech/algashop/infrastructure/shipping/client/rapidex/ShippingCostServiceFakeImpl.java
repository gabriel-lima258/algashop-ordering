package com.gtech.algashop.infrastructure.shipping.client.rapidex;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.order.shipping.ShippingCostService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@ConditionalOnProperty(name = "algashop.integrations.shipping.provider", havingValue = "FAKE")
public class ShippingCostServiceFakeImpl implements ShippingCostService {

    @Override
    public CalculationResult calculate(CalculationRequest request) {
        return new CalculationResult(
                new Money("20"),
                LocalDate.now().plusDays(5)
        );
    }
}
