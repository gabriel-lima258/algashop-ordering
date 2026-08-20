package com.gtech.algashop.core.application.security;

import com.gtech.algashop.core.application.AbstractIntegrationTest;
import com.gtech.algashop.core.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.utils.WithMockJwt;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * As regras de seguranca exercitadas contra o adapter REAL.
 *
 * Nada aqui e mockado do lado do SecurityCheckApplicationService: quem responde e o
 * OAuth2SecurityCheckApplicationServiceImpl, lendo um SecurityContext que o @WithMockJwt
 * montou a partir de um Jwt de verdade, convertido pelo converter de producao. Se o
 * prefixo ROLE_ mudar, se o converter parar de somar o papel, ou se a heuristica de
 * isMachineAuthenticated() (aud contendo sub) for alterada, estes testes acusam.
 *
 * A identidade padrao vem do @WithMockJwt da superclasse: CUSTOMER, com o id do cliente
 * padrao dos builders. Cada teste que precisa de outra a declara na propria assinatura.
 */
class SecurityCheckApplicationServiceIT extends AbstractIntegrationTest {

    private static final String MACHINE_CLIENT_ID = "machine-client-id";

    @Autowired
    private SecurityCheckApplicationService securityCheckApplicationService;

    @Test
    void givenAuthenticatedCustomerShouldAllowOrderForHimself() {
        UUID customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID.value();
        Assertions.assertThat(securityCheckApplicationService.canOrderFor(customerId)).isTrue();
    }

    /** O caso que a regra existe para impedir: pedir em nome de outra pessoa. */
    @Test
    void givenAuthenticatedCustomerShouldDenyOrderForSomeoneElse() {
        Assertions.assertThat(securityCheckApplicationService.canOrderFor(UUID.randomUUID())).isFalse();
    }

    /** O guarda de nulo: sem cliente nao ha dono a comparar, e a resposta e negar. */
    @Test
    void givenNullCustomerShouldDenyOrder() {
        Assertions.assertThat(securityCheckApplicationService.canOrderFor(null)).isFalse();
    }

    @Test
    @WithMockJwt(role = "MANAGER")
    void givenAuthenticatedManagerShouldNotBeCustomer() {
        Assertions.assertThat(securityCheckApplicationService.isCustomer()).isFalse();
    }

    /**
     * Back-office nao compra em nome do cliente - decisao da Fase 27, registrada como
     * pendencia. Este teste fixa o comportamento atual; se a decisao mudar, ele avisa.
     */
    @Test
    @WithMockJwt(role = "MANAGER")
    void givenAuthenticatedManagerShouldDenyOrder() {
        UUID customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID.value();
        Assertions.assertThat(securityCheckApplicationService.canOrderFor(customerId)).isFalse();
    }

    // ---- token de maquina -------------------------------------------------------------
    // role vazio + subject igual a audience: e assim que se simula client_credentials.
    // O converter so soma ROLE_* quando o claim nao e branco, e isMachineAuthenticated()
    // deduz maquina de aud conter sub.

    @Test
    @WithMockJwt(role = "", audiences = MACHINE_CLIENT_ID, subject = MACHINE_CLIENT_ID)
    void givenAuthenticatedMachineShouldBeDetectedAsMachine() {
        Assertions.assertThat(securityCheckApplicationService.isMachineAuthenticated()).isTrue();
    }

    @Test
    @WithMockJwt(role = "", audiences = MACHINE_CLIENT_ID, subject = MACHINE_CLIENT_ID)
    void givenAuthenticatedMachineShouldNotAllowOrder() {
        UUID customerId = CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID.value();
        Assertions.assertThat(securityCheckApplicationService.canOrderFor(customerId)).isFalse();
    }

    /** Um usuario nunca deve ser confundido com maquina - o outro lado da heuristica. */
    @Test
    void givenAuthenticatedCustomerShouldNotBeDetectedAsMachine() {
        Assertions.assertThat(securityCheckApplicationService.isMachineAuthenticated()).isFalse();
    }

    /**
     * O contrato do proprio @WithMockJwt: papel preenchido vira ROLE_*, papel vazio nao
     * vira nada. Toda a anotacao depende desse comportamento do
     * JwtGrantedAuthoritiesDelegatingConverter, e nenhum compilador o verifica.
     */
    @Test
    void authenticatedCustomerShouldCarryTheRoleAuthority() {
        Assertions.assertThat(securityCheckApplicationService.isCustomer()).isTrue();
        Assertions.assertThat(securityCheckApplicationService.isAuthenticated()).isTrue();
    }
}
