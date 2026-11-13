package com.gtech.algashop.core.application.metrics;

import com.gtech.algashop.core.ports.in.metrics.ForQueryingMetrics;
import com.gtech.algashop.core.ports.out.metrics.ForObtainingMetrics;
import com.gtech.algashop.core.ports.out.metrics.SalesOverviewOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

// Traduz meses (o que o negocio pede) em datas (o que o banco filtra): do dia 1 do
// primeiro mes ao ultimo dia do ultimo mes, os dois inclusos.
@Service
@RequiredArgsConstructor
public class MetricsQueryService implements ForQueryingMetrics {

    private final ForObtainingMetrics forObtainingMetrics;

    @Override
    public List<SalesOverviewOutput> getSalesOverview(YearMonth yearMonthStart, YearMonth yearMonthEnd) {
        return forObtainingMetrics.getSalesOverview(
                yearMonthStart.atDay(1),
                yearMonthEnd.atEndOfMonth()
        );
    }
}
