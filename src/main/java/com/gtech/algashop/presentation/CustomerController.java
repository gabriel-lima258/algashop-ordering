package com.gtech.algashop.presentation;

import com.gtech.algashop.application.commons.AddressData;
import com.gtech.algashop.application.customer.management.CustomerInput;
import com.gtech.algashop.application.customer.management.CustomerManagementApplicationService;
import com.gtech.algashop.application.customer.management.CustomerUpdateInput;
import com.gtech.algashop.application.customer.query.CustomerFilter;
import com.gtech.algashop.application.customer.query.CustomerOutput;
import com.gtech.algashop.application.customer.query.CustomerQueryService;
import com.gtech.algashop.application.customer.query.CustomerSummaryOutput;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
@RequestMapping("api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerManagementApplicationService customerManagementApplicationService;
    private final CustomerQueryService customerQueryService;


    @GetMapping
    public PageModel<CustomerSummaryOutput> findAll(CustomerFilter customerFilter) {
        return PageModel.of(customerQueryService.filter(customerFilter));
    }

    @GetMapping("/{customerId}")
    public CustomerOutput findById(@PathVariable UUID customerId) {
        return customerQueryService.findById(customerId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOutput create(@RequestBody @Valid CustomerInput input, HttpServletResponse httpServletResponse) {
        UUID customerId = customerManagementApplicationService.create(input);

        // Cria um proxy de CustomerController para capturar os metadados do método findById
        // sem executá-lo de fato. O proxy usa as anotações @RequestMapping e @GetMapping
        // do método alvo para derivar o path: "api/v1/customers/{customerId}".
        // fromMethodCall combina esse path com o scheme, host e porta da requisição atual,
        // produzindo a URI completa do recurso criado (ex: http://host/api/v1/customers/{id}).
        UriComponentsBuilder builder = fromMethodCall(on(CustomerController.class).findById(customerId));

        // Adiciona o header Location na resposta 201 Created, conforme RFC 7231,
        // indicando ao cliente onde o novo recurso pode ser encontrado.
        httpServletResponse.addHeader("Location", builder.toUriString());

        return customerQueryService.findById(customerId);
    }

    @PutMapping("/{customerId}")
    public CustomerOutput update(@PathVariable UUID customerId, @RequestBody @Valid CustomerUpdateInput input) {
        customerManagementApplicationService.update(customerId, input);
        return customerQueryService.findById(customerId);
    }

    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void archiveCustomer(@PathVariable UUID customerId) {
        customerManagementApplicationService.archive(customerId);
    }

}