package com.gtech.algashop.infrastructure.config.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

// Pluga o Bean Validation nos listeners Kafka: por padrao um @Valid em @Payload NAO
// valida nada - e preciso registrar um validator no endpoint registrar, e e exatamente
// isso que este KafkaListenerConfigurer faz. Com ele, payload que viole as anotacoes
// (os @NotNull do ProductPriceChangedV2IntegrationEvent) vira
// MethodArgumentNotValidException ANTES do metodo do handler rodar - evento invalido
// nao entra no fluxo de negocio.
// O destino do invalido e o DefaultErrorHandler padrao do spring-kafka: 10 tentativas
// sem backoff (inuteis - o payload nao vai mudar) e descarte com offset commitado.
// Sem DLQ ainda, isso e perda silenciosa - pendencia registrada.
@Configuration
@RequiredArgsConstructor
public class KafkaBeanValidationConfigurer implements KafkaListenerConfigurer {

    private final LocalValidatorFactoryBean validator;

    @Override
    public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
        registrar.setValidator(validator);
    }
}
