package com.gtech.algashop.domain.entity.VO;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FullNameTest {

    @Test
    void shouldCreateAFullName() {
        FullName fullName = new FullName("John", "Doe");
        Assertions.assertThat(fullName.firstName()).isEqualTo("John");
        Assertions.assertThat(fullName.lastName()).isEqualTo("Doe");
    }

    @Test
    void shouldTrimNames() {
        FullName fullName = new FullName("  Gabriel  ", "  Lima  ");

        Assertions.assertThat(fullName.firstName()).isEqualTo("Gabriel");
        Assertions.assertThat(fullName.lastName()).isEqualTo("Lima");
    }


    @Test
    void shouldNotAllowNullFirstName() {
        Assertions.assertThatThrownBy(() -> new FullName(null, "Lima"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowNullLastName() {
        Assertions.assertThatThrownBy(() -> new FullName("John", null))
                .isInstanceOf(NullPointerException.class);
    }



    @Test
    void givenFirstNameBlankShouldThrowException() {
        Assertions.assertThatThrownBy(() -> new FullName("   ", "Doe"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void givenLastNameBlankShouldThrowException() {
        Assertions.assertThatThrownBy(() -> new FullName("John", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFormatToStringCorrectly() {
        FullName fullName = new FullName("John", "Lima");

        Assertions.assertThat(fullName.toString()).isEqualTo("John Lima");
    }

}