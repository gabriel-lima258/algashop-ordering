package com.gtech.algashop.core.application.order.event;

import com.gtech.algashop.core.application.notification.command.SendEmailIntegrationCommand;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderItem;
import com.gtech.algashop.core.domain.model.order.OrderStatus;
import com.gtech.algashop.core.domain.model.order.OrderTestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Unitario puro (sem Spring nem banco): as chaves do mapa sao o contrato com o template
// Order Placed do notifications, entao o teste fixa nome, tipo e ordem de cada variavel.
class OrderPlacedEmailCommandAssemblerTest {

    private final OrderPlacedEmailCommandAssembler assembler = new OrderPlacedEmailCommandAssembler();
    private final UUID templateId = UUID.fromString("0198d0a1-2c44-7d0b-8e11-9a0b7c5d3f21");

    @Test
    void shouldAssembleCommandFromOrderAndCustomer() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        Order order = OrderTestDataBuilder.anOrder().customerId(customer.id()).status(OrderStatus.PLACED).build();

        SendEmailIntegrationCommand command = assembler.toCommand(order, customer, templateId);

        Assertions.assertThat(command.getAggregateId()).isEqualTo(order.id().toString());
        Assertions.assertThat(command.getTemplateId()).isEqualTo(templateId);
        Assertions.assertThat(command.getRecipient().getEmail()).isEqualTo(customer.email().email());
        Assertions.assertThat(command.getRecipient().getName()).isEqualTo("John Doe");

        Map<String, Object> variables = command.getVariables();
        Assertions.assertThat(variables).containsOnlyKeys(
                "orderId", "placedAt", "totalAmount", "shippingCost", "expectedDeliveryDate", "items");
        Assertions.assertThat(variables.get("orderId")).isEqualTo(order.id().toString());
        Assertions.assertThat(variables.get("placedAt")).isEqualTo(order.placedAt().toString());
        Assertions.assertThat(variables.get("totalAmount")).isEqualTo(order.totalAmount().money());
        Assertions.assertThat(variables.get("shippingCost")).isEqualTo(order.shipping().cost().money());
        Assertions.assertThat(variables.get("expectedDeliveryDate"))
                .isEqualTo(order.shipping().expectedDate().toString());
    }

    @Test
    void shouldAssembleItemsSortedById() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        Order order = OrderTestDataBuilder.anOrder().customerId(customer.id()).status(OrderStatus.PLACED).build();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) assembler
                .toCommand(order, customer, templateId).getVariables().get("items");

        List<OrderItem> expected = order.items().stream()
                .sorted(Comparator.comparing(item -> item.id().toString()))
                .toList();
        Assertions.assertThat(items).hasSize(expected.size());
        for (int i = 0; i < expected.size(); i++) {
            OrderItem item = expected.get(i);
            Assertions.assertThat(items.get(i)).containsExactly(
                    Map.entry("productName", item.productName().name()),
                    Map.entry("quantity", item.quantity().quantity()),
                    Map.entry("price", item.price().money()),
                    Map.entry("totalAmount", item.totalAmount().money()));
        }
    }

    @Test
    void shouldRejectNullArguments() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        Order order = OrderTestDataBuilder.anOrder().customerId(customer.id()).status(OrderStatus.PLACED).build();

        Assertions.assertThatNullPointerException().isThrownBy(() -> assembler.toCommand(null, customer, templateId));
        Assertions.assertThatNullPointerException().isThrownBy(() -> assembler.toCommand(order, null, templateId));
        Assertions.assertThatNullPointerException().isThrownBy(() -> assembler.toCommand(order, customer, null));
    }
}
