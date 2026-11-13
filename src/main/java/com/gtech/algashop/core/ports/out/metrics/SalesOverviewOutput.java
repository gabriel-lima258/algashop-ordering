package com.gtech.algashop.core.ports.out.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

// Uma linha por mes: quantos pedidos pagos e quanto entrou. O YearMonth sai no JSON como "2026-07".
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesOverviewOutput {
    private YearMonth yearMonth;
    private Integer quantity;
    private BigDecimal revenue;
}
