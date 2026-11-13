package com.gtech.algashop.infrastructure.adapters.out.metrics;

import com.gtech.algashop.core.ports.out.metrics.ForObtainingMetrics;
import com.gtech.algashop.core.ports.out.metrics.SalesOverviewOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

// Relatorio tatico (periodo fechado) direto no banco do proprio ordering. SQL na mao e nao
// JPA: e leitura agregada (soma e contagem por mes), nao ha agregado de dominio para carregar.
//   - generate_series cria um mes por linha: mes sem venda aparece com 0 em vez de sumir
//     do grafico (por isso o left join e o coalesce);
//   - conta pelo paid_at, nao pelo placed_at: receita e o que foi pago, nao o que foi pedido;
//   - cancelado fica fora, mesmo que tenha sido pago.
// Limite deste caminho: so enxerga o banco do ordering. Juntar pagamento (billing) ou
// catalogo exige outra estrategia (streaming para um banco de relatorios).
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ForObtainingMetricJdbcImpl implements ForObtainingMetrics {

    private static final String SALES_OVERVIEW_SQL = """
            select to_char(g, 'yyyy-mm') as year_month,
                   count(o.id) as quantity,
                   coalesce(sum(o.total_amount), 0) as revenue
            from generate_series(cast(:startDate as date), cast(:endDate as date), interval '1 month') as g
            left join "order" o
                on date_trunc('month', o.paid_at) = date_trunc('month', g)
                and o.paid_at is not null
                and o.canceled_at is null
            group by 1
            order by 1
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<SalesOverviewOutput> getSalesOverview(LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.query(SALES_OVERVIEW_SQL,
                Map.of("startDate", startDate, "endDate", endDate),
                (rs, rowNum) -> new SalesOverviewOutput(
                        YearMonth.parse(rs.getString("year_month")),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("revenue")
                    )
                );
    }
}
