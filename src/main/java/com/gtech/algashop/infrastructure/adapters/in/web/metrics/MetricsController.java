package com.gtech.algashop.infrastructure.adapters.in.web.metrics;

import com.gtech.algashop.core.ports.in.metrics.ForQueryingMetrics;
import com.gtech.algashop.core.ports.out.metrics.SalesOverviewOutput;
import com.gtech.algashop.core.ports.out.metrics.TotalOverviewOutput;
import com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;
import java.time.YearMonth;
import java.util.List;

// Metricas do ordering para o painel admin: vendas por mes e totais de clientes e carrinhos.
// Nas vendas, sem periodo vale o ano corrente inteiro, e mes fora do formato yyyy-MM nao
// converte: responde 400 antes de chegar ao servico.
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class MetricsController {

    private final ForQueryingMetrics forQueryingMetrics;

    @SecurityAnnotations.CanReadMetrics
    @GetMapping("/sales-overview")
    public List<SalesOverviewOutput> getSalesOverview(@RequestParam(required = false)
                                                      @DateTimeFormat(pattern = "yyyy-MM")
                                                      YearMonth yearMonthStart,
                                                      @RequestParam(required = false)
                                                      @DateTimeFormat(pattern = "yyyy-MM")
                                                      YearMonth yearMonthEnd) {
        if (yearMonthStart == null) {
            yearMonthStart = Year.now().atMonth(1);
        }

        if (yearMonthEnd == null) {
            yearMonthEnd = Year.now().atMonth(12);
        }

        return forQueryingMetrics.getSalesOverview(yearMonthStart, yearMonthEnd);
    }

    // sem parametros: e uma foto de agora, nao um periodo
    @SecurityAnnotations.CanReadMetrics
    @GetMapping("/total-overview")
    public TotalOverviewOutput getTotalOverview() {
        return forQueryingMetrics.getTotalOverview();
    }
}
