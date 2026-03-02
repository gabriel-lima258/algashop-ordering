package com.gtech.algashop.infrastructure.persistence.customer;

import com.gtech.algashop.application.customer.query.CustomerOutput;
import com.gtech.algashop.application.customer.query.CustomerQueryService;
import com.gtech.algashop.domain.model.costumer.CustomerNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

// CQRS - Implementação do Serviço de Consulta (Query Service)
//
// Esta classe é a ponte entre a camada de aplicação (CustomerQueryService) e
// a infraestrutura de persistência. Ela implementa a interface definida na
// camada de aplicação, mantendo a inversão de dependência (DIP):
//
//   Camada Application:     define CustomerQueryService (interface)
//   Camada Infrastructure:  implementa CustomerQueryServiceImpl (esta classe)
//
// No padrão CQRS, o Query Service:
//   - Não executa nenhuma regra de negócio ou validação de domínio
//   - Apenas delega a consulta para o repositório e trata o caso de "não encontrado"
//   - Retorna diretamente o DTO de leitura (CustomerOutput), nunca a entidade de domínio
//
// Isso contrasta com o Command Side, onde os Application Services orquestram
// chamadas ao domínio, aplicam validações e disparam eventos.
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryServiceImpl implements CustomerQueryService {

    private final EntityManager entityManager;

    // JPQL com projeção direta para o DTO de leitura (Read Model)
    // Nota: os campos devem seguir a mesma ordem do @AllArgsConstructor de CustomerOutput
    private static final String findByIdAsOutputJPQL = """
            SELECT new com.gtech.algashop.application.customer.query.CustomerOutput(
                c.id,
                c.firstName,
                c.lastName,
                c.email,
                c.phone,
                c.document,
                c.birthDate,
                c.promotionNotificationsAllowed,
                c.loyaltyPoints,
                c.registeredAt,
                c.archivedAt,
                c.archived,
                new com.gtech.algashop.application.commons.AddressData(
                    c.address.street,
                    c.address.number,
                    c.address.complement,
                    c.address.neighborhood,
                    c.address.city,
                    c.address.state,
                    c.address.zipCode
                )
            )
            FROM CustomerPersistenceEntity c
            WHERE c.id = :id""";

    @Override
    public CustomerOutput findById(UUID customerId) {
        try {
            TypedQuery<CustomerOutput> query = entityManager.createQuery(findByIdAsOutputJPQL, CustomerOutput.class);
            query.setParameter("id", customerId);
            return query.getSingleResult();
        } catch (NoResultException e) {
            throw new CustomerNotFoundException();
        }
    }
}
