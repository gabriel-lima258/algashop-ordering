package com.gtech.algashop.infrastructure.adapters.in.web.shoppingcart;

import com.gtech.algashop.core.ports.in.shoppingcart.*;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.core.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.UnprocessableEntityException;
import com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.CanWriteShoppingCart;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.*;

@RestController
@RequestMapping(path = "/api/v1/shopping-carts")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ForQueryShoppingCarts forQueryShoppingCarts;

    private final ForManagingShoppingCarts forManagingShoppingCarts;

    @CanWriteShoppingCart
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCartOutput addItemInShoppingCart(@RequestBody @Valid ShoppingCartInput input) {
        UUID shoppingCartId;
        try {
            shoppingCartId = forManagingShoppingCarts.createNew(input.getCustomerId());
        } catch (CustomerNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }
        return forQueryShoppingCarts.findById(shoppingCartId);
    }

    @CanReadShoppingCart
    @GetMapping("/{shoppingCartId}")
    public ShoppingCartOutput findById(@PathVariable UUID shoppingCartId) {
        return forQueryShoppingCarts.findById(shoppingCartId);
    }

    @CanReadShoppingCart
    @GetMapping("/{shoppingCartId}/items")
    public ShoppingCartItemListModel findItemsFromShoppingById(@PathVariable UUID shoppingCartId) {
        List<ShoppingCartItemOutput> items = forQueryShoppingCarts.findById(shoppingCartId).getItems();
        return new ShoppingCartItemListModel(items);
    }

    @CanWriteShoppingCart
    @DeleteMapping("/{shoppingCartId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShoppingCart(@PathVariable UUID shoppingCartId) {
        forManagingShoppingCarts.delete(shoppingCartId);
    }

    @CanWriteShoppingCart
    @DeleteMapping("/{shoppingCartId}/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void emptyAllItemsFromShoppingCart(@PathVariable UUID shoppingCartId) {
        forManagingShoppingCarts.empty(shoppingCartId);
    }

    @CanWriteShoppingCart
    @PostMapping("/{shoppingCartId}/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addItemInShoppingCart(@PathVariable UUID shoppingCartId, @RequestBody @Valid ShoppingCartItemInput input) {
        input.setShoppingCartId(shoppingCartId);
        try {
            forManagingShoppingCarts.addItem(input);
        } catch (ProductNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }
    }

    @CanWriteShoppingCart
    @DeleteMapping("/{shoppingCartId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void emptyAllItemsFromShoppingCart(@PathVariable UUID shoppingCartId, @PathVariable String itemId) {
        forManagingShoppingCarts.removeItem(shoppingCartId, itemId);
    }

}
