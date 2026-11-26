package com.gtech.algashop.infrastructure.adapters.out.messaging.outbox;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Trava a regra do OutboxProperties: so UMA forma de publicar a outbox. Com
// delete-after-record (Debezium) a linha some na gravacao; ligar o dispatcher junto
// faria ele procurar linhas que nunca vai encontrar. O servico nao sobe nessa combinacao.
class OutboxPropertiesTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest(name = "deleteAfterRecord={0}, dispatcher={1} -> valido={2}")
    @CsvSource({
            "false, false, true",  // outbox gravada, ninguem publica (ex.: testes)
            "false, true,  true",  // publicacao por polling (dispatcher)
            "true,  false, true",  // publicacao pelo Debezium
            "true,  true,  false"  // as duas: proibido
    })
    void shouldAllowOnlyOnePublishingStrategy(boolean deleteAfterRecord, boolean dispatcherEnabled, boolean valid) {
        OutboxProperties properties = new OutboxProperties();
        properties.setDeleteAfterRecord(deleteAfterRecord);
        properties.getDispatcher().setEnabled(dispatcherEnabled);

        Set<ConstraintViolation<OutboxProperties>> violations = validator.validate(properties);

        assertThat(violations.isEmpty()).isEqualTo(valid);
        if (!valid) {
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactly("configurationConsistent");
        }
    }
}
