package com.gtech.algashop.core.ports.in.shoppingcart;

import java.util.UUID;

/**
 * Porta de ENTRADA (driving port) — Arquitetura Hexagonal.
 *
 * Representa o "para que serve" a aplicação: define o caso de uso de
 * consulta de carrinhos que o mundo externo (Controllers REST, jobs,
 * mensageria, testes) pode invocar.
 *
 * Quem implementa: a camada de aplicação ({@code ShoppingCartQueryService}).
 * Quem consome:    adaptadores primários ({@code ShoppingCartController},
 *                  {@code CustomerController}).
 *
 * Por que separar de {@code ForObtainingShoppingCarts}?
 *  - Esta interface descreve a INTENÇÃO do caso de uso (o que a aplicação
 *    oferece). A outra descreve a DEPENDÊNCIA de infraestrutura (o que a
 *    aplicação precisa para funcionar — ex.: ler do banco).
 *  - Embora os métodos hoje sejam idênticos, eles podem divergir: o caso de
 *    uso pode aplicar autorização, cache, eventos, orquestrar várias fontes,
 *    enquanto a porta de saída permanece focada apenas em obter dados.
 *  - Garante a inversão de dependência: o núcleo não conhece JPA/Postgres;
 *    a infraestrutura é que se pluga no contrato definido aqui.
 */
public interface ForQueryShoppingCarts {
    ShoppingCartOutput findById(UUID shoppingCartId);
    ShoppingCartOutput findByCustomerId(UUID customerId);
}
