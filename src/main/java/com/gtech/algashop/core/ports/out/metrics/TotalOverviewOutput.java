package com.gtech.algashop.core.ports.out.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Totais do momento: clientes ativos e carrinhos ativos. Mesmos nomes do schema TotalOverview.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TotalOverviewOutput {
    private Integer activeCustomers;
    private Integer activeShoppingCarts;
}
