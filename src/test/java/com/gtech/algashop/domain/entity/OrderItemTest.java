package com.gtech.algashop.domain.entity;

import com.gtech.algashop.domain.entity.VO.Money;
import com.gtech.algashop.domain.entity.VO.ProductName;
import com.gtech.algashop.domain.entity.VO.Quantity;
import com.gtech.algashop.domain.entity.VO.id.OrderId;
import com.gtech.algashop.domain.entity.VO.id.ProductId;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void shouldGenerate() {
        OrderItem orderItem = OrderItem.brandNew()
                .productId(new ProductId())
                .quantity(new Quantity(1))
                .orderId(new OrderId())
                .productName(new ProductName("Keyboard"))
                .price(new Money("200"))
                .build();

        Assertions.assertThat(orderItem).isNotNull();
    }

}