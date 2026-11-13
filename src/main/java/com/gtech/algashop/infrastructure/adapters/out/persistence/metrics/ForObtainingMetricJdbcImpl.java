package com.gtech.algashop.infrastructure.adapters.out.persistence.metrics;

import com.gtech.algashop.core.ports.out.metrics.ForObtainingMetrics;
import com.gtech.algashop.core.ports.out.metrics.SalesOverviewOutput;
import com.gtech.algashop.core.ports.out.metrics.TotalOverviewOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

// Relatorios taticos direto no banco do proprio ordering. SQL na mao e nao JPA: e leitura
// agregada (soma e contagem), nao ha agregado de dominio para carregar.
// Vendas por mes:
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

    // Totais. O numero vale o que vale a definicao de "ativo":
    //   - cliente ativo = nao arquivado, tenha carrinho ou nao;
    //   - carrinho ativo = com pelo menos um item E de cliente ativo.
    // Uma consulta so: parte do cliente (left join, para quem nao tem carrinho continuar na
    // conta) e conta coluna, nao linha: count(sc.id) ignora o null de quem ficou sem carrinho,
    // count(*) contaria. O total_items > 0 fica no ON: no WHERE ele tiraria da contagem
    // de clientes quem tem carrinho vazio ou nenhum carrinho.
    // Depende de haver no maximo um carrinho por cliente (regra do dominio, sem UNIQUE no
    // banco): com dois carrinhos com item, o cliente seria contado duas vezes.
    private static final String TOTAL_OVERVIEW_SQL = """
            select count(c.id) as active_customers,
                   count(sc.id) as active_shopping_carts
            from customer c
            left join shopping_cart sc
                on sc.customer_id = c.id
                and sc.total_items > 0
            where c.archived is false
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

    @Override
    public TotalOverviewOutput getTotalOverview() {
        return jdbcTemplate.queryForObject(TOTAL_OVERVIEW_SQL,
                Map.of(),
                (rs, rowNum) -> new TotalOverviewOutput(
                        rs.getInt("active_customers"),
                        rs.getInt("active_shopping_carts")
                    )
                );
    }
}
