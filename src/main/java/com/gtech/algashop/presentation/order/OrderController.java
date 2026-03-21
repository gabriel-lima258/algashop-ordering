package com.gtech.algashop.presentation.order;

import com.gtech.algashop.application.checkout.BuyNowApplicationService;
import com.gtech.algashop.application.checkout.BuyNowInput;
import com.gtech.algashop.application.checkout.CheckoutApplicationService;
import com.gtech.algashop.application.checkout.CheckoutInput;
import com.gtech.algashop.application.order.query.OrderDetailOutput;
import com.gtech.algashop.application.order.query.OrderFilter;
import com.gtech.algashop.application.order.query.OrderQueryService;
import com.gtech.algashop.application.order.query.OrderSummaryOutput;
import com.gtech.algashop.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.domain.model.shoppingcart.ShoppingCartNotFound;
import com.gtech.algashop.presentation.PageModel;
import com.gtech.algashop.presentation.UnprocessableEntityException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderQueryService orderQueryService;

    private final BuyNowApplicationService buyNowService;

    private final CheckoutApplicationService checkoutService;

    @GetMapping
    public PageModel<OrderSummaryOutput> findAll(OrderFilter filter) {
        return PageModel.of(orderQueryService.filter(filter));
    }

    @GetMapping("/{orderId}")
    public OrderDetailOutput findById(@PathVariable String orderId) {
        return orderQueryService.findById(orderId);
    }

    @PostMapping(consumes = "application/vnd.order-with-product.v1+json")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailOutput createWithProduct(@RequestBody @Valid BuyNowInput input) {
        String orderId;

        try {
            orderId = buyNowService.buyNow(input);
        } catch (CustomerNotFoundException | ProductNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }

        return orderQueryService.findById(orderId);
    }

    @PostMapping(consumes = "application/vnd.order-with-shopping-cart.v1+json")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailOutput createWithShoppingCart(@Valid @RequestBody CheckoutInput input) {
        String orderId;

        try {
            orderId = checkoutService.checkout(input);
        } catch (CustomerNotFoundException | ShoppingCartNotFound e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }

        return orderQueryService.findById(orderId);
    }
}
