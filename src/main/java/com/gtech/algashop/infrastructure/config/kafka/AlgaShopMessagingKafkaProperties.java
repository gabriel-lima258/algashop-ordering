package com.gtech.algashop.infrastructure.config.kafka;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

// Copia deliberada da classe homonima do product-catalog: os servicos nao compartilham
// jar (decisao antiga do projeto - acoplamento por biblioteca e pior que duplicacao),
// entao cada lado declara sua propria propriedade com o MESMO valor. O que liga os dois
// nao e codigo compartilhado, e a string do nome do topico nos dois application.yml.
// O @NotBlank cumpre o mesmo papel dos dois lados: sem nome de topico, nem sobe.
@Component
@Validated
@Data
@ConfigurationProperties("algashop.messaging.kafka")
public class AlgaShopMessagingKafkaProperties {

    @NotBlank
    private String productEventTopicName;
}
