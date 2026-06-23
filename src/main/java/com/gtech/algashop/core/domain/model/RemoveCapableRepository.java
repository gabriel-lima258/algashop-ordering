package com.gtech.algashop.core.domain.model;

/**
 * EXTENSÃO OPCIONAL para repositórios que precisam remover Aggregates.
 *
 * --- POR QUE ISSO NÃO ESTÁ NO Repository BASE? ---
 * No DDD, nem todo Aggregate deve ser deletável diretamente.
 * Existir como interface separada é uma decisão de design intencional.
 *
 * Exemplo prático:
 *   - Um "Order" (Pedido) jamais deve ser deletado de verdade — é um dado financeiro.
 *     O correto é cancelar (soft delete / mudança de status).
 *   - Um "Customer" (Cliente) pode ser "arquivado" em vez de deletado fisicamente.
 *   - Um "ShoppingCart" (Carrinho) pode ser deletado ao ser convertido em pedido.
 *
 * Ao separar em interface, o compilador Java nos protege:
 *   - O repositório de Orders NÃO implementa RemoveCapableRepository → não tem delete.
 *   - O repositório de ShoppingCarts implementa → tem delete.
 * Código que recebe um Repository<Order, OrderId> não consegue nem chamar remove().
 *
 * Isso é "segurança por design" — não depende de ninguém lembrar de não chamar o método.
 *
 * @param <T>  O tipo do Aggregate Root removível
 * @param <ID> O tipo do identificador único do Aggregate Root
 */
public interface RemoveCapableRepository<T extends AggregateRoot<ID>, ID> extends Repository<T, ID> {

    /**
     * Remove o Aggregate passado como argumento.
     * Útil quando você já tem a instância carregada em memória.
     */
    void remove(T t);

    /**
     * Remove o Aggregate pelo seu identificador.
     * Útil quando você só tem o id, evitando carregar o objeto só para deletar.
     */
    void remove(ID id);
}
