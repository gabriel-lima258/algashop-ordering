package com.gtech.algashop.utils;

import com.gtech.algashop.infrastructure.config.security.token.JwtGrantedAuthoritiesDelegatingConverter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Constroi autenticacoes de teste do jeito que o servidor as constroi de verdade.
 *
 * POR QUE NAO UM MOCK DO SecurityCheckApplicationService
 * Mockar a porta faria o teste afirmar apenas "quando eu digo que e um CUSTOMER, o servico
 * trata como CUSTOMER" - uma tautologia. Aqui o Jwt e montado com os MESMOS claims que o
 * JwtTokenCustomizer do authorization server escreve (sub, aud, scope, role) e passa pelo
 * MESMO JwtGrantedAuthoritiesDelegatingConverter que roda em producao. Se o prefixo ROLE_
 * mudar de lado, ou o converter parar de somar o papel, estes testes acusam.
 *
 * O que continua sendo de mentira e apenas a ASSINATURA: nenhum token e verificado, porque
 * o objeto Jwt ja sai pronto. Autenticacao real so acontece contra o servidor rodando.
 */
public class TestAuthentications {

    private static final JwtGrantedAuthoritiesDelegatingConverter CONVERTER =
            new JwtGrantedAuthoritiesDelegatingConverter();

    public static AbstractAuthenticationToken customer(UUID customerId) {
        return authentication(customerId.toString(), "CUSTOMER");
    }

    public static AbstractAuthenticationToken manager(UUID userId) {
        return authentication(userId.toString(), "MANAGER");
    }

    /** Token de maquina: sub == aud (client_credentials) e sem papel nenhum. */
    public static AbstractAuthenticationToken machine() {
        return authentication(Arrays.stream(MockJwtFactory.DEFAULT_AUDIENCES).findFirst().get(), null);
    }

    public static void authenticateAsCustomer(UUID customerId) {
        SecurityContextHolder.getContext().setAuthentication(customer(customerId));
    }

    public static void clear() {
        SecurityContextHolder.clearContext();
    }

    private static AbstractAuthenticationToken authentication(String subject, String role) {
        Instant now = Instant.now();
        Jwt.Builder builder = Jwt.withTokenValue("fake.jwt.token")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(600))
                .issuer(MockJwtFactory.DEFAULT_ISSUER_URI)
                .subject(subject)
                .audience(List.of(MockJwtFactory.DEFAULT_AUDIENCES))
                .claim("scope", String.join(" ", MockJwtFactory.DEFAULT_SCOPES))
                .header("alg", "none");

        if (role != null) {
            builder.claim("role", role);
        }

        Jwt jwt = builder.build();
        return new JwtAuthenticationToken(jwt, CONVERTER.convert(jwt));
    }
}
