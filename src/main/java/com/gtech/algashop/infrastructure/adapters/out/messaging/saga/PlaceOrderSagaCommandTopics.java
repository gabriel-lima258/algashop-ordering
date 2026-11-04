package com.gtech.algashop.infrastructure.adapters.out.messaging.saga;

import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import com.gtech.algashop.core.application.invoice.command.IssueInvoiceIntegrationCommand;
import com.gtech.algashop.infrastructure.config.kafka.AlgaShopMessagingKafkaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaceOrderSagaCommandTopics {

    private final AlgaShopMessagingKafkaProperties properties;

    // Roteia cada comando para o topico do participante que o executa (pattern matching
    // por tipo). Comando sem rota falha alto: melhor um erro no envio que um comando
    // parado num topico que ninguem consome.
    public String of(OutboundIntegrationCommand command) {
        return switch (command) {
            case IssueInvoiceIntegrationCommand ignored -> properties.getBillingInvoiceCommandsTopicName();
            default -> throw new IllegalArgumentException("Unsupported saga command " + command.getClass().getSimpleName());
        };
    }
}
