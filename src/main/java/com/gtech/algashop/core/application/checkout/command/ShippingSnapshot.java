package com.gtech.algashop.core.application.checkout.command;

import java.math.BigDecimal;
import java.time.LocalDate;

// Dados de entrega do OrderSnapshot: custo e data ja calculados no checkout, mais
// destinatario e endereco - o consumidor nao volta a chamar o servico de frete.
public record ShippingSnapshot(
        BigDecimal cost,
        LocalDate expectedDate,
        RecipientSnapshot recipient,
        AddressSnapshot address
) {}
