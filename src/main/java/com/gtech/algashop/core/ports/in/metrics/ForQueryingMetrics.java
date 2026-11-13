package com.gtech.algashop.core.ports.in.metrics;

import com.gtech.algashop.core.ports.out.metrics.SalesOverviewOutput;

import java.time.YearMonth;
import java.util.List;

// Porta de entrada das metricas de vendas: o painel pergunta por um intervalo de meses.
public interface ForQueryingMetrics {
    List<SalesOverviewOutput> getSalesOverview(YearMonth yearMonthStart, YearMonth yearMonthEnd);
}
