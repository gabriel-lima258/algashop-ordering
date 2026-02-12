package com.gtech.algashop.domain.entity;

import com.gtech.algashop.domain.exceptions.CustomerArchivedException;
import com.gtech.algashop.domain.exceptions.LoyaltyValueException;
import com.gtech.algashop.domain.util.FieldValidations;
import com.gtech.algashop.domain.valueobject.CustomerId;
import com.gtech.algashop.domain.valueobject.FullName;
import com.gtech.algashop.domain.valueobject.LoyaltyPoints;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.gtech.algashop.domain.exceptions.ErrorMessages.*;

@Getter
@Setter
@NoArgsConstructor
public class Customer {
    private CustomerId id;
    private FullName fullName;
    private LocalDate birthDate;
    private String email;
    private String phone;
    private String document;
    private Boolean promotionNotificationsAllowed;
    private Boolean archived;
    private OffsetDateTime registeredAt;
    private OffsetDateTime archivedAt;
    private LoyaltyPoints loyaltyPoints;

    public Customer(CustomerId id, FullName fullName, LocalDate birthDate, String email,
                    String phone, String document, Boolean promotionNotificationsAllowed,
                    OffsetDateTime registeredAt) {
        this.setId(id);
        this.setFullName(fullName);
        this.setBirthDate(birthDate);
        this.setEmail(email);
        this.setPhone(phone);
        this.setDocument(document);
        this.setPromotionNotificationsAllowed(promotionNotificationsAllowed);
        this.setRegisteredAt(registeredAt);
        this.setArchived(false);
        this.setLoyaltyPoints(LoyaltyPoints.ZERO);
    }

    public Customer(CustomerId id, FullName fullName, LocalDate birthDate, String email, String phone,
                    String document, Boolean promotionNotificationsAllowed, Boolean archived,
                    OffsetDateTime registeredAt, OffsetDateTime archivedAt, LoyaltyPoints loyaltyPoints) {
        this.setId(id);
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
    }



    /////////////////////////////////////
    ///  METHODS
    ////////////////////////////////////

    public void addLoyaltyPoints(LoyaltyPoints pointsAdded) {
        verifyIsChangeble();
        this.setLoyaltyPoints(this.loyaltyPoints().add(pointsAdded));
    }

    public void archive() {
        verifyIsChangeble();
        this.setArchived(true);
        this.setEmail(UUID.randomUUID() + "@anonymous.com");
        this.setArchivedAt(OffsetDateTime.now());
        this.setFullName(new FullName("Anonymous", "Anonymous"));
        this.setPhone("000-000-0000");
        this.setDocument("000-00-0000");
        this.setBirthDate(null);
        this.setPromotionNotificationsAllowed(false);
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

    public void changeEmail(String email) {
        verifyIsChangeble();
        this.setEmail(email);
    }

    public void changePhone(String phone) {
        verifyIsChangeble();
        this.setPhone(phone);
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

    public LocalDate birthDate() {
        return birthDate;
    }

    public String document() {
        return document;
    }

    public String email() {
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

    public String phone() {
        return phone;
    }

    public Boolean isPromotionNotificationsAllowed() {
        return promotionNotificationsAllowed;
    }

    public OffsetDateTime registeredAt() {
        return registeredAt;
    }

    /////////////////////////////////////
    ///  SETTERS
    ////////////////////////////////////

    private void setArchived(Boolean archived) {
        Objects.requireNonNull(archived);
        this.archived = archived;
    }

    private void setArchivedAt(OffsetDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    private void setBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            this.birthDate = null;
            return;
        }
        // verifica se data está no passado
        if (birthDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(VALIDATION_ERROR_BIRTHDATE_MUST_IN_PAST);
        }
        this.birthDate = birthDate;
    }

    private void setDocument(String document) {
        Objects.requireNonNull(document);
        this.document = document;
    }

    private void setEmail(String email) {
        FieldValidations.requiresEmailValid(email, VALIDATION_ERROR_EMAIL_IS_INVALID);
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

    private void setPhone(String phone) {
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
