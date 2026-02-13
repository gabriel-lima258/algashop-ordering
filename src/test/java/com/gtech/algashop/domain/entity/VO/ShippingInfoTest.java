package com.gtech.algashop.domain.entity.VO;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class ShippingInfoTest {

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
    void shouldCreateValidShippingInfo() {
        ShippingInfo shippingInfo = ShippingInfo.builder()
                .fullName(validFullName())
                .document(validDocument())
                .phone(validPhone())
                .address(validAddress())
                .build();

        assertThat(shippingInfo.fullName()).isNotNull();
        assertThat(shippingInfo.document()).isNotNull();
        assertThat(shippingInfo.phone()).isNotNull();
        assertThat(shippingInfo.address()).isNotNull();
    }

    @Test
    void shouldNotAllowNullFullName() {
        assertThatThrownBy(() ->
                ShippingInfo.builder()
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
                ShippingInfo.builder()
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
                ShippingInfo.builder()
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
                ShippingInfo.builder()
                        .fullName(validFullName())
                        .document(validDocument())
                        .phone(validPhone())
                        .address(null)
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        ShippingInfo b1 = ShippingInfo.builder()
                .fullName(validFullName())
                .document(validDocument())
                .phone(validPhone())
                .address(validAddress())
                .build();

        ShippingInfo b2 = ShippingInfo.builder()
                .fullName(validFullName())
                .document(validDocument())
                .phone(validPhone())
                .address(validAddress())
                .build();

        assertThat(b1).isEqualTo(b2);
    }

}