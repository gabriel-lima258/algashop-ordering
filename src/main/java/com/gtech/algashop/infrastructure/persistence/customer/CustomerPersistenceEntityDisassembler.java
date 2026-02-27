package com.gtech.algashop.infrastructure.persistence.customer;

import com.gtech.algashop.domain.model.commons.*;
import com.gtech.algashop.domain.model.costumer.BirthDate;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.domain.model.costumer.CustomerId;
import com.gtech.algashop.infrastructure.persistence.commons.AddressEmbeddable;
import org.springframework.stereotype.Component;

/**
 * Disassembler responsável por converter CustomerPersistenceEntity (modelo JPA)
 * de volta para o Aggregate Root Customer (modelo de domínio).
 *
 * Enquanto o Assembler vai do domínio → persistência,
 * o Disassembler faz o caminho inverso: persistência → domínio.
 *
 * Responsabilidades:
 * - Reconstruir todos os Value Objects (FullName, Email, Phone, etc.) a partir de tipos primitivos
 * - Lidar com campos opcionais que podem ser nulos (como birthDate em clientes arquivados)
 * - Utilizar o builder Customer.existing() para garantir que invariantes de domínio sejam respeitados
 */
@Component
public class CustomerPersistenceEntityDisassembler {

    /**
     * Converte uma CustomerPersistenceEntity (JPA) em um Customer (domínio).
     *
     * Usa o builder Customer.existing() porque estamos reconstruindo um cliente
     * que já existe no banco — não é um novo cadastro.
     */
    public Customer toDomainEntity(CustomerPersistenceEntity persistenceEntity) {
        return Customer.existing()
                .id(new CustomerId(persistenceEntity.getId()))
                .version(persistenceEntity.getVersion())
                .fullName(new FullName(persistenceEntity.getFirstName(), persistenceEntity.getLastName()))
                .birthDate(persistenceEntity.getBirthDate() != null ? new BirthDate(persistenceEntity.getBirthDate()) : null)
                .email(new Email(persistenceEntity.getEmail()))
                .phone(new Phone(persistenceEntity.getPhone()))
                .document(new Document(persistenceEntity.getDocument()))
                .promotionNotificationsAllowed(persistenceEntity.getPromotionNotificationsAllowed())
                .archived(persistenceEntity.getArchived())
                .registeredAt(persistenceEntity.getRegisteredAt())
                .archivedAt(persistenceEntity.getArchivedAt())
                .loyaltyPoints(new LoyaltyPoints(persistenceEntity.getLoyaltyPoints()))
                .address(toAddressValueObject(persistenceEntity.getAddress()))
                .build();
    }

    /**
     * Converte AddressEmbeddable (JPA) → Address VO (domínio).
     * Reconstrói o ZipCode VO a partir da String armazenada no banco.
     */
    private Address toAddressValueObject(AddressEmbeddable address) {
        if (address == null) return null;
        return Address.builder()
                .street(address.getStreet())
                .number(address.getNumber())
                .complement(address.getComplement())
                .neighborhood(address.getNeighborhood())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(new ZipCode(address.getZipCode()))
                .build();
    }
}
