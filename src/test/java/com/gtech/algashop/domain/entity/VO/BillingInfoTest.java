package com.gtech.algashop.domain.entity.VO;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class BillingInfoTest {

    private FullName validFullName() {
        return new FullName("Gabriel", "Lima");
    }

    private Document validDocument() {
        return new Document("12345678900");
    }

    private Phone validPhone() {
        return new Phone("61999999999");
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
        BillingInfo billingInfo = BillingInfo.builder()
                .fullName(validFullName())
                .document(validDocument())
                .phone(validPhone())
                .address(validAddress())
                .build();

        assertThat(billingInfo.fullName()).isNotNull();
        assertThat(billingInfo.document()).isNotNull();
        assertThat(billingInfo.phone()).isNotNull();
        assertThat(billingInfo.address()).isNotNull();
    }

    @Test
    void shouldNotAllowNullFullName() {
        assertThatThrownBy(() ->
                BillingInfo.builder()
                        .fullName(null)
                        .document(validDocument())
                        .phone(validPhone())
                        .address(validAddress())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNullDocument() {
        assertThatThrownBy(() ->
                BillingInfo.builder()
                        .fullName(validFullName())
                        .document(null)
                        .phone(validPhone())
                        .address(validAddress())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNullPhone() {
        assertThatThrownBy(() ->
                BillingInfo.builder()
                        .fullName(validFullName())
                        .document(validDocument())
                        .phone(null)
                        .address(validAddress())
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNullAddress() {
        assertThatThrownBy(() ->
                BillingInfo.builder()
                        .fullName(validFullName())
                        .document(validDocument())
                        .phone(validPhone())
                        .address(null)
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        BillingInfo b1 = BillingInfo.builder()
                .fullName(validFullName())
                .document(validDocument())
                .phone(validPhone())
                .address(validAddress())
                .build();

        BillingInfo b2 = BillingInfo.builder()
                .fullName(validFullName())
                .document(validDocument())
                .phone(validPhone())
                .address(validAddress())
                .build();

        assertThat(b1).isEqualTo(b2);
    }

}