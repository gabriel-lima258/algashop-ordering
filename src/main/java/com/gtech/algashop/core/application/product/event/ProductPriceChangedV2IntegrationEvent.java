package com.gtech.algashop.core.application.product.event;

import com.gtech.algashop.core.application.IntegrationEvent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Copia local do contrato ECST do product-catalog: o evento CARREGA os precos e o
// handler usa o newSalePrice direto - nenhuma chamada de volta ao catalogo. O "V2" no
// nome logico (ProductCatalog.ProductPriceChangedV2IntegrationEvent) e a versao do
// contrato viajando no __TypeId__: uma futura V3 conviveria com esta no mesmo topico.
// Os @NotNull valem de verdade AQUI: o @Valid do @KafkaHandler + o validator do
// KafkaBeanValidationConfigurer barram payload invalido antes do metodo rodar.
// changedAt e old* chegam e ainda NAO sao usados - sao a materia-prima da idempotencia
// e da guarda de ordem que estao nas pendencias.

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPriceChangedV2IntegrationEvent implements IntegrationEvent {
    @NotNull
    private UUID productId;
    @NotNull
    private OffsetDateTime changedAt;
    @NotNull
    private BigDecimal oldRegularPrice;
    @NotNull
    private BigDecimal oldSalePrice;
    @NotNull
    private BigDecimal newRegularPrice;
    @NotNull
    private BigDecimal newSalePrice;

    @Override
    public String getAggregateId() {
        if (productId == null) {
            return null;
        }

        return productId.toString();
    }
}
