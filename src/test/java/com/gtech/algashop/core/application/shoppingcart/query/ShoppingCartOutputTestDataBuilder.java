package com.gtech.algashop.core.application.shoppingcart.query;

import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartItemOutput;
import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartOutput;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ShoppingCartOutputTestDataBuilder {

    public static ShoppingCartOutput.ShoppingCartOutputBuilder aShoppingCart() {
        return ShoppingCartOutput.builder()
                .id(UUID.fromString("ad265aa3-c77d-46e9-9782-b70c487c1e17"))
                .customerId(UUID.fromString("f5ab7a1e-37da-41e1-892b-a1d38275c2f2"))
                .totalItems(3)
                .totalAmount(new BigDecimal("1250.00"))
                .items(List.of(
                        ShoppingCartItemOutput.builder()
                                .id(UUID.randomUUID().toString())
                                .productId(UUID.randomUUID())
                                .name("Notebook")
                                .price(new BigDecimal("500.00"))
                                .quantity(2)
                                .totalAmount(new BigDecimal("1000.00"))
                                .available(true)
                                .build(),
                        ShoppingCartItemOutput.builder()
                                .id(UUID.randomUUID().toString())
                                .productId(UUID.randomUUID())
                                .name("Mouse pad")
                                .price(new BigDecimal("250.00"))
                                .quantity(1)
                                .totalAmount(new BigDecimal("250.00"))
                                .available(true)
                                .build()
                ));
    }
}
