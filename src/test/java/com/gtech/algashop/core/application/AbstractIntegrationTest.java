package com.gtech.algashop.core.application;

import com.gtech.algashop.utils.MockJwtDecoderConfig;
import com.gtech.algashop.utils.TestContainerPostgresSQLConfig;
import com.gtech.algashop.utils.WithMockJwt;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestContainerPostgresSQLConfig.class, MockJwtDecoderConfig.class})
/**
 * Identidade DECLARATIVA para os ITs de aplicacao.
 *
 * Desde a Fase 27 checkout, buyNow e a consulta de pedidos exigem SER O DONO do recurso -
 * nao basta estar autenticado. Sem identidade no contexto, todo IT levaria AccessDenied.
 *
 * O @WithMockJwt aqui vale para a hierarquia inteira e usa o cliente padrao dos builders
 * (o mesmo id do seed, e o mesmo do auth_user no authorization server). Quem precisa de
 * outro papel sobrescreve na propria classe - @WithMockJwt(role = "MANAGER") - e quem
 * precisa de uma identidade que so existe DEPOIS que o teste roda (um cliente criado no
 * proprio metodo) usa TestAuthentications, que faz o mesmo de forma imperativa.
 */
@WithMockJwt
public abstract class AbstractIntegrationTest {
}
