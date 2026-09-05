package com.gtech.algashop.core.application.product.event;

import com.gtech.algashop.core.application.IntegrationEvent;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

// Copia local do contrato do product-catalog para "produto saiu da vitrine" (disable,
// nao exclusao) - event notification, par do ProductListedIntegrationEvent.
// O campo e delistedAt: o typo deslistedAt foi consertado ANTES do primeiro deploy -
// depois que o JSON circula, renomear campo e quebra de contrato com o produtor.

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDelistedIntegrationEvent implements IntegrationEvent {
    private UUID productId;
    private OffsetDateTime delistedAt;

    @Override
    public String getAggregateId() {
        if (productId == null) {
            return null;
        }

        return productId.toString();
    }
}
