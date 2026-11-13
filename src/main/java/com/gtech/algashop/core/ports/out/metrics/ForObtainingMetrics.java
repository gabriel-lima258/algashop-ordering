package com.gtech.algashop.core.ports.out.metrics;

import java.time.LocalDate;
import java.util.List;

// Porta de saida: quem calcula (hoje SQL no banco do ordering) fica fora do core.
public interface ForObtainingMetrics {
    List<SalesOverviewOutput> getSalesOverview(LocalDate startDate, LocalDate endDate);
    TotalOverviewOutput getTotalOverview();
}
