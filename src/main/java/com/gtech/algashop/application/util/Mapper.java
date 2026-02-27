package com.gtech.algashop.application.util;

public interface Mapper {
    <T> T convert(Object object, Class<T> destinationType);
}
