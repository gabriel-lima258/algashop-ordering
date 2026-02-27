package com.gtech.algashop.application.shoppingcart.management;

import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import com.gtech.algashop.domain.model.product.ProductId;
import com.gtech.algashop.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.domain.model.shoppingcart.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartManagementApplicationService {

    private final ProductCatalogService productCatalogService;
    private final ShoppingService shoppingService;

    // repositorio
    private final ShoppingCarts shoppingCarts;

    @Transactional
    public void addItem(ShoppingCartItemInput input) {
        Objects.requireNonNull(input);

        ShoppingCartId shoppingCartId = new ShoppingCartId(input.getShoppingCartId());
        ProductId productId = new ProductId(input.getProductId());

        ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingCartId)
                .orElseThrow(ShoppingCartNotFound::new);
        Product product = productCatalogService.ofId(productId)
                .orElseThrow(ProductNotFoundException::new);

        shoppingCart.addItem(product, new Quantity(input.getQuantity()));

        shoppingCarts.add(shoppingCart);
    }

    @Transactional
    public UUID createNew(UUID customerId) {
        Objects.requireNonNull(customerId);
        ShoppingCart shoppingCart = shoppingService.startShopping(new CustomerId(customerId));
        shoppingCarts.add(shoppingCart);
        return shoppingCart.id().value();
    }

    @Transactional
    public void removeItem(UUID shoppingCartId, String shoppingCartItemId) {
        Objects.requireNonNull(shoppingCartId);
        Objects.requireNonNull(shoppingCartItemId);

        ShoppingCartId shoppingId = new ShoppingCartId(shoppingCartId);
        ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingId)
                .orElseThrow(ShoppingCartNotFound::new);

        shoppingCart.removeItem(new ShoppingCartItemId(shoppingCartItemId));
        shoppingCarts.add(shoppingCart);
    }

    @Transactional
    public void empty(UUID shoppingCartId) {
        Objects.requireNonNull(shoppingCartId);

        ShoppingCartId shoppingId = new ShoppingCartId(shoppingCartId);
        ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingId)
                .orElseThrow(ShoppingCartNotFound::new);

        shoppingCart.empty();
        shoppingCarts.add(shoppingCart);
    }

    @Transactional
    public void delete(UUID shoppingCartId) {
        Objects.requireNonNull(shoppingCartId);

        ShoppingCartId shoppingId = new ShoppingCartId(shoppingCartId);
        ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingId)
                .orElseThrow(ShoppingCartNotFound::new);

        shoppingCarts.remove(shoppingCart);
    }

}
