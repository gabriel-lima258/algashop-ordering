package com.gtech.algashop.infrastructure.adapters.out.persistence.customer;

import com.gtech.algashop.core.ports.in.customer.CustomerFilter;
import com.gtech.algashop.core.ports.in.customer.CustomerOutput;
import com.gtech.algashop.core.ports.in.customer.ForQueryCustomers;
import com.gtech.algashop.core.ports.in.customer.CustomerSummaryOutput;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.core.ports.out.customer.ForObtainingCustomers;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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
public class ForObtainingCustomerJpaRepositoryImpl implements ForObtainingCustomers {

    private final EntityManager entityManager;

    // JPQL com projeção direta para o DTO de leitura (Read Model)
    // Nota: os campos devem seguir a mesma ordem do @AllArgsConstructor de CustomerOutput
    private static final String findByIdAsOutputJPQL = """
            SELECT new com.gtech.algashop.core.ports.in.customer.CustomerOutput(
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
                new com.gtech.algashop.core.ports.in.commons.AddressData(
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

    @Override
    public Page<CustomerSummaryOutput> filter(CustomerFilter filter) {
        // construindo uma paginaçao usando criteria api
        Long totalQueryResults = countTotalQueryResults(filter);

        if (totalQueryResults.equals(0L)) {
            // se o total for vazio retorne uma pagina vazia
            PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize());
            return new PageImpl<>(new ArrayList<>(), pageRequest, totalQueryResults);
        }

        return filterQuery(filter, totalQueryResults);
    }

    private Page<CustomerSummaryOutput> filterQuery(CustomerFilter filter, Long totalQueryResults) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CustomerSummaryOutput> criteriaQuery = builder.createQuery(CustomerSummaryOutput.class);
        Root<CustomerPersistenceEntity> root = criteriaQuery.from(CustomerPersistenceEntity.class);

        criteriaQuery.select(
                builder.construct(CustomerSummaryOutput.class,
                        root.get("id"),
                        root.get("firstName"),
                        root.get("lastName"),
                        root.get("email"),
                        root.get("document"),
                        root.get("phone"),
                        root.get("birthDate"),
                        root.get("loyaltyPoints"),
                        root.get("registeredAt"),
                        root.get("archivedAt"),
                        root.get("promotionNotificationsAllowed"),
                        root.get("archived")
                )
        );

        Predicate[] predicates = toPredicates(builder, root, filter);
        Order sortOrder = toSortOrder(builder, root, filter);

        criteriaQuery.where(predicates);
        if (sortOrder != null) {
            criteriaQuery.orderBy(sortOrder);
        }

        TypedQuery<CustomerSummaryOutput> typedQuery = entityManager.createQuery(criteriaQuery);

        typedQuery.setFirstResult(filter.getSize() * filter.getPage());
        typedQuery.setMaxResults(filter.getSize());

        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize());

        return new PageImpl<>(typedQuery.getResultList(), pageRequest, totalQueryResults);
    }

    private Long countTotalQueryResults(CustomerFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        Root<CustomerPersistenceEntity> root = criteriaQuery.from(CustomerPersistenceEntity.class);

        Expression<Long> count = builder.count(root);
        Predicate[] predicates = toPredicates(builder, root, filter);

        criteriaQuery.select(count);
        criteriaQuery.where(predicates);

        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

        return query.getSingleResult();
    }

    private Order toSortOrder(CriteriaBuilder builder, Root<CustomerPersistenceEntity> root, CustomerFilter filter) {
        if (filter.getSortDirectionOrDefault() == Sort.Direction.ASC) {
            return builder.asc(root.get(filter.getSortByPropertyOrDefault().getPropertyName()));
        }

        if (filter.getSortDirectionOrDefault() == Sort.Direction.DESC) {
            return builder.desc(root.get(filter.getSortByPropertyOrDefault().getPropertyName()));
        }

        return null;
    }

    // predicates são os filtros aplicados a consulta, busca e pesquisa em query
    private Predicate[] toPredicates(CriteriaBuilder builder, Root<CustomerPersistenceEntity> root, CustomerFilter filter) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getEmail() != null && !filter.getEmail().isBlank()) {
            predicates.add(builder.like(builder.lower(root.get("email")), "%" + filter.getEmail().toLowerCase() + "%"));
        }

        if (filter.getFirstName() != null && !filter.getFirstName().isBlank()) {
            predicates.add(builder.like(builder.lower(root.get("firstName")), "%" + filter.getFirstName().toLowerCase() + "%"));
        }

        return predicates.toArray(new Predicate[]{});
    }
}
