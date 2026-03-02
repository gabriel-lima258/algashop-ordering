package com.gtech.algashop.infrastructure.persistence.order;

import com.gtech.algashop.application.order.query.CustomerMinimalOutput;
import com.gtech.algashop.application.order.query.OrderDetailOutput;
import com.gtech.algashop.application.order.query.OrderQueryService;
import com.gtech.algashop.application.order.query.OrderSummaryOutput;
import com.gtech.algashop.application.util.Mapper;
import com.gtech.algashop.application.util.PageFilter;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.OrderNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryServiceImpl implements OrderQueryService {

    private final OrderJpaEntityRepository repository;
    private final Mapper mapper;

    private final EntityManager entityManager;

    @Override
    public OrderDetailOutput findById(String orderId) {
        OrderPersistenceEntity entity = repository.findById(new OrderId(orderId).value().toLong())
                .orElseThrow(OrderNotFoundException::new);
        return mapper.convert(entity, OrderDetailOutput.class);
    }

    @Override
    public Page<OrderSummaryOutput> filter(PageFilter filter) {
        // construindo uma paginaçao usando criteria api
        Long totalQueryResults = countTotalQueryResults(filter);

        if (totalQueryResults.equals(0L)) {
            // se o total for vazio retorne uma pagina vazia
            PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize());
            return new PageImpl<>(new ArrayList<>(), pageRequest, totalQueryResults);
        }

        return filterQuery(filter, totalQueryResults);
    }

    private Page<OrderSummaryOutput> filterQuery(PageFilter filter, Long totalQueryResults) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderSummaryOutput> criteriaQuery = builder.createQuery(OrderSummaryOutput.class);
        Root<OrderPersistenceEntity> root = criteriaQuery.from(OrderPersistenceEntity.class);

        Path<Object> customer = root.get("customer");

        criteriaQuery.select(
                builder.construct(OrderSummaryOutput.class,
                        root.get("id"),
                        builder.construct(CustomerMinimalOutput.class,
                                customer.get("id"),
                                customer.get("firstName"),
                                customer.get("lastName"),
                                customer.get("email"),
                                customer.get("document"),
                                customer.get("phone")
                        ),
                        root.get("totalItems"),
                        root.get("totalAmount"),
                        root.get("placedAt"),
                        root.get("paidAt"),
                        root.get("readyAt"),
                        root.get("canceledAt"),
                        root.get("status"),
                        root.get("paymentMethod")
                )
        );

        TypedQuery<OrderSummaryOutput> typedQuery = entityManager.createQuery(criteriaQuery);

        typedQuery.setFirstResult(filter.getSize() * filter.getPage());
        typedQuery.setMaxResults(filter.getSize());

        PageRequest pageRequest = PageRequest.of(filter.getPage(), filter.getSize());

        return new PageImpl<>(typedQuery.getResultList(), pageRequest, totalQueryResults);
    }

    private Long countTotalQueryResults(PageFilter filter) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = builder.createQuery(Long.class);
        Root<OrderPersistenceEntity> root = criteriaQuery.from(OrderPersistenceEntity.class);

        Expression<Long> count = builder.count(root);
        criteriaQuery.select(count);

        TypedQuery<Long> query = entityManager.createQuery(criteriaQuery);

        return query.getSingleResult();
    }
}
