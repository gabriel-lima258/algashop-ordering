package com.gtech.algashop.domain.model.customer;


import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.CustomerLoyaltyPointsService;
import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.order.OrderStatus;
import com.gtech.algashop.domain.model.costumer.LoyaltyPoints;
import com.gtech.algashop.domain.model.product.Product;
import com.gtech.algashop.domain.model.commons.Quantity;
import com.gtech.algashop.domain.model.order.OrderTestDataBuilder;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class CustomerLoyaltyPointsServiceIT {

    @Autowired
    private CustomerLoyaltyPointsService customerLoyaltyPointsService;

    @MockitoBean
    private ProductCatalogService productCatalogService;

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