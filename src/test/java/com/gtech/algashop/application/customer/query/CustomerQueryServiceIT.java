package com.gtech.algashop.application.customer.query;

import com.gtech.algashop.application.commons.AddressData;
import com.gtech.algashop.application.order.query.OrderDetailOutput;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.Customers;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.order.Order;
import com.gtech.algashop.domain.model.order.OrderTestDataBuilder;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@SpringBootTest
@Transactional
class CustomerQueryServiceIT {

    @Autowired
    private CustomerQueryService customerQueryService;

    @Autowired
    private Customers customers;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    @Test
    void shouldFindById() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        customers.add(customer);

        CustomerOutput output = customerQueryService.findById(customer.id().value());

        Assertions.assertThat(output)
                .extracting(
                        CustomerOutput::getId,
                        CustomerOutput::getFirstName,
                        CustomerOutput::getLastName,
                        CustomerOutput::getEmail,
                        CustomerOutput::getPhone,
                        CustomerOutput::getDocument,
                        CustomerOutput::getBirthDate,
                        CustomerOutput::getPromotionNotificationsAllowed,
                        CustomerOutput::getLoyaltyPoints,
                        CustomerOutput::getRegisteredAt,
                        CustomerOutput::getArchivedAt,
                        CustomerOutput::getArchived
                ).containsExactly(
                        customer.id().value(),
                        customer.fullName().firstName(),
                        customer.fullName().lastName(),
                        customer.email().email(),
                        customer.phone().phone(),
                        customer.document().document(),
                        customer.birthDate().birthDate(),
                        customer.isPromotionNotificationsAllowed(),
                        customer.loyaltyPoints().point(),
                        customer.registeredAt(),
                        customer.archivedAt(),
                        customer.isArchived()
                );
    }
}