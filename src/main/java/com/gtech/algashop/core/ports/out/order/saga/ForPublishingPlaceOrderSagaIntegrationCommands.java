package com.gtech.algashop.core.ports.out.order.saga;

import com.gtech.algashop.core.application.OutboundIntegrationCommand;

// Porta de saida dos comandos da saga. O coordenador so diz QUAL comando e de QUAL saga;
// topico, topico de resposta e transporte ficam com o adaptador. O correlationId e o
// sagaId: e ele que volta na resposta e diz ao orquestrador a que saga ela pertence.
public interface ForPublishingPlaceOrderSagaIntegrationCommands {
    void send(String correlationId, OutboundIntegrationCommand command);
}
