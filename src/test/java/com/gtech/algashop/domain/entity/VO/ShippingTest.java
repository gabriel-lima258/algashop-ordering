package com.gtech.algashop.domain.entity.VO;

import com.gtech.algashop.domain.entity.factory.OrderTestDataBuilder;
import com.gtech.algashop.domain.entity.factory.ProductTestDataBuilder;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ShippingTest {

    private Money validMoney() {
        return new Money("20");
    }

    private Recipient validRecipient() {
        return Recipient.builder()
                .fullName(new FullName("Gabriel", "Lima"))
                .document(new Document("12345678900"))
                .phone(new Phone("61999999999"))
                .build();
    }

    private Address validAddress() {
        return OrderTestDataBuilder.anAddress();
    }

    @Test
    void shouldCreateShippingWhenAllFieldsAreValid() {
        Shipping shipping = Shipping.builder()
                .cost(validMoney())
                .expectedDate(LocalDate.now().plusDays(3))
                .recipient(validRecipient())
                .address(validAddress())
                .build();

        assertNotNull(shipping);
        assertEquals(validMoney(), shipping.cost());
        assertEquals(validRecipient(), shipping.recipient());
        assertEquals(validAddress(), shipping.address());
    }

    @Test
    void shouldThrowWhenCostIsNull() {
        assertThrows(NullPointerException.class, () ->
                Shipping.builder()
                        .cost(null)
                        .expectedDate(LocalDate.now())
                        .recipient(validRecipient())
                        .address(validAddress())
                        .build()
        );
    }

    @Test
    void shouldThrowWhenExpectedDateIsNull() {
        assertThrows(NullPointerException.class, () ->
                Shipping.builder()
                        .cost(validMoney())
                        .expectedDate(null)
                        .recipient(validRecipient())
                        .address(validAddress())
                        .build()
        );
    }

    @Test
    void shouldThrowWhenRecipientIsNull() {
        assertThrows(NullPointerException.class, () ->
                Shipping.builder()
                        .cost(validMoney())
                        .expectedDate(LocalDate.now())
                        .recipient(null)
                        .address(validAddress())
                        .build()
        );
    }

    @Test
    void shouldThrowWhenAddressIsNull() {
        assertThrows(NullPointerException.class, () ->
                Shipping.builder()
                        .cost(validMoney())
                        .expectedDate(LocalDate.now())
                        .recipient(validRecipient())
                        .address(null)
                        .build()
        );
    }

    @Test
    void shouldCreateCopyUsingToBuilder() {
        Shipping original = Shipping.builder()
                .cost(validMoney())
                .expectedDate(LocalDate.now().plusDays(3))
                .recipient(validRecipient())
                .address(validAddress())
                .build();

        Shipping copy = original.toBuilder()
                .cost(new Money("30"))
                .build();

        assertNotNull(copy);
        assertEquals(new Money("30"), copy.cost());
        assertEquals(original.recipient(), copy.recipient());
        assertEquals(original.address(), copy.address());
    }

}