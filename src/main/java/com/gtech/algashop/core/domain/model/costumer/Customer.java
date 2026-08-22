package com.gtech.algashop.core.domain.model.costumer;

import com.gtech.algashop.core.domain.model.AbstractEventSourceEntity;
import com.gtech.algashop.core.domain.model.AggregateRoot;
import com.gtech.algashop.core.domain.model.commons.*;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.gtech.algashop.core.domain.model.ErrorMessages.VALIDATION_ERROR_FULLNAME_IS_NULL;

/*
 * Aggregate Root = entidade principal que controla todos os objetos internos
 * garantindo consistência do domínio.
 *
 * Nenhum objeto interno deve ser alterado diretamente de fora.
 * Nenhum membro value ou entity dentro de root deve conhecer o outro
 * dentro da entidade root! Somente se fizer parte de sua relação
 * Toda modificação passa por métodos de domínio do Customer.
 */
public class Customer
        extends AbstractEventSourceEntity
        implements AggregateRoot<CustomerId> {
    private CustomerId id;                  // Identidade da entidade (Entity ID)
    private FullName fullName;              // Value Object (imutável)
    private BirthDate birthDate;            // Value Object
    private Email email;                    // Value Object
    private Phone phone;                    // Value Object
    private Document document;              // Value Object
    private Boolean promotionNotificationsAllowed; // Estado de regra de negócio
    private Boolean archived;               // Soft delete lógico
    private OffsetDateTime registeredAt;    // Auditoria de criação
    private OffsetDateTime archivedAt;      // Auditoria de arquivamento
    private LoyaltyPoints loyaltyPoints;    // Value Object com comportamento
    private Address address;                // Value Object complexo

    // controle de concorrencia no banco
    private Long version;

    // construtor para novo cliente, padrao static factory
    @Builder(builderClassName = "BrandNewCustomerBuild", builderMethodName = "brandNew")
    private static Customer createBrandNew(CustomerId id, FullName fullName, BirthDate birthDate, Email email,
                                          Phone phone, Document document, Boolean promotionNotificationsAllowed,
                                          Address address) {
        if (id == null) {
            id = new CustomerId();
        }

        Customer customer = new Customer(
                id,
                null,
                fullName,
                birthDate,
                email,
                phone,
                document,
                promotionNotificationsAllowed,
                false,
                OffsetDateTime.now(),
                null,
                LoyaltyPoints.ZERO,
                address);

        customer.publishDomainEvent(new
                CustomerRegisteredEvent(
                        customer.id(),
                        customer.registeredAt(),
                        customer.fullName(),
                        customer.email()));

        return customer;
    }

    @Builder(builderClassName = "ExistingCustomerBuild", builderMethodName = "existing")
    private Customer(CustomerId id, Long version, FullName fullName, BirthDate birthDate, Email email, Phone phone,
                    Document document, Boolean promotionNotificationsAllowed, Boolean archived,
                    OffsetDateTime registeredAt, OffsetDateTime archivedAt, LoyaltyPoints loyaltyPoints, Address address) {
        this.setId(id);
        this.setVersion(version);
        this.setFullName(fullName);
        this.setBirthDate(birthDate);
        this.setEmail(email);
        this.setPhone(phone);
        this.setDocument(document);
        this.setPromotionNotificationsAllowed(promotionNotificationsAllowed);
        this.setArchived(archived);
        this.setRegisteredAt(registeredAt);
        this.setArchivedAt(archivedAt);
        this.setLoyaltyPoints(loyaltyPoints);
        this.setAddress(address);
    }

    /////////////////////////////////////
    ///  METHODS
    ////////////////////////////////////

    public void addLoyaltyPoints(LoyaltyPoints pointsAdded) {
        verifyIsChangeble();
        // ignorar valores 0
        if (pointsAdded.equals(LoyaltyPoints.ZERO)) {
            return;
        }
        this.setLoyaltyPoints(this.loyaltyPoints().add(pointsAdded));
    }

    public void archive() {
        verifyIsChangeble();
        this.setArchived(true);
        this.setEmail(new Email(UUID.randomUUID() + "@anonymous.com"));
        this.setArchivedAt(OffsetDateTime.now());
        this.setFullName(new FullName("Anonymous", "Anonymous"));
        this.setPhone(new Phone("000-000-0000"));
        this.setDocument(new Document("000-00-0000"));
        this.setBirthDate(null);
        this.setPromotionNotificationsAllowed(false);
        this.setAddress(this.address().toBuilder()
                .number("Anonymized")
                .complement(null)
                .build());

        this.publishDomainEvent(new CustomerArchivedEvent(this.id(), this.archivedAt()));
    }

    public void enablePromotionNotifications() {
        verifyIsChangeble();
        this.setPromotionNotificationsAllowed(true);
    }

    public void disablePromotionNotifications() {
        verifyIsChangeble();
        this.setPromotionNotificationsAllowed(false);
    }

    public void changeName(FullName fullName) {
        verifyIsChangeble();
        this.setFullName(fullName);
    }

    public void changeEmail(Email email) {
        verifyIsChangeble();
        this.setEmail(email);
    }

    public void changePhone(Phone phone) {
        verifyIsChangeble();
        this.setPhone(phone);
    }

    public void changeAddress(Address address) {
        verifyIsChangeble();
        this.setAddress(address);
    }

    /////////////////////////////////////
    ///  GETTERS
    ////////////////////////////////////

    public Boolean isArchived() {
        return archived;
    }

    public OffsetDateTime archivedAt() {
        return archivedAt;
    }

    public BirthDate birthDate() {
        return birthDate;
    }

    public Document document() {
        return document;
    }

    public Email email() {
        return email;
    }

    public FullName fullName() {
        return fullName;
    }

    public CustomerId id() {
        return id;
    }

    public LoyaltyPoints loyaltyPoints() {
        return loyaltyPoints;
    }

    public Phone phone() {
        return phone;
    }

    public Boolean isPromotionNotificationsAllowed() {
        return promotionNotificationsAllowed;
    }

    public OffsetDateTime registeredAt() {
        return registeredAt;
    }

    public Address address() {
        return address;
    }

    public Long version() {
        return version;
    }

    /////////////////////////////////////
    ///  SETTERS
    ////////////////////////////////////

    private void setVersion(Long version) {
        this.version = version;
    }

    private void setArchived(Boolean archived) {
        Objects.requireNonNull(archived);
        this.archived = archived;
    }

    private void setArchivedAt(OffsetDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    private void setBirthDate(BirthDate birthDate) {
        if (birthDate == null) {
            this.birthDate = null;
            return;
        }
        this.birthDate = birthDate;
    }

    private void setDocument(Document document) {
        Objects.requireNonNull(document);
        this.document = document;
    }

    private void setEmail(Email email) {
        this.email = email;
    }

    private void setFullName(FullName fullName) {
        Objects.requireNonNull(fullName, VALIDATION_ERROR_FULLNAME_IS_NULL);
        this.fullName = fullName;
    }

    private void setId(CustomerId id) {
        Objects.requireNonNull(id);
        this.id = id;
    }

    private void setLoyaltyPoints(LoyaltyPoints loyaltyPoints) {
        Objects.requireNonNull(loyaltyPoints);
        this.loyaltyPoints = loyaltyPoints;
    }

    private void setPhone(Phone phone) {
        Objects.requireNonNull(phone);
        this.phone = phone;
    }

    private void setPromotionNotificationsAllowed(Boolean promotionNotificationsAllowed) {
        Objects.requireNonNull(promotionNotificationsAllowed);
        this.promotionNotificationsAllowed = promotionNotificationsAllowed;
    }

    private void setRegisteredAt(OffsetDateTime registeredAt) {
        Objects.requireNonNull(registeredAt);
        this.registeredAt = registeredAt;
    }

    private void setAddress(Address address) {
        Objects.requireNonNull(address);
        this.address = address;
    }

    private void verifyIsChangeble() {
        if (this.isArchived()) {
            throw new CustomerArchivedException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
