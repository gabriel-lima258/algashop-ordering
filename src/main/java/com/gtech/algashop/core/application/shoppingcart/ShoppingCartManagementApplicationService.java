package com.gtech.algashop.core.application.shoppingcart;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.commons.Quantity;
import com.gtech.algashop.core.domain.model.costumer.CustomerId;
import com.gtech.algashop.core.domain.model.product.Product;
import com.gtech.algashop.core.domain.model.product.ProductCatalogService;
import com.gtech.algashop.core.domain.model.product.ProductId;
import com.gtech.algashop.core.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.core.domain.model.shoppingcart.*;
import com.gtech.algashop.core.ports.in.shoppingcart.ForManagingShoppingCarts;
import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartItemInput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShoppingCartManagementApplicationService implements ForManagingShoppingCarts {

    private final ProductCatalogService productCatalogService;
    private final ShoppingService shoppingService;

    // repositorio
    private final ShoppingCarts shoppingCarts;

    @Transactional
    @Override
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
    @Override
    public UUID createNew(UUID customerId) {
        Objects.requireNonNull(customerId);
        ShoppingCart shoppingCart = shoppingService.startShopping(new CustomerId(customerId));
        shoppingCarts.add(shoppingCart);
        return shoppingCart.id().value();
    }

    @Transactional
    @Override
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
    @Override
    public void empty(UUID shoppingCartId) {
        Objects.requireNonNull(shoppingCartId);

        ShoppingCartId shoppingId = new ShoppingCartId(shoppingCartId);
        ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingId)
                .orElseThrow(ShoppingCartNotFound::new);

        shoppingCart.empty();
        shoppingCarts.add(shoppingCart);
    }

    @Transactional
    @Override
    public void delete(UUID shoppingCartId) {
        Objects.requireNonNull(shoppingCartId);

        ShoppingCartId shoppingId = new ShoppingCartId(shoppingCartId);
        ShoppingCart shoppingCart = shoppingCarts.ofId(shoppingId)
                .orElseThrow(ShoppingCartNotFound::new);

        shoppingCarts.remove(shoppingCart);
    }

    // handlers de evento tocam N carrinhos: a transacao unica garante tudo-ou-nada.
    // Sem ela cada add() abria a propria transacao e uma falha no meio deixava metade
    // dos carrinhos atualizada - sem rollback de nada
    @Override
    @Transactional
    public void changeProductAvailability(UUID productId, boolean available) {
        List<ShoppingCart> affectedShoppingCarts = shoppingCarts.findAllContainingItem(new ProductId(productId));

        if (affectedShoppingCarts.isEmpty()) {
            return;
        }

        affectedShoppingCarts.forEach(shoppingCart -> {
            shoppingCart.changeItemAvailability(new ProductId(productId), available);
            shoppingCarts.add(shoppingCart);
        });
    }

    // caminho via AGREGADO (carrega, muda, salva): as invariantes ficam no dominio, ao
    // custo de N+1 para produto popular. O caminho bulk ja existe pronto e testado
    // (ShoppingCartUpdateProvider: 2 UPDATEs em uma transacao) - e a otimizacao obvia
    // quando este doer; o trade-off esta documentado em ecst-e-validacao-de-eventos.md
    @Override
    @Transactional
    public void refreshProductPrice(UUID productId, BigDecimal salePrice) {
        ProductId domainProductId = new ProductId(productId);
        List<ShoppingCart> affectedShoppingCarts = shoppingCarts.findAllContainingItem(domainProductId);

        if (affectedShoppingCarts.isEmpty()) {
            return;
        }

        affectedShoppingCarts.forEach(shoppingCart -> {
            shoppingCart.changeItemPrice(domainProductId, new Money(salePrice));
            shoppingCarts.add(shoppingCart);
        });
    }

}
