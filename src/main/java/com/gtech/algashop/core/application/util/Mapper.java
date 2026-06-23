package com.gtech.algashop.core.application.util;

public interface Mapper {
    <T> T convert(Object object, Class<T> destinationType);
}
