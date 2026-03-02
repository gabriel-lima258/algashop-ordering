package com.gtech.algashop.infrastructure.persistence.shoppingcart;

import com.gtech.algashop.application.order.query.OrderDetailOutput;
import com.gtech.algashop.application.shoppingcart.query.ShoppingCartOutput;
import com.gtech.algashop.application.shoppingcart.query.ShoppingCartQueryService;
import com.gtech.algashop.application.util.Mapper;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.OrderNotFoundException;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartNotFound;
import com.gtech.algashop.infrastructure.persistence.order.OrderPersistenceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartQueryServiceImpl implements ShoppingCartQueryService {

    private final ShoppingCartJpaEntityRepository repository;
    private final Mapper mapper;

    @Override
    public ShoppingCartOutput findById(UUID shoppingCartId) {
        ShoppingCartPersistenceEntity entity = repository.findById(shoppingCartId)
                .orElseThrow(ShoppingCartNotFound::new);
        return mapper.convert(entity, ShoppingCartOutput.class);
    }

    @Override
    public ShoppingCartOutput findByCustomerId(UUID customerId) {
        ShoppingCartPersistenceEntity entity = repository.findByCustomer_Id(customerId)
                .orElseThrow(ShoppingCartNotFound::new);
        return mapper.convert(entity, ShoppingCartOutput.class);
    }
}
