package com.gtech.algashop.application.customer.management;

import com.gtech.algashop.application.commons.AddressData;
import com.gtech.algashop.application.util.Mapper;
import com.gtech.algashop.domain.model.commons.*;
import com.gtech.algashop.domain.model.costumer.*;
import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.order.OrderId;
import com.gtech.algashop.domain.model.order.OrderNotFoundException;
import com.gtech.algashop.domain.model.order.Orders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerManagementApplicationService {

    private final CustomerRegistrationService customerRegistration;

    // repositorios
    private final Customers customers;

    private final Mapper mapper;

    @Transactional
    public UUID create(CustomerInput input) {
        // valida null
        Objects.requireNonNull(input);
        // pega address de input
        AddressData address = input.getAddress();

        // cria uma entidade de customer
        Customer customer = customerRegistration.register(
                new FullName(input.getFirstName(), input.getLastName()),
                new BirthDate(input.getBirthDate()),
                new Email(input.getEmail()),
                new Phone(input.getPhone()),
                new Document(input.getDocument()),
                input.getPromotionNotificationsAllowed(),
                Address.builder()
                        .street(address.getStreet())
                        .number(address.getNumber())
                        .city(address.getCity())
                        .state(address.getState())
                        .neighborhood(address.getNeighborhood())
                        .complement(address.getComplement())
                        .zipCode(new ZipCode(address.getZipCode()))
                        .build()
        );
        // persiste ela no banco
        customers.add(customer);

        // retorna o ID do customer persistido
        return customer.id().value();
    }

    @Transactional(readOnly = true)
    public CustomerOutput findById(UUID customerId) {
        Objects.requireNonNull(customerId);

        Customer customer = customers.ofId(new CustomerId(customerId))
                .orElseThrow(CustomerNotFoundException::new);

        // mapeia automaticamente de customer para customerDto
        return mapper.convert(customer, CustomerOutput.class);
    }

    @Transactional
    public void update(UUID customerId, CustomerUpdateInput input) {
        Objects.requireNonNull(customerId);
        Objects.requireNonNull(input);

        // procura o customer em persistencia
        Customer customer = customers.ofId(new CustomerId(customerId))
                .orElseThrow(CustomerNotFoundException::new);

        // alterações a serem feitas
        customer.changeName(new FullName(input.getFirstName(), input.getLastName()));
        customer.changePhone(new Phone(input.getPhone()));

        if (Boolean.TRUE.equals(input.getPromotionNotificationsAllowed())) {
            customer.enablePromotionNotifications();
        } else {
            customer.disablePromotionNotifications();
        }

        AddressData address = input.getAddress();

        customer.changeAddress(Address.builder()
                .street(address.getStreet())
                .number(address.getNumber())
                .city(address.getCity())
                .state(address.getState())
                .neighborhood(address.getNeighborhood())
                .complement(address.getComplement())
                .zipCode(new ZipCode(address.getZipCode()))
                .build());

        // persistindo no banco
        customers.add(customer);
    }

    @Transactional
    public void archive(UUID customerId) {
        Objects.requireNonNull(customerId);

        // procura o customer em persistencia
        Customer customer = customers.ofId(new CustomerId(customerId))
                .orElseThrow(CustomerNotFoundException::new);
        customer.archive();
        customers.add(customer);
    }

    @Transactional
    public void changeEmail(UUID customerId, String newEmail) {
        Objects.requireNonNull(customerId);

        Customer customer = customers.ofId(new CustomerId(customerId))
                .orElseThrow(CustomerNotFoundException::new);

        customerRegistration.changeEmail(customer, new Email(newEmail));
        customers.add(customer);
    }

}
