package com.gtech.algashop.infrastructure.adapters.out.persistence;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.infrastructure.config.auditing.SpringDataAuditingConfig;
import com.gtech.algashop.utils.TestContainerPostgresSQLConfig;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
// O SpringDataAuditingConfig depende do SecurityCheckApplicationService (Fase 25), e a
// fatia @DataJpaTest nao carrega os @Service da infraestrutura. Ate a Fase 28 isso era
// resolvido por um TestSecurityCheckConfig com implementacao anonima; agora e um
// @MockitoBean, que diz a mesma coisa em menos linhas e permite a cada teste ajustar a
// resposta se precisar.
@Import({
        TestContainerPostgresSQLConfig.class,   // gerenciamento do bean do postgres
        SpringDataAuditingConfig.class
})
public class AbstractPersistenceIT {

    /**
     * UUID FIXO, e nao aleatorio, de proposito.
     *
     * O que estas fatias verificam sobre auditoria e que o Spring Data grava O AUTOR CERTO -
     * e afirmar isso exige um valor conhecido. Com UUID.randomUUID() aqui, a unica assercao
     * possivel seria isNotNull(), que passaria igual se a auditoria regredisse ao autor
     * aleatorio que existia antes da Fase 25. O teste diria "tem alguma coisa na coluna",
     * que e exatamente o que o placeholder antigo tambem dizia.
     */
    protected static final UUID TEST_USER_ID =
            UUID.fromString("01912e0a-0000-7000-8000-000000000001");

    @MockitoBean
    protected SecurityCheckApplicationService securityCheckApplicationService;

    @BeforeEach
    public void setup() {
        Mockito.when(securityCheckApplicationService.isAuthenticated()).thenReturn(true);
        Mockito.when(securityCheckApplicationService.isMachineAuthenticated()).thenReturn(false);
        Mockito.when(securityCheckApplicationService.getAuthenticatedUserId()).thenReturn(TEST_USER_ID);
    }
}
