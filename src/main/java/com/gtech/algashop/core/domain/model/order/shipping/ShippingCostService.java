package com.gtech.algashop.core.domain.model.order.shipping;

import com.gtech.algashop.core.domain.model.commons.Money;
import com.gtech.algashop.core.domain.model.commons.ZipCode;
import lombok.Builder;

import java.time.LocalDate;

/**
 * 🔌 Domain Port – ShippingCostService
 *
 * Representa um serviço de domínio para cálculo de frete.
 *
 * ❗ Esta interface NÃO conhece:
 * - APIs externas
 * - DTOs de terceiros
 * - formatos de JSON
 * - códigos de status externos
 *
 * Ela define apenas o que o DOMÍNIO precisa:
 * "Dado uma origem e um destino, me retorne o custo e a data estimada."
 *
 * A implementação desta interface ficará na camada de infraestrutura,
 * onde uma Anti-Corruption Layer (ACL) irá:
 *
 * 1. Traduzir os Value Objects do domínio (ZipCode, Money)
 *    → para o formato exigido pela API externa
 *
 * 2. Chamar o client externo (ex: Correios, MelhorEnvio, etc.)
 *
 * 3. Traduzir a resposta externa (DTO, status, centavos, datas)
 *    → para objetos puros do domínio (Money, LocalDate)
 *
 * Dessa forma, o domínio permanece isolado e sem acoplamento
 * com modelos externos.
 */
public interface ShippingCostService {
    CalculationResult calculate(CalculationRequest request);

    @Builder
    record CalculationRequest(ZipCode origin, ZipCode destination){}

    @Builder
    record CalculationResult(Money cost, LocalDate expectedDate){}
}
