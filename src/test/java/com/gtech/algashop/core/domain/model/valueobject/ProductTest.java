package com.gtech.algashop.core.domain.model.valueobject;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.product.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    private ProductName validName() {
        return new ProductName("Pizza");
    }

    private Money validPrice() {
        return new Money("100.00");
    }

    // ✅ Caso válido
    @Test
    void shouldCreateProductWhenAllFieldsAreValid() {
        Product product = Product.builder()
                .id(new ProductId())
                .productName(validName())
                .price(validPrice())
                .inStock(true)
                .build();

        assertNotNull(product);
        assertThat(product.id()).isNotNull();
        assertEquals(validName(), product.productName());
        assertEquals(validPrice(), product.price());
        assertTrue(product.inStock());
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThatThrownBy(() ->
                Product.builder()
                        .id(null)
                        .productName(validName())
                        .price(validPrice())
                        .inStock(true)
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenProductNameIsNull() {
        assertThatThrownBy(() ->
                Product.builder()
                        .id(new ProductId())
                        .productName(null)
                        .price(validPrice())
                        .inStock(true)
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenPriceIsNull() {
        assertThatThrownBy(() ->
                Product.builder()
                        .id(new ProductId())
                        .productName(validName())
                        .price(null)
                        .inStock(true)
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowWhenInStockIsNull() {
        assertThatThrownBy(() ->
                Product.builder()
                        .id(new ProductId())
                        .productName(validName())
                        .price(validPrice())
                        .inStock(null)
                        .build()
        ).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldNotThrowWhenProductIsInStock() {
        Product product = ProductTestDataBuilder.aProduct().build();

        assertDoesNotThrow(product::checkOutOfStock);
    }

    @Test
    void shouldThrowWhenProductIsOutOfStock() {
        Product product = ProductTestDataBuilder.aProductUnavailable().build();

        assertThrows(ProductOutOfStockException.class, product::checkOutOfStock);
    }
}