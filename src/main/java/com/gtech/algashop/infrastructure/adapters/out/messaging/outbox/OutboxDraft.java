package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import lombok.Builder;

// O rascunho que o publisher entrega ao OutboxRecorder: diz O QUE enviar e PARA ONDE.
// O resto (id da linha, __TypeId__, JSON, tentativas) e decisao do recorder.
// Evento preenche channelName, aggregateId e payload; comando da saga preenche tambem
// correlationId e replyTopic, que viram os headers kafka_correlationId e kafka_replyTopic.
@Builder
public record OutboxDraft(String channelName, String aggregateId,
                          String correlationId, String replyTopic,
                          Object payload) {
}
