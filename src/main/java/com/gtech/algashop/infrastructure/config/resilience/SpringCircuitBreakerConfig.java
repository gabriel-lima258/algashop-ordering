package com.gtech.algashop.infrastructure.config.resilience;

import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.BadGatewayException;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.GatewayTimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;

import java.time.Duration;

// FUNCAO DESTA CLASSE: registrar, em um lugar so, a politica de resiliencia. Ela nao executa chamada
// nenhuma - so descreve COMO o Spring Cloud Circuit Breaker deve se comportar quando
// alguem pedir um circuit breaker com o nome "productCatalogCB", "rapidexAPICB" e etc.
//
// A diferenca para o @Retryable espalhado no client: la a politica fica grudada no
// metodo (so retry, sem circuito); aqui ela e centralizada e ganha o estado do circuito -
// depois de N falhas seguidas o circuito ABRE e as chamadas passam a falhar na hora,
// sem nem sair para a rede. Isso evita ficar martelando um servico que ja esta caido.
//
// POR QUE OS TEMPOS VEM DE PROPERTY: os defaults abaixo sao exatamente os valores que
// estavam fixos aqui, entao producao nao muda nada sem YAML. Mas com backoff de
// 3s -> 6s -> 12s, um unico teste de retry levaria 21s parado; os ITs sobrescrevem para
// milissegundos. Testabilidade e o motivo principal, ajuste por ambiente e o bonus.
@Configuration
public class SpringCircuitBreakerConfig {

    public static final String productCatalogCBId = "productCatalogCB";
    public static final String rapidexCBId = "rapidexAPICB";

    // Customizer<...Factory> e o gancho que o Spring Cloud chama uma unica vez, no
    // startup, entregando a fabrica de circuit breakers para configurarmos.
    @Bean
    public Customizer<FrameworkRetryCircuitBreakerFactory> defaultCustomizer(
            @Value("${algashop.resilience.circuit-breaker.max-retries:3}") long maxRetries,
            @Value("${algashop.resilience.circuit-breaker.delay:3s}") Duration delay,
            @Value("${algashop.resilience.circuit-breaker.multiplier:2}") double multiplier,
            @Value("${algashop.resilience.circuit-breaker.open-timeout:5s}") Duration openTimeout,
            @Value("${algashop.resilience.circuit-breaker.reset-timeout:30s}") Duration resetTimeout) {

        // Mesma politica dos clients: 3 novas tentativas (alem da chamada original),
        // com backoff exponencial 3s -> 6s -> 12s (delay * multiplier a cada rodada).
        // includes = so essas exceptions disparam o retry; qualquer outra (ex.: 400,
        // 404) sobe direto, porque tentar de novo nao mudaria o resultado.
        //
        // ServerErrorException e ClientErrorException agora herdam de BadGatewayException,
        // mas o includes continua listando SO a ServerErrorException: o retry casa por
        // assignability, entao 4xx e a BadGatewayException generica seguem sem retry.
        RetryPolicy retryPolicy = RetryPolicy.builder()
                .maxRetries(maxRetries)
                .multiplier(multiplier)
                .delay(delay)
                .includes(GatewayTimeoutException.class, BadGatewayException.ServerErrorException.class)
                .build();

        return factory -> {
            // O segundo argumento do configure() e o ID do circuit breaker: essa config
            // so vale para quem pedir o breaker por esse nome.
            //
            // Semantica dos dois tempos (CircuitBreakerRetryPolicy):
            // openTimeout  = quanto tempo o circuito fica ABERTO falhando rapido. Passados
            //                os 5s, a proxima chamada vira HALF_OPEN e e deixada passar
            //                como teste: se der certo o circuito fecha, se falhar ele
            //                reabre e conta 5s de novo.
            // resetTimeout = tempo SEM nenhuma falha que zera o estado do circuito (fecha
            //                e descarta a ultima excecao). 30s parado sem erro = pagina
            //                limpa.
            //
            // ATENCAO 1: nao ha threshold de falhas. UMA unica execucao que termine em
            // excecao (ou seja, um ciclo de retry esgotado) ja leva CLOSED -> OPEN. E uma
            // diferenca grande em relacao ao Resilience4j, onde o padrao e abrir por
            // PERCENTUAL de falha numa janela deslizante. Aqui um pico isolado abre.
            //
            // ATENCAO 2 - os dois numeros nao conversam: openTimeout e 5s, mas um ciclo de
            // retry esgotado leva 3+6+12 = 21s so de backoff, mais o read timeout de cada
            // tentativa. Quando o circuito finalmente abre, os 5s dele ja expiraram ou estao
            // por expirar - entao a proxima chamada tende a passar como HALF_OPEN em vez de
            // falhar rapido. Na pratica o circuito protege menos do que parece.
            //
            // Nao foi alterado aqui de proposito: mexer em qualquer um dos dois muda
            // comportamento, e a escolha certa depende de medir. Fica registrado em
            // docs/01-arquitetura-design/resiliencia.md
            factory.configure(builder -> builder
                    .retryPolicy(retryPolicy)
                    .openTimeout(openTimeout)
                    .resetTimeout(resetTimeout)
                    .build(), productCatalogCBId);

            factory.configure(builder -> builder
                    .retryPolicy(retryPolicy)
                    .openTimeout(openTimeout)
                    .resetTimeout(resetTimeout)
                    .build(), rapidexCBId);
        };
    }
}
