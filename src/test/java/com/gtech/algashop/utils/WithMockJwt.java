package com.gtech.algashop.utils;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// criamos uma annotation para injetar um token jwt valido nas classes de testes
// podemos customizar em testes isolados seus construtores
// @WithMockJwt(role = "MANAGER")

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockJwtSecurityContextFactory.class)
public @interface WithMockJwt {

    String subject() default "6e148bd5-47f6-4022-b9da-07cfaa294f7a";
    String[] scopes() default {
        "orders:read",
        "orders:write",
        "customers:read",
        "customers:write",
        "shopping-carts:read",
        "shopping-carts:write",
        "shipping-costs:preview"
    };
    String role() default "CUSTOMER";
    String[] audiences() default {
        "ecommerce-web-app"
    };
}
