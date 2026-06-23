package com.gtech.algashop.core.ports.out.shoppingcart;

import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartOutput;

import java.util.UUID;

/**
 * Porta de SAÍDA (driven port) — Arquitetura Hexagonal.
 *
 * Representa o "do que a aplicação precisa": define o contrato que a camada
 * de aplicação exige da infraestrutura para conseguir obter dados de
 * carrinhos. Quem realmente sabe COMO buscar (JPA, Mongo, API externa,
 * cache) fica do lado de fora do núcleo.
 *
 * Quem implementa: adaptador secundário ({@code ShoppingCartQueryServiceImpl}
 *                  em {@code infrastructure.persistence}).
 * Quem consome:    a camada de aplicação ({@code ShoppingCartQueryService}),
 *                  que recebe esta interface por injeção e a invoca para
 *                  cumprir o caso de uso definido em {@code ForQueryShoppingCarts}.
 *
 * Por que existir separada da porta de entrada?
 *  - Inversão de dependência: o núcleo depende de uma abstração
 *    (esta interface), não de Spring Data/JPA. Trocar Postgres por outro
 *    mecanismo não afeta o caso de uso.
 *  - Testabilidade: em testes da aplicação, mockamos esta porta sem
 *    precisar subir banco.
 *  - Direção das setas: portas IN são chamadas DE FORA pra DENTRO;
 *    portas OUT são chamadas DE DENTRO pra FORA. Manter as duas
 *    explícitas deixa o fluxo da requisição evidente no código.
 */
public interface ForObtainingShoppingCarts {
    ShoppingCartOutput findById(UUID shoppingCartId);
    ShoppingCartOutput findByCustomerId(UUID customerId);
}
