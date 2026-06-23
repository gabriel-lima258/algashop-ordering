package com.gtech.algashop.infrastructure.adapters.in.web.customer;

import com.gtech.algashop.core.ports.in.customer.CustomerInput;
import com.gtech.algashop.core.ports.in.customer.CustomerUpdateInput;
import com.gtech.algashop.core.ports.in.customer.ForManagingCustomer;
import com.gtech.algashop.core.ports.in.customer.CustomerFilter;
import com.gtech.algashop.core.ports.in.customer.CustomerOutput;
import com.gtech.algashop.core.ports.in.customer.ForQueryCustomers;
import com.gtech.algashop.core.ports.in.customer.CustomerSummaryOutput;
import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartOutput;
import com.gtech.algashop.core.ports.in.shoppingcart.ForQueryShoppingCarts;
import com.gtech.algashop.infrastructure.adapters.in.web.PageModel;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ForManagingCustomer forManagingCustomer;
    private final ForQueryCustomers forQueryCustomers;
    private final ForQueryShoppingCarts forQueryShoppingCarts;

    @GetMapping
    public PageModel<CustomerSummaryOutput> findAll(CustomerFilter customerFilter) {
        return PageModel.of(forQueryCustomers.filter(customerFilter));
    }

    @GetMapping("/{customerId}")
    public CustomerOutput findById(@PathVariable UUID customerId) {
        return forQueryCustomers.findById(customerId);
    }

    @GetMapping("/{customerId}/shopping-cart")
    public ShoppingCartOutput findShoppinCartByCustomerId(@PathVariable UUID customerId) {
        return forQueryShoppingCarts.findByCustomerId(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOutput create(@RequestBody @Valid CustomerInput input, HttpServletResponse httpServletResponse) {
        UUID customerId = forManagingCustomer.create(input);

        // Cria um proxy de CustomerController para capturar os metadados do método findById
        // sem executá-lo de fato. O proxy usa as anotações @RequestMapping e @GetMapping
        // do método alvo para derivar o path: "api/v1/customers/{customerId}".
        // fromMethodCall combina esse path com o scheme, host e porta da requisição atual,
        // produzindo a URI completa do recurso criado (ex: http://host/api/v1/customers/{id}).
        UriComponentsBuilder builder = fromMethodCall(on(CustomerController.class).findById(customerId));

        // Adiciona o header Location na resposta 201 Created, conforme RFC 7231,
        // indicando ao cliente onde o novo recurso pode ser encontrado.
        httpServletResponse.addHeader("Location", builder.toUriString());

        return forQueryCustomers.findById(customerId);
    }

    @PutMapping("/{customerId}")
    public CustomerOutput update(@PathVariable UUID customerId, @RequestBody @Valid CustomerUpdateInput input) {
        forManagingCustomer.update(customerId, input);
        return forQueryCustomers.findById(customerId);
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveCustomer(@PathVariable UUID customerId) {
        forManagingCustomer.archive(customerId);
    }

}