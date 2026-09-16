package com.gtech.algashop.core.ports.out.idempotency;

import java.util.UUID;

// Porta da idempotencia de NEGOCIO (a do producer Kafka dedupe so o transporte):
// executa o work NO MAXIMO UMA VEZ por chave. true = executou; false = chave ja
// processada, work nao rodou. O chamador decide o que fazer com a duplicata - em
// geral, nada: skip silencioso com log e o comportamento certo para redelivery.
public interface ForGuardingIdempotency {
    boolean runOnce(UUID idempotencyKey, Runnable work);
}
