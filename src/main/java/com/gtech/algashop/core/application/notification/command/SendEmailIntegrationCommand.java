package com.gtech.algashop.core.application.notification.command;

import com.gtech.algashop.core.application.OutboundIntegrationCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Comando "envie este e-mail" para o notifications. O JSON e o mesmo do SendEmailCommand do
// contrato (docs/asyncapi/messages.yml); no __TypeId__ viaja o alias Notifications.SendEmailCommand,
// nunca o nome desta classe. Dispare e esqueca: o notifications nao responde, por isso nao ha
// correlationId nem topico de reply. O aggregateId (id do pedido) e @JsonIgnore na interface.
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SendEmailIntegrationCommand implements OutboundIntegrationCommand {

    @NotBlank
    private String aggregateId;

    @NotNull
    @Valid
    private Recipient recipient;

    @NotNull
    private UUID templateId;

    @Builder.Default
    private Map<String, Object> variables = new HashMap<>();

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Recipient {

        @NotBlank
        private String email;

        private String name;
    }
}
