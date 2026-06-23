package com.gtech.algashop.core.application.shoppingcart;

import com.gtech.algashop.core.ports.in.shoppingcart.ForQueryShoppingCarts;
import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartOutput;
import com.gtech.algashop.core.ports.out.shoppingcart.ForObtainingShoppingCarts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartQueryService implements ForQueryShoppingCarts {

    private final ForObtainingShoppingCarts forObtainingShoppingCarts;

    @Override
    public ShoppingCartOutput findById(UUID shoppingCartId) {
        return forObtainingShoppingCarts.findById(shoppingCartId);
    }

    @Override
    public ShoppingCartOutput findByCustomerId(UUID customerId) {
        return forObtainingShoppingCarts.findByCustomerId(customerId);
    }
}
