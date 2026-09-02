package com.gtech.algashop.core.application.product.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

// Copia local do contrato publicado pelo product-catalog. O acoplamento entre os
// servicos e pela FORMA do JSON e pelo nome logico no header __TypeId__, nunca por
// classe compartilhada - por isso a copia manual (sem contract testing na mensageria
// por enquanto). Este evento NAO esta no type.mapping do consumidor DE PROPOSITO:
// chega como ObjectNode e cai no handler default do listener - o exemplo vivo de
// como evento nao mapeado degrada para log em vez de quebrar o consumidor.
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAddedIntegrationEvent {
    private UUID productId;
    private OffsetDateTime addedAt;
}
