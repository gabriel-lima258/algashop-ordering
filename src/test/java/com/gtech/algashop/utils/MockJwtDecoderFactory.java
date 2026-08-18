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
public class MockJwtDecoderFactory {

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

    // O "sub" precisa ser um UUID: desde a Fase 24 o authorization server emite o id do
    // usuario ali, e o OAuth2SecurityCheckApplicationServiceImpl faz UUID.fromString(sub)
    // para alimentar a auditoria. Um "test-user" qualquer estoura IllegalArgumentException.
    public static final String DEFAULT_SUBJECT = "01912e0a-0000-7000-8000-000000000001";

    // A audiencia existe e e DIFERENTE do subject - e assim que se distingue token de
    // usuario de token de maquina (no client_credentials o sub e o proprio client_id,
    // que tambem aparece na aud).
    public static final String DEFAULT_AUDIENCE = "algashop-ordering";

    public static final String DEFAULT_TOKEN_VALUE = "fake.jwt.token";
    public static final String NO_SCOPE_TOKEN_VALUE = "fake.jwt.no-scope";
    public static final String EXPIRED_TOKEN_VALUE = "fake.jwt.expired";

    public static JwtDecoder createMockJwtDecoder() {
        JwtDecoder jwtDecoder = Mockito.mock(JwtDecoder.class);

        Mockito.when(jwtDecoder.decode(DEFAULT_TOKEN_VALUE))
                .thenReturn(buildDefaultJwt());
        Mockito.when(jwtDecoder.decode(NO_SCOPE_TOKEN_VALUE))
                .thenReturn(buildNoScopeJwt());
        Mockito.when(jwtDecoder.decode(EXPIRED_TOKEN_VALUE))
                .thenThrow(new JwtException("Token is expired"));


        return jwtDecoder;
    }

    public static Jwt buildJwt(String tokenValue, String subject, String issuer, String[] scopes) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(600);

    Map<String, Object> claims = new HashMap<>();
    claims.put("sub", subject);
    claims.put("iss", issuer);
    claims.put("aud", List.of(DEFAULT_AUDIENCE));

    if (scopes != null && scopes.length > 0) {
        claims.put("scope", String.join(" ", scopes));
    }

    return Jwt.withTokenValue(tokenValue)
            .issuedAt(now)
            .expiresAt(expiresAt)
            .issuer(issuer)
            .subject(subject)
            .audience(List.of(DEFAULT_AUDIENCE))
            .claims(c -> c.putAll(claims))
            .headers(h -> h.put("alg", "none"))
            .build();
    }

    private static Jwt buildDefaultJwt() {
        return buildJwt(DEFAULT_TOKEN_VALUE, DEFAULT_SUBJECT, DEFAULT_ISSUER_URI, DEFAULT_SCOPES);
    }

    private static Jwt buildNoScopeJwt() {
        return buildJwt(NO_SCOPE_TOKEN_VALUE, DEFAULT_SUBJECT, DEFAULT_ISSUER_URI, new String[]{});
    }
}
