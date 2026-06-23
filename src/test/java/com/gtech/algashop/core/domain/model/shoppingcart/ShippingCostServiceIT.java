package com.gtech.algashop.core.domain.model.shoppingcart;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.gtech.algashop.core.domain.model.AbstractDomainIT;
import com.gtech.algashop.core.domain.model.commons.ZipCode;
import com.gtech.algashop.core.domain.model.order.shipping.OriginAddressService;
import com.gtech.algashop.core.domain.model.order.shipping.ShippingCostService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

class ShippingCostServiceIT extends AbstractDomainIT {

    @Autowired
    private ShippingCostService shippingCostService;

    @Autowired
    private OriginAddressService originAddressService;

    private WireMockServer wireMockRapidex;

    @BeforeEach
    public void setup() {
        initWireMock();
    }

    @AfterEach
    public void clean() {
        wireMockRapidex.stop();
    }

    private void initWireMock() {
        wireMockRapidex = new WireMockServer(options()
                .port(8780)
                .usingFilesUnderDirectory("src/test/resources/wiremock/rapidex")
                .extensions(new ResponseTemplateTransformer(true)));

        wireMockRapidex.start();
    }

    @Test
    void shouldCalculate() {
        ZipCode origin = originAddressService.originAddress().zipCode();
        ZipCode destination = new ZipCode("12345");

        var calculate = shippingCostService
                .calculate(new ShippingCostService.CalculationRequest(origin, destination));

        Assertions.assertThat(calculate.cost()).isNotNull();
        Assertions.assertThat(calculate.expectedDate()).isNotNull();
    }


}