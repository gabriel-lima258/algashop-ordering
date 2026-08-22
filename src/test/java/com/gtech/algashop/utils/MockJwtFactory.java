package com.gtech.algashop.utils;

import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// JwtDecoder de mentira, para os *IT rodarem sem authorization server de pe.
//
// ATENCAO ao que ele NAO faz, porque a suite verde sugere mais cobertura do que existe:
// ele substitui o decoder inteiro, entao assinatura, iss, exp e aud nunca sao
// verificados de verdade - o Jwt ja sai pronto do mock. O que estes testes cobrem e a
// camada de AUTORIZACAO (o @PreAuthorize decidindo 403), nao a de AUTENTICACAO.
//
// O caso "expirado" tambem e simulado: e um thenThrow(JwtException), que exercita o
// tratamento de falha de decode - nao um token com exp no passado sendo rejeitado pelo
// validador. Validacao real de token so acontece contra o authorization server rodando.
public class MockJwtFactory {

    public static final String DEFAULT_ISSUER_URI = "http://auth.algashop.local:9000";

    public static final String[] DEFAULT_SCOPES = new String[] {
            "orders:read",
            "orders:write",
            "customers:read",
            "customers:write",
            "shopping-carts:read",
            "shopping-carts:write",
            "shipping-costs:preview"
    };

    public static final String DEFAULT_SUBJECT = "6e148bd5-47f6-4022-b9da-07cfaa294f7a";

    // A audiencia existe e  DIFERENTE do subject - e assim que se distingue token de
    // usuario de token de maquina (no client_credentials o sub e o proprio client_id,
    // que tambem aparece na aud).
    public static final String[] DEFAULT_AUDIENCES = {"ecommerce-web-app"};

    public static final String DEFAULT_ROLE = "CUSTOMER";

    // Um token por NIVEL de acesso. O MANAGER tem sub proprio (um back-office user nao e um
    // customer da base); o de maquina segue a regra do client_credentials: sub == aud e
    // nenhum papel - e assim que o SecurityCheck distingue maquina de gente.
    public static final String MANAGER_SUBJECT = "9c1f4d27-0000-7000-8000-0000000000aa";

    // usuario CUSTOMER que existe no authorization server mas AINDA NAO se registrou como
    // customer aqui - e o unico jeito de exercitar o POST /me sem colidir com o seed
    public static final String NEW_CUSTOMER_SUBJECT = "3d94c2b1-0000-7000-8000-0000000000bb";

    public static final String DEFAULT_TOKEN_VALUE = "fake.jwt.token";
    public static final String NO_SCOPE_TOKEN_VALUE = "fake.jwt.no-scope";
    public static final String EXPIRED_TOKEN_VALUE = "fake.jwt.expired";
    public static final String MANAGER_TOKEN_VALUE = "fake.jwt.manager";
    public static final String MACHINE_TOKEN_VALUE = "fake.jwt.machine";
    public static final String NEW_CUSTOMER_TOKEN_VALUE = "fake.jwt.new-customer";

    public static JwtDecoder createMockJwtDecoder() {
        JwtDecoder jwtDecoder = Mockito.mock(JwtDecoder.class);

        Mockito.when(jwtDecoder.decode(DEFAULT_TOKEN_VALUE))
                .thenReturn(buildDefaultJwt());
        Mockito.when(jwtDecoder.decode(NO_SCOPE_TOKEN_VALUE))
                .thenReturn(buildNoScopeJwt());
        Mockito.when(jwtDecoder.decode(EXPIRED_TOKEN_VALUE))
                .thenThrow(new JwtException("Token is expired"));
        Mockito.when(jwtDecoder.decode(MANAGER_TOKEN_VALUE))
                .thenReturn(buildManagerJwt());
        Mockito.when(jwtDecoder.decode(MACHINE_TOKEN_VALUE))
                .thenReturn(buildMachineJwt());
        Mockito.when(jwtDecoder.decode(NEW_CUSTOMER_TOKEN_VALUE))
                .thenReturn(buildNewCustomerJwt());


        return jwtDecoder;
    }

    public static Jwt buildJwt(String tokenValue, String subject,
                               String issuer, String[] scopes,
                               String role, String[] audiences) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(600);

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        claims.put("iss", issuer);
        // token de maquina (client_credentials) nao carrega papel nenhum
        if (role != null) {
            claims.put("role", role);
        }
        claims.put("aud", List.of(audiences));
        claims.put("scope", List.of(scopes));

        return Jwt.withTokenValue(tokenValue)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .issuer(issuer)
                .subject(subject)
                .audience(List.of(DEFAULT_AUDIENCES))
                .claims(c -> c.putAll(claims))
                .headers(h -> h.put("alg", "none"))
                .build();
    }

    private static Jwt buildDefaultJwt() {
        return buildJwt(DEFAULT_TOKEN_VALUE, DEFAULT_SUBJECT, DEFAULT_ISSUER_URI, DEFAULT_SCOPES, DEFAULT_ROLE, DEFAULT_AUDIENCES);
    }

    private static Jwt buildNoScopeJwt() {
        return buildJwt(NO_SCOPE_TOKEN_VALUE, DEFAULT_SUBJECT, DEFAULT_ISSUER_URI, new String[]{}, DEFAULT_ROLE, DEFAULT_AUDIENCES);
    }

    private static Jwt buildManagerJwt() {
        return buildJwt(MANAGER_TOKEN_VALUE, MANAGER_SUBJECT, DEFAULT_ISSUER_URI, DEFAULT_SCOPES, "MANAGER", DEFAULT_AUDIENCES);
    }

    private static Jwt buildMachineJwt() {
        return buildJwt(MACHINE_TOKEN_VALUE, DEFAULT_AUDIENCES[0], DEFAULT_ISSUER_URI, DEFAULT_SCOPES, null, DEFAULT_AUDIENCES);
    }

    private static Jwt buildNewCustomerJwt() {
        return buildJwt(NEW_CUSTOMER_TOKEN_VALUE, NEW_CUSTOMER_SUBJECT, DEFAULT_ISSUER_URI, DEFAULT_SCOPES, DEFAULT_ROLE, DEFAULT_AUDIENCES);
    }
}
