package com.gtech.algashop.infrastructure.config.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.Set;

// Valida um objeto com Bean Validation (as anotacoes @NotNull etc. da propria classe)
// e lanca ConstraintViolationException se houver violacao. Usado pelos publishers
// Kafka DIRETOS (KafkaOrderIntegrationEvent/CommandPublisher): o evento e validado
// ANTES do send. ATENCAO: esses publishers so existem com outbox.enabled=false - no
// caminho outbox (o que roda em dev) o OutboxRecorder NAO valida nada antes de gravar,
// e a pendencia esta registrada em outbox-e-checkout-assincrono.md.
// Atencao ao sentido da condicao: valida-se lancando quando o conjunto de violacoes NAO
// esta vazio. A versao invertida (if isEmpty -> throw) compila, passa despercebida e faz
// o oposto do combinado: derruba todo evento valido e deixa o invalido passar.

@Component
@RequiredArgsConstructor
public class BeanValidationUtil {

    private final LocalValidatorFactoryBean validatorFactory;

    public void validate(Object object) {
        Validator validator = validatorFactory.getValidator();
        Set<ConstraintViolation<Object>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            // ConstraintViolationException ja e ValidationException e carrega as violacoes -
            // embrulhar em outra excecao so esconderia os campos violados da mensagem
            throw new ConstraintViolationException(violations);
        }
    }
}
