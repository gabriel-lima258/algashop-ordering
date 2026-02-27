package com.gtech.algashop.application.checkout;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.Orders;
import com.gtech.algashop.domain.model.order.shipping.ShippingCostService;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;


@Transactional
@SpringBootTest
class BuyNowApplicationServiceIT {

    @Autowired
    private BuyNowApplicationService buyNowApplicationService;

    @Autowired
    private Orders orders;

    @Autowired
    private Customers customers;

    // cenario ficticios de mock
    @MockitoBean
    private ProductCatalogService productCatalogService;

    @MockitoBean
    private ShippingCostService shippingCostService;

    @BeforeEach
    void setUp() {
        // cadastra customers antes de cada teste
        if (!customers.exists(CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID)) {
            customers.add(CustomerTestDataBuilder.existingCustomer().build());
        }
    }

    @Test
    void shouldBuyNow() {
        Product product = ProductTestDataBuilder.aProduct()
                .id(ProductTestDataBuilder.DEFAULT_PRODUCT_ID)
                .build();

        // criando comportamento do mock
        Mockito.when(productCatalogService.ofId(product.id())).thenReturn(Optional.of(product));
        Mockito.when(shippingCostService.calculate(Mockito.any(ShippingCostService.CalculationRequest.class)))
                .thenReturn(new ShippingCostService.CalculationResult(
                        new Money("10.00"),
                        LocalDate.now().plusDays(3)
                ));

        BuyNowInput input = BuyNowInputTestDataBuilder.aBuyNowInput().build();

        String orderId = buyNowApplicationService.buyNow(input);

        Assertions.assertThat(orderId).isNotBlank();
        Assertions.assertThat(orders.exists(new
                OrderId(orderId))).isTrue();
    }

}