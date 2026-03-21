package com.gtech.algashop.domain.model.shoppingcart;

import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.costumer.CustomerAlreadyHaveShoppingCartException;
import com.gtech.algashop.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.DomainService;
import lombok.RequiredArgsConstructor;

@DomainService
@RequiredArgsConstructor
public class ShoppingService {

    private final Customers customers;
    private final ShoppingCarts shoppingCarts;

    public ShoppingCart startShopping(CustomerId customerId) {
        if (!customers.exists(customerId)) {
            throw new CustomerNotFoundException();
        }

        if (shoppingCarts.ofCustomer(customerId).isPresent()) {
            throw new CustomerAlreadyHaveShoppingCartException(customerId);
        }

        return ShoppingCart.startShopping(customerId);
    }
}
