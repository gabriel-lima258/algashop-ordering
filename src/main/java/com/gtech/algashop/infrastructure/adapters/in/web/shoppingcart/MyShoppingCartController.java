package com.gtech.algashop.infrastructure.adapters.in.web.shoppingcart;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.core.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.core.ports.in.shoppingcart.*;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.UnprocessableEntityException;
import com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.CanReadMyShoppingCart;
import com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.CanWriteMyShoppingCart;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping(path = "/api/v1/customers/me/shopping-cart")
@RequiredArgsConstructor
public class MyShoppingCartController {

    private final ForQueryShoppingCarts forQueryShoppingCarts;
    private final ForManagingShoppingCarts forManagingShoppingCarts;

    private final SecurityCheckApplicationService securityCheck;

    @CanReadMyShoppingCart
    @GetMapping
    public ShoppingCartOutput get() {
        return findAuthenticateCustomerShoppingCart();
    }

    @CanWriteMyShoppingCart
    @PostMapping
    public ResponseEntity<ShoppingCartOutput> createMyShoppingCart() {
        try {
            forManagingShoppingCarts.createNew(securityCheck.getAuthenticatedUserId());
        } catch (CustomerNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }
        return ResponseEntity.created(URI.create("/api/v1/customers/me/shopping-cart"))
                .body(findAuthenticateCustomerShoppingCart());
    }

    @CanWriteMyShoppingCart
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShoppingCart() {
        ShoppingCartOutput shoppingCart = findAuthenticateCustomerShoppingCart();
        forManagingShoppingCarts.delete(shoppingCart.getId());
    }

    @CanReadMyShoppingCart
    @GetMapping("/items")
    public ShoppingCartItemListModel getItems() {
        return new ShoppingCartItemListModel(findAuthenticateCustomerShoppingCart().getItems());
    }

    @CanWriteMyShoppingCart
    @DeleteMapping("/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void emptyShoppingCart() {
        ShoppingCartOutput shoppingCart = findAuthenticateCustomerShoppingCart();
        forManagingShoppingCarts.empty(shoppingCart.getId());
    }

    @CanWriteMyShoppingCart
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addItem(@RequestBody @Valid ShoppingCartItemInput input) {
        ShoppingCartOutput shoppingCart = findAuthenticateCustomerShoppingCart();
        input.setShoppingCartId(shoppingCart.getId());
        try {
            forManagingShoppingCarts.addItem(input);
        } catch (ProductNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }
    }

    @CanWriteMyShoppingCart
    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable String itemId) {
        ShoppingCartOutput shoppingCart = findAuthenticateCustomerShoppingCart();
        forManagingShoppingCarts.removeItem(shoppingCart.getId(), itemId);
    }

    private ShoppingCartOutput findAuthenticateCustomerShoppingCart() {
        return forQueryShoppingCarts.findByCustomerId(securityCheck.getAuthenticatedUserId());
    }

}
