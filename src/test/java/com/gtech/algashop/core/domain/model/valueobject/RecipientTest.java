package com.gtech.algashop.core.domain.model.valueobject;

import com.gtech.algashop.core.domain.model.commons.Document;
import com.gtech.algashop.core.domain.model.commons.FullName;
import com.gtech.algashop.core.domain.model.commons.Phone;
import com.gtech.algashop.core.domain.model.order.Recipient;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RecipientTest {

    private FullName validName() {
        return new FullName("Gabriel", "Lima");
    }

    private Document validDocument() {
        return new Document("12345678900");
    }

    private Phone validPhone() {
        return new Phone("61999999999");
    }

    @Test
    void shouldCreateRecipientWhenAllFieldsAreValid() {
        Recipient recipient = Recipient.builder()
                .fullName(validName())
                .document(validDocument())
                .phone(validPhone())
                .build();

        assertNotNull(recipient);
        assertEquals(validName(), recipient.fullName());
        assertEquals(validDocument(), recipient.document());
        assertEquals(validPhone(), recipient.phone());
    }

    @Test
    void shouldThrowWhenFullNameIsNull() {
        assertThatThrownBy(() ->
                Recipient.builder()
                        .fullName(null)
                        .document(validDocument())
                        .phone(validPhone())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenDocumentIsNull() {
        assertThatThrownBy(() ->
                Recipient.builder()
                        .fullName(validName())
                        .document(null)
                        .phone(validPhone())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenPhoneIsNull() {
        assertThatThrownBy(() ->
                Recipient.builder()
                        .fullName(validName())
                        .document(validDocument())
                        .phone(null)
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }
}