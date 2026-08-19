package com.gtech.algashop.core.application;

import com.gtech.algashop.core.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.utils.MockJwtDecoderConfig;
import com.gtech.algashop.utils.TestAuthentications;
import com.gtech.algashop.utils.TestContainerPostgresSQLConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestContainerPostgresSQLConfig.class, MockJwtDecoderConfig.class})
public abstract class AbstractIntegrationTest {

    /**
     * Fase 27: os application services deixaram de aceitar "autenticado" como suficiente.
     * Checkout, buyNow e a consulta de pedidos passaram a exigir SER O DONO - isCustomer() e
     * getAuthenticatedUserId() iguais ao customerId do recurso.
     *
     * Sem uma autenticacao no contexto, estes ITs levariam AccessDeniedException em toda
     * escrita. Autenticar como o cliente padrao dos builders (o mesmo id do seed, e o mesmo
     * do auth_user no authorization server) e o que faz o teste EXERCITAR a regra em vez de
     * contorna-la: quem quiser provar a recusa autentica como outro id.
     */
    @BeforeEach
    void authenticateAsDefaultCustomer() {
        TestAuthentications.authenticateAsCustomer(CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID.value());
    }

    @AfterEach
    void clearAuthentication() {
        TestAuthentications.clear();
    }

    // inicia o postgres automaticamnete no docker e depois morre ao final do teste
//    @Container
//    @ServiceConnection // uma forma alternativa de criar dynamicSource de configs
//    protected static PostgreSQLContainer postgresSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine")
//            .withDatabaseName("ordering_test");

    // configura as configurações dinamicas do container postgres ao docker
//    @DynamicPropertySource
//    private static void configureDatasourceProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgresSQLContainer::getJdbcUrl);
//        registry.add("spring.datasource.username", postgresSQLContainer::getUsername);
//        registry.add("spring.datasource.password", postgresSQLContainer::getPassword);
//        registry.add("spring.flyway.url", postgresSQLContainer::getJdbcUrl);
//        registry.add("spring.flyway.user", postgresSQLContainer::getUsername);
//        registry.add("spring.flyway.password", postgresSQLContainer::getPassword);
//    }

}
