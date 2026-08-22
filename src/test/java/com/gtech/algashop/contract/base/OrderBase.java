package com.gtech.algashop.contract.base;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.ports.in.checkout.BuyNowInput;
import com.gtech.algashop.core.ports.in.checkout.CheckoutInput;
import com.gtech.algashop.core.ports.in.checkout.ForBuyingProduct;
import com.gtech.algashop.core.ports.in.checkout.ForBuyingWithShoppingCart;
import com.gtech.algashop.core.application.order.query.OrderDetailOutputTestDataBuilder;
import com.gtech.algashop.core.ports.in.order.OrderFilter;
import com.gtech.algashop.core.ports.in.order.ForQueryOrders;
import com.gtech.algashop.core.application.order.query.OrderSummaryOutputTestDataBuilder;
import com.gtech.algashop.core.domain.model.order.OrderNotFoundException;
import com.gtech.algashop.infrastructure.adapters.in.web.order.MyOrderController;
import com.gtech.algashop.infrastructure.adapters.in.web.order.OrderController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

// Os POSTs de pedido migraram para o MyOrderController (/api/v1/customers/me/orders):
// a fatia carrega os dois controllers, e o SecurityCheck e mockado porque o /me pergunta
// a ele quem e o autenticado - mesmo padrao do MyCustomerControllerContractTest.
@WebMvcTest(controllers = {OrderController.class, MyOrderController.class})
public class OrderBase {

    public static final UUID AUTHENTICATED_USER_ID =
            UUID.fromString("6e148bd5-47f6-4022-b9da-07cfaa294f7a");

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ForQueryOrders orderQueryService;

    @MockitoBean
    private ForBuyingProduct buyNowApplicationService;

    @MockitoBean
    private ForBuyingWithShoppingCart checkoutApplicationService;

    @MockitoBean
    private SecurityCheckApplicationService securityCheck;

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

        Mockito.when(securityCheck.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER_ID);

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
