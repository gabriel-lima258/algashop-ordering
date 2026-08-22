package com.gtech.algashop.infrastructure.adapters.in.web.customer;

import com.gtech.algashop.core.ports.in.customer.ForManagingCustomer;
import com.gtech.algashop.core.ports.in.customer.CustomerFilter;
import com.gtech.algashop.core.ports.in.customer.CustomerOutput;
import com.gtech.algashop.core.ports.in.customer.ForQueryCustomers;
import com.gtech.algashop.core.ports.in.customer.CustomerSummaryOutput;
import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartOutput;
import com.gtech.algashop.core.ports.in.shoppingcart.ForQueryShoppingCarts;
import com.gtech.algashop.infrastructure.adapters.in.web.PageModel;
import com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.CanReadCustomers;
import com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.CanReadShoppingCarts;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

// controller administrativo, apenas MANAGER e OPERATOR tem acesso

@RestController
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ForManagingCustomer forManagingCustomer;
    private final ForQueryCustomers forQueryCustomers;
    private final ForQueryShoppingCarts forQueryShoppingCarts;

    @CanReadCustomers
    @GetMapping
    public PageModel<CustomerSummaryOutput> findAll(CustomerFilter customerFilter) {
        return PageModel.of(forQueryCustomers.filter(customerFilter));
    }

    @CanReadCustomers
    @GetMapping("/{customerId}")
    public CustomerOutput findById(@PathVariable UUID customerId) {
        return forQueryCustomers.findById(customerId);
    }

    // exige escopo de CARRINHO, nao de cliente - e a AuthorizationMatrixTest fixa isso
    @CanReadShoppingCarts
    @GetMapping("/{customerId}/shopping-cart")
    public ShoppingCartOutput findShoppinCartByCustomerId(@PathVariable UUID customerId) {
        return forQueryShoppingCarts.findByCustomerId(customerId);
    }

}