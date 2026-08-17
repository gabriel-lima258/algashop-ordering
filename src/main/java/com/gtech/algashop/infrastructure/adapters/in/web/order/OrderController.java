package com.gtech.algashop.infrastructure.adapters.in.web.order;

import com.gtech.algashop.core.ports.in.checkout.BuyNowInput;
import com.gtech.algashop.core.ports.in.checkout.CheckoutInput;
import com.gtech.algashop.core.ports.in.checkout.ForBuyingProduct;
import com.gtech.algashop.core.ports.in.checkout.ForBuyingWithShoppingCart;
import com.gtech.algashop.core.ports.in.order.ForQueryOrders;
import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.ports.in.order.OrderFilter;
import com.gtech.algashop.core.ports.out.order.OrderSummaryOutput;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.core.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartNotFound;
import com.gtech.algashop.infrastructure.adapters.in.web.PageModel;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.UnprocessableEntityException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.*;

@RestController
@RequestMapping(path = "/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final ForQueryOrders forQueryOrders;

    private final ForBuyingProduct forBuyingProduct;

    private final ForBuyingWithShoppingCart forBuyingWithShoppingCart;

    @CanReadOrders
    @GetMapping
    public PageModel<OrderSummaryOutput> findAll(OrderFilter filter) {
        return PageModel.of(forQueryOrders.filter(filter));
    }

    @CanReadOrders
    @GetMapping("/{orderId}")
    public OrderDetailOutput findById(@PathVariable String orderId) {
        return forQueryOrders.findById(orderId);
    }

    @CanWriteOrders
    @PostMapping(consumes = "application/vnd.order-with-product.v1+json")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailOutput createWithProduct(@RequestBody @Valid BuyNowInput input) {
        String orderId;

        try {
            orderId = forBuyingProduct.buyNow(input);
        } catch (CustomerNotFoundException | ProductNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }

        return forQueryOrders.findById(orderId);
    }

    @CanWriteOrders
    @PostMapping(consumes = "application/vnd.order-with-shopping-cart.v1+json")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailOutput createWithShoppingCart(@Valid @RequestBody CheckoutInput input) {
        String orderId;

        try {
            orderId = forBuyingWithShoppingCart.checkout(input);
        } catch (CustomerNotFoundException | ShoppingCartNotFound e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }

        return forQueryOrders.findById(orderId);
    }
}
