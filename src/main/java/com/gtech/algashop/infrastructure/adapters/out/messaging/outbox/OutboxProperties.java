package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import jakarta.validation.constraints.AssertTrue;
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

    // true = a publicacao e feita pelo Debezium (CDC por log): o OutboxRecorder apaga a
    // linha logo depois de grava-la, na mesma transacao. O INSERT fica no WAL, que e de
    // onde o conector le; a tabela nunca acumula.
    private boolean deleteAfterRecord = false;

    // Trava: com a linha apagada na gravacao, o dispatcher nunca a veria - o envio dependeria
    // so do conector e o dispatcher ficaria girando em vao. Uma das duas formas de publicar,
    // nunca as duas. Getter sem setter: o Spring so valida, nao faz bind de "configuration-consistent".
    @AssertTrue(message = "algashop.messaging.outbox: delete-after-record (publicacao pelo Debezium) "
            + "e dispatcher.enabled (publicacao por polling) nao podem estar ligados juntos - "
            + "a linha seria apagada antes de o dispatcher le-la")
    public boolean isConfigurationConsistent() {
        return !(deleteAfterRecord && dispatcher != null && dispatcher.isEnabled());
    }

    @Data
    public static class Dispatcher {
        private boolean enabled = false;
    }
}
