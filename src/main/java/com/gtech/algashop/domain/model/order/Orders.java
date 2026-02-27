package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.commons.Money;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.Repository;

import java.time.Year;
import java.util.List;

/**
 * REPOSITÓRIO DE DOMÍNIO para o Aggregate Root Order.
 *
 * --- POR QUE TÃO SIMPLES? ---
 * Esta interface é propositalmente minimalista. Ela apenas declara
 * "quero uma coleção de Orders, identificadas por OrderId".
 * Toda a complexidade de persistência fica na camada de infraestrutura.
 *
 * --- POR QUE NÃO ESTENDE RemoveCapableRepository? ---
 * Pedidos são registros imutáveis de negócio (rastreabilidade financeira/fiscal).
 * Eles NUNCA devem ser deletados fisicamente — devem ser cancelados (mudança de status).
 * Ao não estender RemoveCapableRepository, o compilador garante que ninguém
 * consegue deletar um pedido acidentalmente através desta interface.
 *
 * --- ONDE ESTÁ A IMPLEMENTAÇÃO? ---
 * Na camada de infraestrutura: OrderPersistenceProvider.
 * Esta interface não sabe nada sobre JPA, SQL, banco de dados, etc.
 * Isso mantém o domínio puro e testável de forma isolada.
 */
public interface Orders extends Repository<Order, OrderId> {
    List<Order> placedByCustomerInYear(CustomerId customerId, Year year);
    long salesQuantityByCustomerInYear(CustomerId customerId, Year year);
    Money totalSoldForCustomer(CustomerId customerId);
}
