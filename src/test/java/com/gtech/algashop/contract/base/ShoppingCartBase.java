package com.gtech.algashop.contract.base;

import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartItemInput;
import com.gtech.algashop.core.application.shoppingcart.ShoppingCartManagementApplicationService;
import com.gtech.algashop.core.application.shoppingcart.query.ShoppingCartOutputTestDataBuilder;
import com.gtech.algashop.core.ports.in.shoppingcart.ForQueryShoppingCarts;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartNotFound;
import com.gtech.algashop.infrastructure.adapters.in.web.shoppingcart.ShoppingCartController;
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

@WebMvcTest(controllers = ShoppingCartController.class)
public class ShoppingCartBase {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private ShoppingCartManagementApplicationService managementService;

    @MockitoBean
    private ForQueryShoppingCarts queryService;

    public static final UUID validShoppingCartId = UUID.fromString("ad265aa3-c77d-46e9-9782-b70c487c1e17");

    public static final UUID notFoundShoppingCartId = UUID.fromString("e2103964-5353-4910-81ee-212a40a2ca70");

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(
                MockMvcBuilders.webAppContextSetup(context)
                        .defaultResponseCharacterEncoding(StandardCharsets.UTF_8)
                        .build()
        );

        RestAssuredMockMvc.enableLoggingOfRequestAndResponseIfValidationFails();

        Mockito.when(queryService.findById(validShoppingCartId))
                .thenReturn(ShoppingCartOutputTestDataBuilder.aShoppingCart().id(validShoppingCartId).build());

        Mockito.when(queryService.findById(notFoundShoppingCartId))
                .thenThrow(new ShoppingCartNotFound());

        Mockito.when(managementService.createNew(Mockito.any(UUID.class)))
                .thenReturn(validShoppingCartId);

        Mockito.doNothing().when(managementService).addItem(Mockito.any(ShoppingCartItemInput.class));

        Mockito.doNothing().when(managementService).delete(validShoppingCartId);

        Mockito.doNothing().when(managementService).empty(validShoppingCartId);

        Mockito.doNothing().when(managementService).removeItem(Mockito.eq(validShoppingCartId), Mockito.anyString());

    }
}
