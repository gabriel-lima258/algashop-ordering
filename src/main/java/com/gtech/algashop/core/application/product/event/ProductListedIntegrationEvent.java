package com.gtech.algashop.core.application.product.event;

import com.gtech.algashop.core.application.IntegrationEvent;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

// Copia local do contrato do product-catalog para "produto voltou a vitrine" - event
// NOTIFICATION: o evento so avisa o fato e o id; o efeito (disponibilidade do item nos
// carrinhos) e decisao do consumidor. O que casa este POJO com o JSON publicado e a
// forma dos campos + o nome logico ProductCatalog.ProductListedIntegrationEvent no
// type.mapping - nada de classe compartilhada entre os servicos.

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListedIntegrationEvent implements IntegrationEvent {
    private UUID productId;
    private OffsetDateTime listedAt;

    @Override
    public String getAggregateId() {
        if (productId == null) {
            return null;
        }

        return productId.toString();
    }
}
