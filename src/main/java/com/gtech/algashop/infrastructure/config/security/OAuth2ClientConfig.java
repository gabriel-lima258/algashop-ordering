package com.gtech.algashop.infrastructure.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

// O ordering tem papel DUPLO no OAuth2, e as duas metades sao independentes:
//   - resource server: EXIGE token de quem o chama       (OrderingSecurityConfig)
//   - client:          PEDE token para chamar o catalogo (esta classe + o interceptor)
//
// Aqui mora so a peca do meio. Sao tres, e confundi-las e o motivo mais comum de
// "configurei tudo e nao vai":
//   1. o registration do YAML descreve COMO obter o token (url, id, segredo, escopo)
//   2. o AuthorizedClientManager EXECUTA o client_credentials e guarda o resultado
//   3. o OAuth2ClientHttpRequestInterceptor ANEXA o header em cada request
// Sem a 3, tudo esta configurado e nenhuma chamada leva Authorization.
@Configuration
public class OAuth2ClientConfig {

    // AuthorizedClientService..., e nao o Default...: o Default resolve o cliente
    // autorizado a partir da requisicao HTTP em curso (HttpServletRequest e o
    // SecurityContext da thread). Aqui nao ha requisicao de usuario envolvida - e uma
    // chamada de maquina para maquina, que tambem precisa funcionar em job agendado e em
    // pool assincrono. A variante de servico guarda o token num
    // OAuth2AuthorizedClientService, sem depender de contexto web nenhum.
    @Bean
    public OAuth2AuthorizedClientManager auth2AuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService oAuth2AuthorizedClientService
    ) {
        // So client_credentials: e o unico grant que este servico usa. O provider tambem
        // e quem RENOVA - antes de devolver um cliente autorizado ele confere a expiracao
        // (com uma folga de relogio) e, se estiver vencido, busca outro token. E por isso
        // que o TTL de 5 minutos do registration nao custa uma ida a cada requisicao.
        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, oAuth2AuthorizedClientService);

        manager.setAuthorizedClientProvider(provider);

        return manager;
    }
}
