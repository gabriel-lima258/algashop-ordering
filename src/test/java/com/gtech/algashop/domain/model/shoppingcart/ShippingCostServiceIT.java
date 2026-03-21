package com.gtech.algashop.domain.model.shoppingcart;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.commons.ZipCode;
import com.gtech.algashop.domain.model.order.shipping.OriginAddressService;
import com.gtech.algashop.domain.model.order.shipping.ShippingCostService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@SpringBootTest
@TestPropertySource(properties = "algashop.integrations.shipping.provider=RAPIDEX")
class ShippingCostServiceIT {

    @Autowired
    private ShippingCostService shippingCostService;

    @Autowired
    private OriginAddressService originAddressService;

    private WireMockServer wireMockRapidex;

    @BeforeEach
    void setup() {
        wireMockRapidex = new WireMockServer(options()
                .port(8780)
                .usingFilesUnderDirectory("src/test/resources/wiremock/rapidex")
                .extensions(new ResponseTemplateTransformer(true)));
        wireMockRapidex.start();
    }

    @AfterEach
    void teardown() {
        wireMockRapidex.stop();
    }

    @Test
    void shouldCalculate() {
        ZipCode origin = originAddressService.originAddress().zipCode();
        ZipCode destination = new ZipCode("12345");

        var calculate = shippingCostService
                .calculate(new ShippingCostService.CalculationRequest(origin, destination));

        Assertions.assertThat(calculate.cost()).isNotNull();
        Assertions.assertThat(calculate.cost()).isEqualTo(new Money(new BigDecimal("35.00")));
        Assertions.assertThat(calculate.expectedDate()).isNotNull();
        Assertions.assertThat(calculate.expectedDate()).isEqualTo(LocalDate.now().plusDays(7));
    }

}