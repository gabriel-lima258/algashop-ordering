package com.gtech.algashop.infrastructure.config.email;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

// Configuracao do que o ordering precisa saber do notifications. O id do template Order Placed
// muda por ambiente (no development-env o notifications o recria a cada subida; fora dele o
// template e criado pela API), por isso nunca fica fixo no codigo. @NotNull: sem id, nem sobe.
@Component
@Validated
@Data
@ConfigurationProperties("algashop.notifications")
public class NotificationsProperties {

    @NotNull
    @Valid
    private EmailTemplates emailTemplates;

    @Data
    public static class EmailTemplates {

        /** Id do template "pedido recebido" cadastrado no Notifications. */
        @NotNull
        private UUID orderPlaced;
    }
}
