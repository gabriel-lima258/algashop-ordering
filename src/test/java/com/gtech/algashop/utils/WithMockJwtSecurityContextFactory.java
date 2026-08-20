package com.gtech.algashop.utils;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

@Component
public class WithMockJwtSecurityContextFactory implements WithSecurityContextFactory<WithMockJwt> {

    // transforma uma jwt em um authentication
    private final JwtAuthenticationConverter jwtAuthenticationConverter;

    public WithMockJwtSecurityContextFactory(JwtAuthenticationConverter jwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    @Override
    public SecurityContext createSecurityContext(WithMockJwt annotation) {
        // 1 - cria o jwt
        Jwt jwt = MockJwtFactory.buildJwt(
                "mock-value",
                annotation.subject(),
                MockJwtFactory.DEFAULT_ISSUER_URI,
                annotation.scopes(),
                annotation.role(),
                annotation.audiences()
        );

        // 2 - converte jwt em authentication valido em spring
        AbstractAuthenticationToken tokenAuthenticated = jwtAuthenticationConverter.convert(jwt);

        // 3 - carregamos o authentication com o jwt
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(tokenAuthenticated);

        return context;
    }
}
