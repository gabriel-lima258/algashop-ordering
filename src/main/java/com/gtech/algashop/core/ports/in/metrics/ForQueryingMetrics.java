package com.gtech.algashop.core.ports.in.metrics;

import com.gtech.algashop.core.ports.out.metrics.SalesOverviewOutput;
import com.gtech.algashop.core.ports.out.metrics.TotalOverviewOutput;

import java.time.YearMonth;
import java.util.List;

// Porta de entrada das metricas do ordering: vendas por intervalo de meses e totais de agora.
public interface ForQueryingMetrics {
    List<SalesOverviewOutput> getSalesOverview(YearMonth yearMonthStart, YearMonth yearMonthEnd);
    TotalOverviewOutput getTotalOverview();
}
