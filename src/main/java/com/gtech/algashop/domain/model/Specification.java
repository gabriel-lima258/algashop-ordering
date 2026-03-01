package com.gtech.algashop.domain.model;

/**
 * Padrão Specification (DDD) — encapsula regras de negócio como objetos reutilizáveis e combináveis.
 *
 * Cada implementação representa uma regra isolada (ex: CustomerHaveFreeShippingSpecification).
 * Os métodos default permitem compor regras complexas a partir de regras simples, sem criar novas classes.
 *
 * Exemplo de composição:
 *   Specification<Customer> eligible = freteGratis.and(clienteAtivo);
 *   Specification<Customer> special  = freteGratis.or(clienteVip);
 *   Specification<Customer> filtered = clienteAtivo.andNot(clienteBloqueado);
 *   Specification<Customer> inverted = clienteAtivo.not(clienteAtivo); // nega a própria regra
 */
public interface Specification<T> {

    // Método principal: avalia se o objeto T satisfaz a regra de negócio.
    // Cada implementação concreta define sua própria lógica (ex: verificar pontos de fidelidade).
    boolean isSatisfiedBy(T t);

    // Combina duas regras com E lógico. Ambas devem ser verdadeiras.
    // Uso: clienteAtivo.and(clienteComCredito) → só aprova se ativo E com crédito.
    default Specification<T> and(Specification<T> other) {
        return t -> this.isSatisfiedBy(t) && other.isSatisfiedBy(t);
    }

    // Combina duas regras com OU lógico. Pelo menos uma deve ser verdadeira.
    // Uso: freteGratis.or(clienteVip) → aprova se tem frete grátis OU é VIP.
    default Specification<T> or(Specification<T> other) {
        return t -> this.isSatisfiedBy(t) || other.isSatisfiedBy(t);
    }

    // Nega a regra atual (ignora o parâmetro other — nega a si mesma).
    // Uso: clienteAtivo.not(null) → retorna true quando o cliente NÃO é ativo.
    // Obs: o parâmetro 'other' não é utilizado; a negação é aplicada sobre 'this'.
    default Specification<T> not(Specification<T> other) {
        return t -> !this.isSatisfiedBy(t);
    }

    // Combina a regra atual com a negação de outra. this E NÃO other.
    // Uso: clienteAtivo.andNot(clienteBloqueado) → ativo E não bloqueado.
    default Specification<T> andNot(Specification<T> other) {
        return t -> this.isSatisfiedBy(t) && !other.isSatisfiedBy(t);
    }
}
