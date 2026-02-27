package com.gtech.algashop.domain.model.VO;

import com.gtech.algashop.domain.model.commons.Address;
import com.gtech.algashop.domain.model.commons.ZipCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class AddressTest {

    @Test
    void shouldCreateValidAddress() {
        Address address = Address.builder()
                .street("Street A")
                .complement("Apt 1")
                .neighborhood("Center")
                .number("123")
                .city("Brasilia")
                .state("DF")
                .zipCode(new ZipCode("12345"))
                .build();

        assertThat(address.street()).isEqualTo("Street A");
    }

    @Test
    void shouldAllowNullComplement() {
        Address address = Address.builder()
                .street("Street A")
                .neighborhood("Center")
                .number("123")
                .city("Brasilia")
                .state("DF")
                .zipCode(new ZipCode("12345"))
                .build();

        assertThat(address.complement()).isNull();
    }

    @Test
    void shouldNotAllowNullStreet() {
        assertThatThrownBy(() ->
                Address.builder()
                        .street(null)
                        .neighborhood("Center")
                        .number("123")
                        .city("Brasilia")
                        .state("DF")
                        .zipCode(new ZipCode("12345"))
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowBlankStreet() {
        assertThatThrownBy(() ->
                Address.builder()
                        .street("   ")
                        .neighborhood("Center")
                        .number("123")
                        .city("Brasilia")
                        .state("DF")
                        .zipCode(new ZipCode("12345"))
                        .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotAllowNullNeighborhood() {
        assertThatThrownBy(() ->
                Address.builder()
                        .street("Street A")
                        .neighborhood(null)
                        .number("123")
                        .city("Brasilia")
                        .state("DF")
                        .zipCode(new ZipCode("12345"))
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowBlankNumber() {
        assertThatThrownBy(() ->
                Address.builder()
                        .street("Street A")
                        .neighborhood("Center")
                        .number("   ")
                        .city("Brasilia")
                        .state("DF")
                        .zipCode(new ZipCode("12345"))
                        .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotAllowNullCity() {
        assertThatThrownBy(() ->
                Address.builder()
                        .street("Street A")
                        .neighborhood("Center")
                        .number("123")
                        .city(null)
                        .state("DF")
                        .zipCode(new ZipCode("12345"))
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowBlankState() {
        assertThatThrownBy(() ->
                Address.builder()
                        .street("Street A")
                        .neighborhood("Center")
                        .number("123")
                        .city("Brasilia")
                        .state("   ")
                        .zipCode(new ZipCode("12345"))
                        .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotAllowNullZipCode() {
        assertThatThrownBy(() ->
                Address.builder()
                        .street("Street A")
                        .neighborhood("Center")
                        .number("123")
                        .city("Brasilia")
                        .state("DF")
                        .zipCode(null)
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldUseToBuilderToModifyAddress() {
        Address original = Address.builder()
                .street("Street A")
                .neighborhood("Center")
                .number("123")
                .city("Brasilia")
                .state("DF")
                .zipCode(new ZipCode("12345"))
                .build();

        Address modified = original.toBuilder()
                .number("999")
                .build();

        assertThat(original.number()).isEqualTo("123");
        assertThat(modified.number()).isEqualTo("999");
    }

    @Test
    void shouldBeEqualWhenSameValues() {
        Address a1 = Address.builder()
                .street("Street A")
                .neighborhood("Center")
                .number("123")
                .city("Brasilia")
                .state("DF")
                .zipCode(new ZipCode("12345"))
                .build();

        Address a2 = Address.builder()
                .street("Street A")
                .neighborhood("Center")
                .number("123")
                .city("Brasilia")
                .state("DF")
                .zipCode(new ZipCode("12345"))
                .build();

        assertThat(a1).isEqualTo(a2);
    }

}