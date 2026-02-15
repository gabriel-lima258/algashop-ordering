package com.gtech.algashop.domain.entity;

import com.gtech.algashop.domain.entity.VO.Quantity;
import com.gtech.algashop.domain.entity.VO.id.OrderId;
import com.gtech.algashop.domain.entity.factory.ProductTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void shouldGenerate() {
        OrderItem orderItem = OrderItem.brandNew()
                .product(ProductTestDataBuilder.aProduct().build())
                .quantity(new Quantity(1))
                .orderId(new OrderId())
                .build();

        Assertions.assertThat(orderItem).isNotNull();
    }

}