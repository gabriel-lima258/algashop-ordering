package com.gtech.algashop.core.application.order.event;

import com.gtech.algashop.core.application.notification.command.SendEmailIntegrationCommand;
import com.gtech.algashop.core.domain.model.costumer.Customer;
import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// Monta o SendEmail do pedido realizado. As chaves do mapa sao o contrato com o template
// Order Placed do notifications: renomear uma delas quebra o e-mail sem erro de compilacao.
// Datas vao como texto ISO-8601 (o template recebe JSON puro e formata la). Os itens saem
// ordenados pelo id para o e-mail ser sempre igual para o mesmo pedido (o Set nao tem ordem).
// O aggregateId e o id do pedido: vira a chave do registro no Kafka e nao vai no corpo.
@Component
public class OrderPlacedEmailCommandAssembler {

    public SendEmailIntegrationCommand toCommand(Order order, Customer customer, UUID templateId) {
        Objects.requireNonNull(order);
        Objects.requireNonNull(customer);
        Objects.requireNonNull(templateId);

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("orderId", order.id().toString());
        variables.put("placedAt", order.placedAt().toString());
        variables.put("totalAmount", order.totalAmount().money());
        variables.put("shippingCost", order.shipping().cost().money());
        variables.put("expectedDeliveryDate", order.shipping().expectedDate().toString());
        variables.put("items", order.items().stream()
                .sorted(Comparator.comparing(item -> item.id().toString()))
                .map(this::toItemVariables)
                .toList());

        return SendEmailIntegrationCommand.builder()
                .aggregateId(order.id().toString())
                .recipient(SendEmailIntegrationCommand.Recipient.builder()
                        .email(customer.email().email())
                        .name(customer.fullName().toString())
                        .build())
                .templateId(templateId)
                .variables(variables)
                .build();
    }

    private Map<String, Object> toItemVariables(OrderItem item) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("productName", item.productName().name());
        variables.put("quantity", item.quantity().quantity());
        variables.put("price", item.price().money());
        variables.put("totalAmount", item.totalAmount().money());
        return variables;
    }
}
