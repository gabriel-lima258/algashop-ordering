package com.gtech.algashop.infrastructure.adapters.in.web.customer;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.ports.in.customer.*;
import com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.CanReadMyCustomerProfile;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.*;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

// controller para profiles /me isso evitar de expor id nos path e body para evitar ataques de IDOR
// utilizando o security checks que envia o id do usuario via token para identificar ele

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customers/me")
public class MyCustomerController {

    private final ForManagingCustomer forManagingCustomer;
    private final ForQueryCustomers forQueryCustomers;
    private final SecurityCheckApplicationService securityCheck;

    @CanReadMyCustomerProfile
    @GetMapping
    public CustomerOutput load() {
        return forQueryCustomers.findById(securityCheck.getAuthenticatedUserId());
    }

    @CanWriteMyCustomerProfile
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOutput create(@RequestBody @Valid CustomerInput input, HttpServletResponse httpServletResponse) {
        // cadastra um customer atraves de um user do authorization server cadastrado
        UUID customerId = forManagingCustomer.create(securityCheck.getAuthenticatedUserId(), input);

        UriComponentsBuilder builder = fromMethodCall(on(MyCustomerController.class).load());

        // Adiciona o header Location na resposta 201 Created, conforme RFC 7231,
        // indicando ao cliente onde o novo recurso pode ser encontrado.
        httpServletResponse.addHeader("Location", builder.toUriString());

        return forQueryCustomers.findById(customerId);
    }

    @CanWriteMyCustomerProfile
    @PutMapping
    public CustomerOutput update(@RequestBody @Valid CustomerUpdateInput input) {
        forManagingCustomer.update(securityCheck.getAuthenticatedUserId(), input);
        return forQueryCustomers.findById(securityCheck.getAuthenticatedUserId());
    }

}
