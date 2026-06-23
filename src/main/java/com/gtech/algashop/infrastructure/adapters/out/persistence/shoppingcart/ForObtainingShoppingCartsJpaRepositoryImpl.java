package com.gtech.algashop.infrastructure.adapters.out.persistence.shoppingcart;

import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartOutput;
import com.gtech.algashop.core.application.util.Mapper;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartNotFound;
import com.gtech.algashop.core.ports.out.shoppingcart.ForObtainingShoppingCarts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForObtainingShoppingCartsJpaRepositoryImpl implements ForObtainingShoppingCarts {

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
