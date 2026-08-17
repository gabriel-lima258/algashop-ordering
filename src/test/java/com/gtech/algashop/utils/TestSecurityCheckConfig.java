package com.gtech.algashop.utils;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.UUID;

// Usuario autenticado de mentira, para as fatias de persistencia (@DataJpaTest).
//
// POR QUE ISTO PASSOU A SER NECESSARIO
// Ate a Fase 24 o SpringDataAuditingConfig devolvia Optional.of(UUID.randomUUID()): nao
// dependia de nada e funcionava em qualquer contexto. Na Fase 25 ele passou a perguntar
// "quem esta chamando?" ao SecurityCheckApplicationService - e uma fatia @DataJpaTest so
// carrega repositorios e entidades, nao os @Service da infraestrutura. Resultado: o
// contexto das 20 ITs de persistencia parou de subir com NoSuchBeanDefinitionException.
//
// POR QUE UM USUARIO FIXO, E NAO Optional.empty()
// As asserções de auditoria (getCreatedByUserId().isNotNull()) existem para provar que o
// Spring Data PREENCHE o autor. Com autor nulo elas so provariam que a coluna aceita
// nulo. Com um UUID fixo e conhecido da-se para afirmar o valor exato - o teste fica mais
// forte do que era antes, quando o valor era aleatorio e nao dizia nada.
//
// O que este stub NAO cobre: a extracao do "sub" do JWT. Isso e trabalho do
// OAuth2SecurityCheckApplicationServiceImpl e se verifica no fluxo real, com token.
@TestConfiguration
public class TestSecurityCheckConfig {

    public static final UUID TEST_USER_ID =
            UUID.fromString("01912e0a-0000-7000-8000-000000000001");

    @Bean
    public SecurityCheckApplicationService securityCheck() {
        return new SecurityCheckApplicationService() {

            @Override
            public UUID getAuthenticatedUserId() {
                return TEST_USER_ID;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public boolean isMachineAuthenticated() {
                return false;
            }

            @Override
            public boolean canAccessOwnProfile() {
                return true;
            }
        };
    }
}
