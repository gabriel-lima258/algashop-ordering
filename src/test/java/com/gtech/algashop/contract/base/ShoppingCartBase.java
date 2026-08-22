package com.gtech.algashop.contract.base;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.application.shoppingcart.query.ShoppingCartOutputTestDataBuilder;
import com.gtech.algashop.core.ports.in.shoppingcart.ForManagingShoppingCarts;
import com.gtech.algashop.core.ports.in.shoppingcart.ForQueryShoppingCarts;
import com.gtech.algashop.infrastructure.adapters.in.web.shoppingcart.MyShoppingCartController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// O recurso virou /customers/me/shopping-cart: nenhum id no path. O carrinho e resolvido
// pelo SecurityCheck (mockado com sub fixo) + findByCustomerId - mesmo padrao do OrderBase.
@WebMvcTest(controllers = MyShoppingCartController.class)
public class ShoppingCartBase {

    public static final UUID AUTHENTICATED_USER_ID =
            UUID.fromString("6e148bd5-47f6-4022-b9da-07cfaa294f7a");

    public static final UUID validShoppingCartId = UUID.fromString("ad265aa3-c77d-46e9-9782-b70c487c1e17");

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ForManagingShoppingCarts managementService;

    @MockitoBean
    private ForQueryShoppingCarts queryService;

    @MockitoBean
    private SecurityCheckApplicationService securityCheck;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(
                MockMvcBuilders.webAppContextSetup(context)
                        .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                        .build()
        );

        RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();

        Mockito.when(securityCheck.getAuthenticatedUserId()).thenReturn(AUTHENTICATED_USER_ID);

        Mockito.when(queryService.findByCustomerId(AUTHENTICATED_USER_ID))
                .thenReturn(ShoppingCartOutputTestDataBuilder.aShoppingCart()
                        .id(validShoppingCartId)
                        .customerId(AUTHENTICATED_USER_ID)
                        .build());

        Mockito.when(managementService.createNew(AUTHENTICATED_USER_ID))
                .thenReturn(validShoppingCartId);
    }
}
