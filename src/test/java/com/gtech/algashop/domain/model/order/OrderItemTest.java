package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void shouldGenerateNewOrderItem() {
        Product product = ProductTestDataBuilder.aProduct().build();
        Quantity quantity = new Quantity(1);
        OrderId orderId = new OrderId();

        OrderItem orderItem = OrderItem.brandNew()
                .product(product)
                .quantity(quantity)
                .orderId(orderId)
                .build();

        Assertions.assertWith(orderItem,
                o -> Assertions.assertThat(o.id()).isNotNull(),
                o -> Assertions.assertThat(o.productId()).isEqualTo(product.id()),
                o -> Assertions.assertThat(o.productName()).isEqualTo(product.productName()),
                o -> Assertions.assertThat(o.price()).isEqualTo(product.price()),
                o -> Assertions.assertThat(o.quantity()).isEqualTo(quantity),
                o -> Assertions.assertThat(o.orderId()).isEqualTo(orderId)
                );
    }

}