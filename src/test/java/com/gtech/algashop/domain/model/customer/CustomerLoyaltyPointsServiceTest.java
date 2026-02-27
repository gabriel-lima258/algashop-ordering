package com.gtech.algashop.domain.model.service;


import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.CustomerLoyaltyPointsService;
import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.order.OrderStatus;
import com.gtech.algashop.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.entity.factory.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.entity.factory.OrderTestDataBuilder;
import com.gtech.algashop.domain.model.entity.factory.ProductTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CustomerLoyaltyPointsServiceTest {

    CustomerLoyaltyPointsService customerLoyaltyPointsService =
            new CustomerLoyaltyPointsService();

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