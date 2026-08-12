package com.gtech.algashop.infrastructure.adapters.out.web.product.client.http;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

// Os dois campos moram juntos de proposito: endereco e credencial de acesso ao catalogo
// sao a mesma decisao de integracao, e separa-los faria um poder mudar sem o outro.
//
// CUIDADO com o @NotBlank: esta classe e @Component incondicional, entao os dois campos
// passam a ser obrigatorios em TODO perfil - inclusive no de teste, que e uma arvore de
// configuracao separada. Foi o que quebrou a suite quando o oauth2ClientRegistrationId
// entrou; ver application-test-env.yaml.
@Component
@Validated
@Data
@ConfigurationProperties(prefix = "algashop.integrations.product-catalog")
public class ProductCatalogIntegrationProperties {

    @NotBlank
    private String url;

    @NotBlank
    private String oauth2ClientRegistrationId;
}
