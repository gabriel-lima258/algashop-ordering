package com.gtech.algashop.contract.base;

import com.gtech.algashop.application.checkout.BuyNowApplicationService;
import com.gtech.algashop.application.checkout.BuyNowInput;
import com.gtech.algashop.application.checkout.CheckoutApplicationService;
import com.gtech.algashop.application.checkout.CheckoutInput;
import com.gtech.algashop.application.order.query.OrderDetailOutputTestDataBuilder;
import com.gtech.algashop.application.order.query.OrderFilter;
import com.gtech.algashop.application.order.query.OrderQueryService;
import com.gtech.algashop.application.order.query.OrderSummaryOutputTestDataBuilder;
import com.gtech.algashop.domain.model.order.OrderNotFoundException;
import com.gtech.algashop.presentation.order.OrderController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;

@WebMvcTest(controllers = OrderController.class)
public class OrderBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private OrderQueryService orderQueryService;

    @MockitoBean
    private BuyNowApplicationService buyNowApplicationService;

    @MockitoBean
    private CheckoutApplicationService checkoutApplicationService;

    public static final String validOrderId = "01226N0640J7Q";
    public static final String invalidOrderId = "01226N0693HDH";

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(
                MockMvcBuilders.webAppContextSetup(context)
                        .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                        .build()
        );

        RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();

        mockFindAllOrderByFilter();
        mockValidOrderFindById();
        mockInvalidOrderIdNotFound();
        mockCreateOrderWithProduct();
        mockCreateOrderWithShoppingCart();
    }

    private void mockValidOrderFindById() {
        Mockito.when(orderQueryService.findById(validOrderId))
                .thenReturn(OrderDetailOutputTestDataBuilder.placedOrder(validOrderId).build());
    }

    private void mockInvalidOrderIdNotFound() {
        Mockito.when(orderQueryService.findById(invalidOrderId))
                .thenThrow(new OrderNotFoundException());
    }

    public void mockCreateOrderWithProduct() {
        Mockito.when(buyNowApplicationService.buyNow(Mockito.any(BuyNowInput.class)))
                .thenReturn(validOrderId);
    }

    public void mockCreateOrderWithShoppingCart() {
        Mockito.when(checkoutApplicationService.checkout(Mockito.any(CheckoutInput.class)))
                .thenReturn(validOrderId);
    }

    public void mockFindAllOrderByFilter() {
        Mockito.when(orderQueryService.filter(Mockito.any(OrderFilter.class)))
                .thenReturn(new PageImpl<>(
                        List.of(OrderSummaryOutputTestDataBuilder.placedOrder().build())
                ));
    }
}
