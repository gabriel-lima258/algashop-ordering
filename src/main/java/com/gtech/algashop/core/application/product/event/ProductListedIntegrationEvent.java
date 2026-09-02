package com.gtech.algashop.core.application.product.event;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

// Copia local do contrato do product-catalog para "produto voltou a vitrine".
// O que casa este POJO com o JSON publicado e a forma dos campos + o nome logico
// ProductCatalog.ProductListedIntegrationEvent no type.mapping do consumidor -
// nada de classe compartilhada entre os servicos.
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListedIntegrationEvent {
    private UUID productId;
    private OffsetDateTime listedAt;
}
