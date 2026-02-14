package com.gtech.algashop.domain.entity.VO;

import com.gtech.algashop.domain.entity.VO.id.CustomerId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class CustomerIdTest {

    @Test
    void shouldGenerateId() {
        CustomerId customerId = new CustomerId();
        Assertions.assertThat(customerId.value()).isNotNull();
    }

    @Test
    void shouldGenerateDifferentIds() {
        CustomerId id1 = new CustomerId();
        CustomerId id2 = new CustomerId();

        Assertions.assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    void shouldCreateWithProvidedUuid() {
        UUID uuid = UUID.randomUUID();

        CustomerId customerId = new CustomerId(uuid);

        Assertions.assertThat(customerId.value()).isEqualTo(uuid);
    }

    @Test
    void shouldNotAllowNullUuid() {
        Assertions.assertThatThrownBy(() -> new CustomerId(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldReturnUuidAsString() {
        UUID uuid = UUID.randomUUID();
        CustomerId customerId = new CustomerId(uuid);

        Assertions.assertThat(customerId.toString())
                .isEqualTo(uuid.toString());
    }

    @Test
    void shouldBeEqualWhenSameUuid() {
        UUID uuid = UUID.randomUUID();

        CustomerId id1 = new CustomerId(uuid);
        CustomerId id2 = new CustomerId(uuid);

        Assertions.assertThat(id1).isEqualTo(id2);
    }




}