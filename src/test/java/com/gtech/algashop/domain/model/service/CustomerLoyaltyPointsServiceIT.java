package com.gtech.algashop.domain.model.service;


import com.gtech.algashop.domain.model.entity.Customer;
import com.gtech.algashop.domain.model.entity.Order;
import com.gtech.algashop.domain.model.entity.OrderStatus;
import com.gtech.algashop.domain.model.entity.VO.LoyaltyPoints;
import com.gtech.algashop.domain.model.entity.VO.Product;
import com.gtech.algashop.domain.model.entity.VO.Quantity;
import com.gtech.algashop.domain.model.entity.factory.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.entity.factory.OrderTestDataBuilder;
import com.gtech.algashop.domain.model.entity.factory.ProductTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CustomerLoyaltyPointsServiceIT {

    @Autowired
    private CustomerLoyaltyPointsService customerLoyaltyPointsService;

    @Test
    void givenValidCustomerAndOrderWhenAddingPointsShouldAccumulate() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();

        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.READY).build();

        customerLoyaltyPointsService.addPoints(customer, order);

        // cada 1000 reais ganha 5 pontos
        Assertions.assertThat(customer.loyaltyPoints()).isEqualTo(new LoyaltyPoints(20));
    }

    @Test
    void shouldValidCustomerAndOrderLowTotalAmountWhenAddingPointsShouldNotAccumulate() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        Order order = OrderTestDataBuilder.anOrder().withItems(false).status(OrderStatus.DRAFT).build();

        Product cheaperProduct = ProductTestDataBuilder.aProductRamMemory().build();
        order.addItem(cheaperProduct, new Quantity(1));
        order.markAsPlaced();
        order.markAsPaid();
        order.markAsReady();

        customerLoyaltyPointsService.addPoints(customer, order);

        Assertions.assertThat(customer.loyaltyPoints()).isEqualTo(new LoyaltyPoints(0));
    }

}