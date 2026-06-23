package com.gtech.algashop.core.application.customer;

import com.gtech.algashop.core.ports.in.commons.AddressData;
import com.gtech.algashop.core.domain.model.commons.*;
import com.gtech.algashop.core.domain.model.costumer.*;
import com.gtech.algashop.core.ports.in.customer.CustomerInput;
import com.gtech.algashop.core.ports.in.customer.CustomerUpdateInput;
import com.gtech.algashop.core.ports.in.customer.ForManagingCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerManagementApplicationService implements ForManagingCustomer {

    private final CustomerRegistrationService customerRegistration;

    // repositorios
    private final Customers customers;

    @Transactional
    @Override
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

    @Transactional
    @Override
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
    @Override
    public void archive(UUID customerId) {
        Objects.requireNonNull(customerId);

        // procura o customer em persistencia
        Customer customer = customers.ofId(new CustomerId(customerId))
                .orElseThrow(CustomerNotFoundException::new);
        customer.archive();
        customers.add(customer);
    }

    @Transactional
    @Override
    public void changeEmail(UUID customerId, String newEmail) {
        Objects.requireNonNull(customerId);

        Customer customer = customers.ofId(new CustomerId(customerId))
                .orElseThrow(CustomerNotFoundException::new);

        customerRegistration.changeEmail(customer, new Email(newEmail));
        customers.add(customer);
    }

}
