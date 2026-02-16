package com.gtech.algashop.domain.entity.VO;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class BillingTest {

    private FullName validFullName() {
        return new FullName("Gabriel", "Lima");
    }

    private Document validDocument() {
        return new Document("12345678900");
    }

    private Phone validPhone() {
        return new Phone("61999999999");
    }

    private Email validEmail() {
        return new Email("johndoe@gmail.com");
    }

    private Address validAddress() {
        return Address.builder()
                .street("Street A")
                .neighborhood("Center")
                .number("123")
                .city("Brasilia")
                .state("DF")
                .zipCode(new ZipCode("12345"))
                .build();
    }

    @Test
    void shouldCreateValidBillingInfo() {
        Billing billing = Billing.builder()
                .fullName(validFullName())
                .document(validDocument())
                .phone(validPhone())
                .address(validAddress())
                .email(validEmail())
                .build();

        assertThat(billing.fullName()).isNotNull();
        assertThat(billing.document()).isNotNull();
        assertThat(billing.phone()).isNotNull();
        assertThat(billing.address()).isNotNull();
        assertThat(billing.email()).isNotNull();
    }

    @Test
    void shouldNotAllowNullFullName() {
        assertThatThrownBy(() ->
                Billing.builder()
                        .fullName(null)
                        .document(validDocument())
                        .phone(validPhone())
                        .address(validAddress())
                        .email(validEmail())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNullDocument() {
        assertThatThrownBy(() ->
                Billing.builder()
                        .fullName(validFullName())
                        .document(null)
                        .phone(validPhone())
                        .address(validAddress())
                        .email(validEmail())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNullPhone() {
        assertThatThrownBy(() ->
                Billing.builder()
                        .fullName(validFullName())
                        .document(validDocument())
                        .phone(null)
                        .address(validAddress())
                        .email(validEmail())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNullAddress() {
        assertThatThrownBy(() ->
                Billing.builder()
                        .fullName(validFullName())
                        .document(validDocument())
                        .phone(validPhone())
                        .address(null)
                        .email(validEmail())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNullEmail() {
        assertThatThrownBy(() ->
                Billing.builder()
                        .fullName(validFullName())
                        .document(validDocument())
                        .phone(validPhone())
                        .address(validAddress())
                        .email(null)
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Billing b1 = Billing.builder()
                .fullName(validFullName())
                .document(validDocument())
                .phone(validPhone())
                .address(validAddress())
                .email(validEmail())
                .build();

        Billing b2 = Billing.builder()
                .fullName(validFullName())
                .document(validDocument())
                .phone(validPhone())
                .address(validAddress())
                .email(validEmail())
                .build();

        assertThat(b1).isEqualTo(b2);
    }

}