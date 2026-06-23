package com.gtech.algashop.core.domain.model;

import java.lang.annotation.*;

// annotation marker, não tem logica apenas indica que é uma annotation de domain
// isso evita sujar o domain com annotations do spring, pratica pura DDD
@Target(ElementType.TYPE) // usada em classes
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DomainService {
}
