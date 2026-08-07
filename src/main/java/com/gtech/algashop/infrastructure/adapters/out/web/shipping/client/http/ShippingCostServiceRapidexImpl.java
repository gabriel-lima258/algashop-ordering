package com.gtech.algashop.infrastructure.adapters.out.web.shipping.client.http;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.order.shipping.ShippingCostService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

// So ADAPTA: implementa a porta do dominio e traduz o DTO da Rapidex para o modelo de
// frete. Cache, retry, circuito e traducao de erro moram no ResilientShippingCostAPIClient
// - mesma divisao do ProductCatalogServiceHttpImpl com o cliente do catalogo.
//
// Nao ha checagem de null aqui de proposito: o cliente resiliente garante que ou vem uma
// resposta valida, ou sobe excecao. Se ele voltar a devolver null em algum caminho, o NPE
// aparece nesta linha e a stack aponta para o lugar errado.
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "algashop.integrations.shipping.provider", havingValue = "RAPIDEX")
public class ShippingCostServiceRapidexImpl implements ShippingCostService {

    private final ResilientShippingCostAPIClient rapiDexAPICLient;

    @Override
    public CalculationResult calculate(CalculationRequest request) {

        DeliveryCostResponse response = rapiDexAPICLient.calculate(
                new DeliveryCostRequest(
                        request.origin().zipcode(),
                        request.destination().zipcode()
                )
        );

        LocalDate expectedDeliveryDate = LocalDate.now().plusDays(response.getEstimatedDaysToDeliver());

        return CalculationResult.builder()
                .cost(new Money(response.getDeliveryCost()))
                .expectedDate(expectedDeliveryDate)
                .build();
    }
}
