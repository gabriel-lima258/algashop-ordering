package com.gtech.algashop.core.application.checkout.command;

import com.gtech.algashop.core.application.IntegrationCommand;
import com.gtech.algashop.core.application.order.snapshot.OrderSnapshot;
import com.gtech.algashop.core.domain.model.IdGenerator;
import lombok.*;

import java.util.UUID;

// COMANDO (nao evento) do listen to yourself: imperativo, tem UM destinatario (o
// proprio ordering) e uma expectativa de execucao - "processe este checkout aceito".
// Evento conta um fato consumado; comando pede um trabalho. O checkout nao grava a
// Order: publica este comando (via outbox, na mesma transacao do aceite) e a propria
// aplicacao o consome depois para persistir o pedido - o dual-write morre porque o
// unico write sincrono e o do banco (outbox), e o Kafka vem do dispatcher.
// Carrega o OrderSnapshot: copia serializavel do Order completo, o bastante para o
// consumidor reconstruir o agregado sem recalcular frete, precos ou estoque.

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessAcceptedCheckoutIntegrationCommand implements IntegrationCommand {

    // @Builder.Default: sem ele o Lombok IGNORA o inicializador no builder e um
    // .builder().build() sairia com idempotencyKey nulo - evento sem header de dedupe,
    // em silencio (o publisher so grava o header quando a chave nao e nula)
    @Builder.Default
    private UUID idempotencyKey = IdGenerator.generateTimeBasedUUID();
    private OrderSnapshot order;

    public ProcessAcceptedCheckoutIntegrationCommand(OrderSnapshot order) {
        this.order = order;
    }

    @Override
    public String getAggregateId() {
        if (order.orderId() == null) {
            return null;
        }

        return order.orderId();
    }

    @Override
    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }
}
