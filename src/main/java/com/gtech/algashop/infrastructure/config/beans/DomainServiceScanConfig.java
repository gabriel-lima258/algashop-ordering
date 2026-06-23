package com.gtech.algashop.infrastructure.config.beans;

import com.gtech.algashop.core.domain.model.DomainService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

// essa classe faz o Spring registrar beans sem poluir o domínio com @Service
@Configuration
@ComponentScan(
        basePackages = "com.gtech.algashop.core.domain.model",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ANNOTATION,
                classes = DomainService.class
        )
)
public class DomainServiceScanConfig {
}
