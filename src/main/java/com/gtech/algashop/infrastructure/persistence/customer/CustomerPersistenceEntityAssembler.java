package com.gtech.algashop.infrastructure.persistence.customer;

import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.commons.Address;
import com.gtech.algashop.infrastructure.persistence.commons.AddressEmbeddable;
import org.springframework.stereotype.Component;

@Component
public class CustomerPersistenceEntityAssembler {

    public CustomerPersistenceEntity fromDomain(Customer customer) {
        // Passa uma entidade vazia para merge() — evita duplicar a lógica de mapeamento
        return merge(new CustomerPersistenceEntity(), customer);
    }

    public CustomerPersistenceEntity merge(CustomerPersistenceEntity customerPersistenceEntity, Customer customer) {
        customerPersistenceEntity.setId(customer.id().value());
        customerPersistenceEntity.setFirstName(customer.fullName().firstName());
        customerPersistenceEntity.setLastName(customer.fullName().lastName());
        customerPersistenceEntity.setBirthDate(customer.birthDate() != null ? customer.birthDate().birthDate() : null);
        customerPersistenceEntity.setEmail(customer.email().email());
        customerPersistenceEntity.setPhone(customer.phone().phone());
        customerPersistenceEntity.setDocument(customer.document().document());
        customerPersistenceEntity.setPromotionNotificationsAllowed(customer.isPromotionNotificationsAllowed());
        customerPersistenceEntity.setArchived(customer.isArchived());
        customerPersistenceEntity.setRegisteredAt(customer.registeredAt());
        customerPersistenceEntity.setArchivedAt(customer.archivedAt());
        customerPersistenceEntity.setLoyaltyPoints(customer.loyaltyPoints().point());

        // Value Objects complexos são convertidos para seus respectivos Embeddables JPA
        customerPersistenceEntity.setAddress(convertAddressEmbeddable(customer.address()));

        // Controle de concorrência otimista: o JPA usa o @Version para detectar conflitos de escrita simultânea
        customerPersistenceEntity.setVersion(customer.version());
        // persiste os eventos disparados
        customerPersistenceEntity.addEvents(customer.domainEvents());

        return customerPersistenceEntity;
    }

    /**
     * Converte o Value Object Address (domínio) para AddressEmbeddable (JPA).
     * Reutilizado tanto pelo Billing quanto pelo Shipping, pois ambos possuem endereço.
     */
    private AddressEmbeddable convertAddressEmbeddable(Address address) {
        if (address == null) return null;

        return AddressEmbeddable.builder()
                .street(address.street())
                .number(address.number())
                .complement(address.complement())
                .neighborhood(address.neighborhood())
                .city(address.city())
                .state(address.state())
                .zipCode(address.zipCode().zipcode()) // ZipCode VO → String
                .build();
    }

}
