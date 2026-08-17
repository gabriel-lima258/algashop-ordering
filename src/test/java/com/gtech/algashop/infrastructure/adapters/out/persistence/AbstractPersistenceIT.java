package com.gtech.algashop.infrastructure.adapters.out.persistence;

import com.gtech.algashop.infrastructure.config.auditing.SpringDataAuditingConfig;
import com.gtech.algashop.utils.TestContainerPostgresSQLConfig;
import com.gtech.algashop.utils.TestSecurityCheckConfig;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
// TestSecurityCheckConfig entra junto porque o SpringDataAuditingConfig passou a depender
// do SecurityCheckApplicationService (Fase 25), que a fatia @DataJpaTest nao carrega.
@Import({
        TestContainerPostgresSQLConfig.class,   // gerenciamento do bean do postgres
        SpringDataAuditingConfig.class,
        TestSecurityCheckConfig.class
})
public class AbstractPersistenceIT {

}
