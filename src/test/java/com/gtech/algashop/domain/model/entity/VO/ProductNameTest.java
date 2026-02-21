package com.gtech.algashop.domain.model.entity.VO;

import com.gtech.algashop.domain.model.entity.VO.ProductName;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ProductNameTest {

    @Test
    void shouldCreateValidProductName() {
        ProductName productName = new ProductName("Iphone max");
        Assertions.assertThat(productName.name()).isEqualTo("Iphone max");
    }

    @Test
    void shouldNotAllowNullProductName() {
        Assertions.assertThatThrownBy(() -> new ProductName(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotAllowBlankProductName() {
        Assertions.assertThatThrownBy(() -> new ProductName("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldReturnProductNameAsString() {
        ProductName productName = new ProductName("Iphone max");
        Assertions.assertThat(productName.toString()).hasToString("Iphone max");
    }
}