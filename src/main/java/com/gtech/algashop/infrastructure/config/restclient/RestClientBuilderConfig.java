package com.gtech.algashop.infrastructure.config.restclient;

import org.springframework.boot.restclient.autoconfigure.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

// builder para instanciar o rest client com load balance e outro padrão sem load

@Configuration
public class RestClientBuilderConfig {

    @Bean
    @Primary
    public RestClient.Builder restClientBuilder(RestClientBuilderConfigurer configurer) {
        // configurer aplica a configurações client padrão do spring
        return configurer.configure(RestClient.builder());
    }

    // habilita server side server discovery junto com load balance
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder(RestClientBuilderConfigurer configurer) {
        // configurer aplica a configurações client padrão do spring
        return configurer.configure(RestClient.builder());
    }

}
