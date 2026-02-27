package com.gtech.algashop.domain.model.valueobject;

import com.gtech.algashop.domain.model.commons.ZipCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ZipCodeTest {

    @Test
    void shouldThrowNullPointerExceptionWhenZipCodeIsNull() {
        assertThrows(NullPointerException.class, () -> new ZipCode(null));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenZipCodeIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> new ZipCode(""));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenZipCodeIsOnlySpaces() {
        assertThrows(IllegalArgumentException.class, () -> new ZipCode("     "));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenZipCodeHasLessThanFiveCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new ZipCode("1234"));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenZipCodeHasMoreThanFiveCharacters() {
        assertThrows(IllegalArgumentException.class, () -> new ZipCode("123456"));
    }

    @Test
    void shouldCreateZipCodeWhenValid() {
        ZipCode zipCode = new ZipCode("12345");

        assertNotNull(zipCode);
        assertEquals("12345", zipCode.toString());
    }

}