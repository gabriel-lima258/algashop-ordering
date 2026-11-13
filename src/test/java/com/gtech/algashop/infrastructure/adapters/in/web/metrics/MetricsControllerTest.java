package com.gtech.algashop.infrastructure.adapters.in.web.metrics;

import com.gtech.algashop.core.ports.in.metrics.ForQueryingMetrics;
import com.gtech.algashop.core.ports.out.metrics.SalesOverviewOutput;
import com.gtech.algashop.core.ports.out.metrics.TotalOverviewOutput;
import com.gtech.algashop.infrastructure.config.security.OrderingSecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Fatia web das metricas: formato do mes no JSON, periodo padrao, mes invalido, o JSON dos
// totais e a regra de acesso (escopo metrics:read e cliente final bloqueado).
@WebMvcTest(controllers = MetricsController.class)
@Import(OrderingSecurityConfig.class)
class MetricsControllerTest {

    private static final String URL = "/api/v1/metrics/sales-overview";
    private static final String TOTALS_URL = "/api/v1/metrics/total-overview";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private ForQueryingMetrics forQueryingMetrics;

    private static RequestPostProcessor manager() {
        return jwt().authorities(new SimpleGrantedAuthority("SCOPE_metrics:read"),
                new SimpleGrantedAuthority("ROLE_MANAGER"));
    }

    @Test
    void shouldReturnMonthAsYearMonthText() throws Exception {
        Mockito.when(forQueryingMetrics.getSalesOverview(YearMonth.of(2026, 1), YearMonth.of(2026, 2)))
                .thenReturn(List.of(
                        new SalesOverviewOutput(YearMonth.of(2026, 1), 40, new BigDecimal("499881.89")),
                        new SalesOverviewOutput(YearMonth.of(2026, 2), 0, BigDecimal.ZERO)));

        mockMvc.perform(get(URL).param("yearMonthStart", "2026-01").param("yearMonthEnd", "2026-02").with(manager()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].yearMonth").value("2026-01"))
                .andExpect(jsonPath("$[0].quantity").value(40))
                .andExpect(jsonPath("$[0].revenue").value(499881.89))
                .andExpect(jsonPath("$[1].yearMonth").value("2026-02"))
                .andExpect(jsonPath("$[1].quantity").value(0));
    }

    @Test
    void shouldUseCurrentYearWhenPeriodIsMissing() throws Exception {
        mockMvc.perform(get(URL).with(manager())).andExpect(status().isOk());

        Mockito.verify(forQueryingMetrics)
                .getSalesOverview(Year.now().atMonth(1), Year.now().atMonth(12));
    }

    @Test
    void shouldAnswerBadRequestForInvalidMonth() throws Exception {
        mockMvc.perform(get(URL).param("yearMonthStart", "2026-13").with(manager()))
                .andExpect(status().isBadRequest());

        Mockito.verifyNoInteractions(forQueryingMetrics);
    }

    @Test
    void shouldRequireToken() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
    }

    // token de maquina (client_credentials, como o algashop-test): escopo e nenhum papel
    @Test
    void shouldAllowMachineTokenWithScope() throws Exception {
        mockMvc.perform(get(URL).with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_metrics:read"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDenyCustomerEvenWithScope() throws Exception {
        mockMvc.perform(get(URL).with(jwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_metrics:read"),
                        new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRequireScopeEvenForManager() throws Exception {
        mockMvc.perform(get(URL).with(jwt().authorities(
                        new SimpleGrantedAuthority("SCOPE_orders:read"),
                        new SimpleGrantedAuthority("ROLE_MANAGER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnTotalOverview() throws Exception {
        Mockito.when(forQueryingMetrics.getTotalOverview()).thenReturn(new TotalOverviewOutput(21, 2));

        mockMvc.perform(get(TOTALS_URL).with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_metrics:read"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeCustomers").value(21))
                .andExpect(jsonPath("$.activeShoppingCarts").value(2));
    }

    @Test
    void shouldRequireTokenForTotalOverview() throws Exception {
        mockMvc.perform(get(TOTALS_URL)).andExpect(status().isUnauthorized());
        Mockito.verifyNoInteractions(forQueryingMetrics);
    }

    @Test
    void shouldRequireScopeForTotalOverview() throws Exception {
        mockMvc.perform(get(TOTALS_URL).with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products:read"))))
                .andExpect(status().isForbidden());
        Mockito.verifyNoInteractions(forQueryingMetrics);
    }
}
