package com.gtech.algashop.presentation.shoppingcart;

import com.gtech.algashop.application.shoppingcart.management.ShoppingCartInput;
import com.gtech.algashop.application.shoppingcart.management.ShoppingCartItemInput;
import com.gtech.algashop.application.shoppingcart.management.ShoppingCartManagementApplicationService;
import com.gtech.algashop.application.shoppingcart.query.ShoppingCartItemListModel;
import com.gtech.algashop.application.shoppingcart.query.ShoppingCartItemOutput;
import com.gtech.algashop.application.shoppingcart.query.ShoppingCartOutput;
import com.gtech.algashop.application.shoppingcart.query.ShoppingCartQueryService;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.presentation.UnprocessableEntityException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/shopping-carts")
@RequiredArgsConstructor
public class ShoppingCartController {

    private final ShoppingCartQueryService shoppingCartQueryService;

    private final ShoppingCartManagementApplicationService shoppingCartManagementApplicationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCartOutput addItemInShoppingCart(@RequestBody @Valid ShoppingCartInput input) {
        UUID shoppingCartId;
        try {
            shoppingCartId = shoppingCartManagementApplicationService.createNew(input.getCustomerId());
        } catch (CustomerNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }
        return shoppingCartQueryService.findById(shoppingCartId);
    }

    @GetMapping("/{shoppingCartId}")
    public ShoppingCartOutput findById(@PathVariable UUID shoppingCartId) {
        return shoppingCartQueryService.findById(shoppingCartId);
    }

    @GetMapping("/{shoppingCartId}/items")
    public ShoppingCartItemListModel findItemsFromShoppingById(@PathVariable UUID shoppingCartId) {
        List<ShoppingCartItemOutput> items = shoppingCartQueryService.findById(shoppingCartId).getItems();
        return new ShoppingCartItemListModel(items);
    }

    @DeleteMapping("/{shoppingCartId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteShoppingCart(@PathVariable UUID shoppingCartId) {
        shoppingCartManagementApplicationService.delete(shoppingCartId);
    }

    @DeleteMapping("/{shoppingCartId}/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void emptyAllItemsFromShoppingCart(@PathVariable UUID shoppingCartId) {
        shoppingCartManagementApplicationService.empty(shoppingCartId);
    }

    @PostMapping("/{shoppingCartId}/items")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addItemInShoppingCart(@PathVariable UUID shoppingCartId, @RequestBody @Valid ShoppingCartItemInput input) {
        input.setShoppingCartId(shoppingCartId);
        try {
            shoppingCartManagementApplicationService.addItem(input);
        } catch (ProductNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }
    }

    @DeleteMapping("/{shoppingCartId}/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void emptyAllItemsFromShoppingCart(@PathVariable UUID shoppingCartId, @PathVariable String itemId) {
        shoppingCartManagementApplicationService.removeItem(shoppingCartId, itemId);
    }

}
