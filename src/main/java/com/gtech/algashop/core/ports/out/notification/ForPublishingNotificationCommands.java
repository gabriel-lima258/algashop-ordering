package com.gtech.algashop.core.ports.out.notification;

import com.gtech.algashop.core.application.OutboundIntegrationCommand;

// Porta de saida dos comandos para o notifications. O core so entrega o comando; topico,
// outbox ou Kafka direto ficam com o adaptador. Sem retorno: ninguem espera resposta.
public interface ForPublishingNotificationCommands {
    void send(OutboundIntegrationCommand command);
}
