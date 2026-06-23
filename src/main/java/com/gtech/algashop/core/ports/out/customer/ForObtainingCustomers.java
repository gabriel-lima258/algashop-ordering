package com.gtech.algashop.core.ports.out.customer;

import com.gtech.algashop.core.ports.in.customer.CustomerFilter;
import com.gtech.algashop.core.ports.in.customer.CustomerOutput;
import com.gtech.algashop.core.ports.in.customer.CustomerSummaryOutput;
import org.springframework.data.domain.Page;

import java.util.UUID;

// CQRS - Query Side (Read Model)
//
// Esta interface representa o lado de LEITURA (Query) do padrão CQRS.
// No CQRS, separamos as operações de leitura das operações de escrita em modelos distintos:
//
//   - Query (Read):  retorna dados otimizados para exibição (este serviço)
//   - Command (Write): executa mutações no domínio (ex: CustomerRegistrationService)
//
// Vantagens desta separação:
//   1. O modelo de leitura (CustomerOutput) é um DTO plano, sem regras de negócio,
//      otimizado para a necessidade da camada de apresentação.
//   2. Consultas podem ser otimizadas independentemente (JPQL direto, projeções, cache)
//      sem impactar a complexidade do modelo de domínio.
//   3. A interface abstrai a origem dos dados — a implementação pode buscar do banco,
//      de um cache, ou até de um banco de leitura separado no futuro.
public interface ForObtainingCustomers {
    CustomerOutput findById(UUID customerId);
    Page<CustomerSummaryOutput> filter(CustomerFilter filter);
}
