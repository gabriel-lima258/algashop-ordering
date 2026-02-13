package com.gtech.algashop.domain.entity.VO;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PhoneTest {
    @Test
    void shouldCreateValidPhone() {
        Phone phone = new Phone("478-256-2504");
        Assertions.assertThat(phone.phone()).isEqualTo("478-256-2504");
    }

    @Test
    void shouldNotAllowNullPhone() {
        Assertions.assertThatThrownBy(() -> new Phone(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowBlankPhone() {
        Assertions.assertThatThrownBy(() -> new Phone("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnPhoneAsString() {
        Phone phone = new Phone("478-256-2504");
        Assertions.assertThat(phone.toString()).hasToString("478-256-2504");
    }
}