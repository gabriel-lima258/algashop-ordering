package com.gtech.algashop.core.domain.model;

import java.util.Optional;

/**
 * CONTRATO BASE para todos os repositórios do domínio.
 *
 * --- POR QUE ISSO EXISTE? ---
 * No DDD (Domain-Driven Design), um repositório NÃO é uma abstração de banco de dados.
 * Ele é uma abstração de uma COLEÇÃO EM MEMÓRIA de Aggregates.
 * Pense nele como uma List<Order> ou Set<Customer>, mas persistida.
 *
 * Errado (pensamento de banco):  "select * from orders where id = ?"
 * Certo  (pensamento DDD):       "orders.ofId(orderId)"
 *
 * --- POR QUE É UMA INTERFACE? ---
 * Porque esta interface fica na camada de DOMÍNIO, que não sabe — e não deve saber —
 * se os dados estão num banco PostgreSQL, MongoDB, em memória, ou em qualquer outro lugar.
 * Quem implementa é a camada de infraestrutura. Isso é o padrão "Ports & Adapters" (Hexagonal).
 *
 * --- POR QUE O GENERIC <T extends AggregateRoot<ID>, ID>? ---
 * Isso reforça uma regra do DDD: apenas Aggregate Roots podem ter repositórios próprios.
 * Entidades internas (ex: OrderItem) são acessadas sempre através do seu Aggregate Root (Order).
 * O compilador Java rejeita qualquer tentativa de criar um repositório para uma entidade comum.
 *
 * @param <T>  O tipo do Aggregate Root que este repositório gerencia
 * @param <ID> O tipo do identificador único do Aggregate Root
 */
public interface Repository<T extends AggregateRoot<ID>, ID> {

    /**
     * Busca um Aggregate pelo seu identificador.
     *
     * Retorna Optional para forçar quem usa a tratar explicitamente o caso
     * em que o objeto não existe, evitando NullPointerException silenciosos.
     * É o equivalente semântico a: "me dê o pedido com este id, se existir".
     */
    Optional<T> ofId(ID id);

    /**
     * Verifica se um Aggregate com esse id já existe na coleção.
     *
     * Útil para checar duplicatas antes de adicionar, sem precisar
     * carregar todos os objetos em memória (mais eficiente que ofId + isPresent).
     */
    boolean exists(ID id);

    /**
     * Adiciona um novo Aggregate à coleção (persiste).
     *
     * O nome "add" é intencional: reforça a metáfora de coleção.
     * Não usamos "save" ou "insert" para não vazar conceitos de banco de dados
     * para dentro do domínio.
     */
    void add(T aggregateRoot);

    /**
     * Retorna a quantidade total de Aggregates na coleção.
     */
    long count();
}
