package com.gtech.algashop.infrastructure.beans;

import com.gtech.algashop.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.domain.model.order.CustomerHaveFreeShippingSpecification;
import com.gtech.algashop.domain.model.order.Orders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpecificationsBeansConfig {

    @Bean
    public CustomerHaveFreeShippingSpecification customerHaveFreeShippingSpecification(Orders orders) {
        // instancia os valores padrão condicional de free shipping, podemos tambem deixar valor fixo em properties
        return new CustomerHaveFreeShippingSpecification(
                orders,
                new LoyaltyPoints(200),
                2L,
                new LoyaltyPoints(2000)
        );
    }
}
