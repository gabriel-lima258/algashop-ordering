package com.gtech.algashop.core.application;

import com.gtech.algashop.infrastructure.adapters.in.web.utils.TestContainerPostgresSQLConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerPostgresSQLConfig.class)
public abstract class AbstractIntegrationTest {

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
