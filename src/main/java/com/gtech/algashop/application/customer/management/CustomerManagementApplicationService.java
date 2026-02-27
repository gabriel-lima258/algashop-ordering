package com.gtech.algashop.application.service;

import com.gtech.algashop.application.model.AddressData;
import com.gtech.algashop.application.model.CustomerInput;
import com.gtech.algashop.application.model.CustomerOutput;
import com.gtech.algashop.domain.model.commons.*;
import com.gtech.algashop.domain.model.costumer.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerManagementApplicationService {

    private final CustomerRegistrationService customerRegistration;
    private final Customers customers;

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

        return CustomerOutput.builder()
                .id(customer.id().value())
                .firstName(customer.fullName().firstName())
                .lastName(customer.fullName().lastName())
                .email(customer.email().email())
                .document(customer.document().document())
                .phone(customer.phone().phone())
                .promotionNotificationsAllowed(customer.isPromotionNotificationsAllowed())
                .loyaltyPoints(customer.loyaltyPoints().point())
                .registeredAt(customer.registeredAt())
                .archived(customer.isArchived())
                .archivedAt(customer.archivedAt() != null ? customer.archivedAt() : null)
                .birthDate(customer.birthDate() != null ? customer.birthDate().birthDate() : null)
                .address(AddressData.builder()
                        .street(customer.address().street())
                        .number(customer.address().number())
                        .complement(customer.address().complement())
                        .neighborhood(customer.address().neighborhood())
                        .city(customer.address().city())
                        .state(customer.address().state())
                        .zipCode(customer.address().zipCode().zipcode())
                        .build())
                .build();
    }
}
