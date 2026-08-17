package com.gtech.algashop.infrastructure.config.security.check;


import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Meta-anotacoes de escopo. Cada uma embrulha um @PreAuthorize para que o controller
// declare a INTENCAO ("quem le pedido") em vez da expressao ("hasAuthority('SCOPE_...')").
//
// Tres razoes para isso nao ser so acucar sintatico:
//
// 1. A expressao do @PreAuthorize e uma STRING avaliada em runtime. Um typo -
//    'SCOPE_orders' sem o sufixo, ou 'SCOPE_orders:raed' - compila, sobe, e NEGA
//    todo mundo em silencio. Concentrar as strings aqui reduz a superficie de erro
//    de N controllers para um arquivo, e e o que a matriz de teste consegue cobrir.
// 2. Renomear um escopo vira uma edicao, nao uma varredura.
// 3. Quem le o controller ve a regra de negocio, nao a sintaxe do Spring Security.
//
// O prefixo SCOPE_ nao e escolha nossa: o JwtGrantedAuthoritiesConverter, padrao do
// resource server, le o claim "scope" do token e prefixa cada valor com "SCOPE_" ao
// transformar em GrantedAuthority. Por isso hasAuthority('SCOPE_x') e nao hasScope('x').
public class SecurityAnnotations {

    // ORDERS
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('SCOPE_orders:read')")
    public @interface CanReadOrders {}

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('SCOPE_orders:write')")
    public @interface CanWriteOrders {}

    // CUSTOMERS
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('SCOPE_customers:read')")
    public @interface CanReadCustomers {}

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('SCOPE_customers:write')")
    public @interface CanWriteCustomers {}

    // SHOPPING-CARTS
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('SCOPE_shopping-carts:read')")
    public @interface CanReadShoppingCart {}

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('SCOPE_shopping-carts:write')")
    public @interface CanWriteShoppingCart {}

    // SHIPPING-COSTS
    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("hasAuthority('SCOPE_shipping-costs:preview')")
    public @interface CanPreviewShippingCosts {}

}
