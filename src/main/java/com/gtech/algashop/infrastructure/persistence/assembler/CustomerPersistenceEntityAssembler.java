package com.gtech.algashop.infrastructure.persistence.assembler;

import com.gtech.algashop.domain.model.entity.Customer;
import com.gtech.algashop.domain.model.entity.VO.Address;
import com.gtech.algashop.infrastructure.persistence.embeddable.AddressEmbeddable;
import com.gtech.algashop.infrastructure.persistence.entity.CustomerPersistenceEntity;
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
        customerPersistenceEntity.setBirthDate(customer.birthDate().birthDate());
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
