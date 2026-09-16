package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

// Tunings do outbox como properties tipadas e validadas, com DEFAULTS de campo: o bean
// e @Component incondicional, entao sobe em TODO perfil - um perfil sem o bloco
// algashop.messaging.outbox (test, production) nao pode derrubar o contexto por causa
// de um @NotNull sem valor. O development-env sobrescreve o que quiser.
@Validated
@Component
@Data
@ConfigurationProperties("algashop.messaging.outbox")
public class OutboxProperties {
    private boolean enabled = false;

    @NotNull
    private Dispatcher dispatcher = new Dispatcher();

    @NotNull
    private Duration sendTimeout = Duration.ofSeconds(5);

    @Min(1)
    private int batchSize = 100;

    @NotNull
    private Duration pollInterval = Duration.ofMinutes(1);

    @Min(1)
    private int maxAttempts = 10;

    @NotNull
    private Duration backoff = Duration.ofSeconds(5);

    @NotNull
    private Duration batchDeadLine = Duration.ofMinutes(3);

    @Data
    public static class Dispatcher {
        private boolean enabled = false;
    }
}
