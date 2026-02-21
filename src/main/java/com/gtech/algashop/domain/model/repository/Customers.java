package com.gtech.algashop.domain.model.repository;

import com.gtech.algashop.domain.model.entity.Customer;
import com.gtech.algashop.domain.model.entity.VO.id.CustomerId;

/**
 * REPOSITÓRIO DE DOMÍNIO para o Aggregate Root Customer.
 *
 * --- MESMO PADRÃO QUE Orders ---
 * Segue a mesma filosofia: a camada de domínio define o CONTRATO (o que fazer),
 * e a infraestrutura define a IMPLEMENTAÇÃO (como fazer).
 *
 * --- POR QUE NÃO ESTENDE RemoveCapableRepository? ---
 * Clientes também não são deletados fisicamente em sistemas sérios.
 * A prática correta é "arquivar" (soft delete com flag archived=true),
 * que é um comportamento de negócio implementado no próprio Aggregate Root Customer.
 * Portanto, a remoção física não é exposta via repositório.
 *
 * --- ISOLAMENTO DO DOMÍNIO ---
 * Esta interface importa apenas classes do próprio domínio (Customer, CustomerId).
 * Não há nenhuma dependência de Spring, JPA, ou qualquer framework externo.
 * Isso significa que o domínio pode ser compilado e testado de forma completamente independente.
 */
public interface Customers extends Repository<Customer, CustomerId> {
}
