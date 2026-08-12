package com.gtech.algashop.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
// Resource server: quem EXIGE e VALIDA o token. O authorization server emite; este
// serviço nunca vê senha nem segredo, só a chave publica que baixa do /oauth2/jwks.
//
// @EnableMethodSecurity liga o @PreAuthorize das meta-anotacoes de SecurityAnnotations.
// Sem ele as anotacoes ficam no codigo sem efeito nenhum - e nada avisa.

// Ordering é client porque faz requisição para product mas também é resource de authorization-server pois fornece api
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class OrderingSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/actuator/health/**").permitAll()
                    .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
            .oauth2Client(Customizer.withDefaults());

        return http.build();
    }
}
