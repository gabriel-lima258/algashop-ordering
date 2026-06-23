package com.gtech.algashop.core.domain.model.customer;


import com.gtech.algashop.core.domain.model.AbstractDomainIT;
import com.gtech.algashop.core.domain.model.commons.Quantity;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.costumer.CustomerLoyaltyPointsService;
import com.gtech.algashop.core.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderStatus;
import com.gtech.algashop.core.domain.model.order.OrderTestDataBuilder;
import com.gtech.algashop.core.domain.model.product.Product;
import com.gtech.algashop.core.domain.model.product.ProductTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CustomerLoyaltyPointsServiceIT extends AbstractDomainIT {

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