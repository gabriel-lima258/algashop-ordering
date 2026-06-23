package com.gtech.algashop.core.domain.model.valueobject;


import com.gtech.algashop.core.domain.model.commons.Email;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void shouldCreateValidEmail() {
        Email email = new Email("test@email.com");
        Assertions.assertThat(email.email()).isEqualTo("test@email.com");
    }

    @Test
    void shouldNotAllowNullEmail() {
        Assertions.assertThatThrownBy(() -> new Email(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowBlankEmail() {
        Assertions.assertThatThrownBy(() -> new Email("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldNotAllowInvalidEmail() {
        Assertions.assertThatThrownBy(() -> new Email("invalid-email"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnEmailAsString() {
        Email email = new Email("test@email.com");
        Assertions.assertThat(email.toString()).hasToString("test@email.com");
    }

}