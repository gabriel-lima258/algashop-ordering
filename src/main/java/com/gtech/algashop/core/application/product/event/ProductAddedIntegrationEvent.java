package com.gtech.algashop.core.application.product.event;

import com.gtech.algashop.core.application.IntegrationEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

// Copia local do contrato publicado pelo product-catalog - event notification: so o
// fato e o id. O acoplamento entre os servicos e pela FORMA do JSON e pelo nome logico
// no header __TypeId__, nunca por classe compartilhada. Este evento NAO esta no
// type.mapping do consumidor DE PROPOSITO: chega como ObjectNode e cai no handler
// default do listener - o exemplo vivo de que evento nao mapeado degrada para log em
// vez de quebrar o consumidor (e por isso o getAggregateId daqui nunca chega a rodar).

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductAddedIntegrationEvent implements IntegrationEvent {
    private UUID productId;
    private OffsetDateTime addedAt;

    @Override
    public String getAggregateId() {
        if (productId == null) {
            return null;
        }

        return productId.toString();
    }
}
