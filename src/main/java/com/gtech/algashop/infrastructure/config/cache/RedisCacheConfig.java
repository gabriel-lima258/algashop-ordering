// =============================================================================
// CONFIGURAÇÃO DO CACHE REDIS
// =============================================================================
//
// Esta classe NÃO cria o cache do zero. O Spring Boot, ao ver o Redis no
// classpath e spring.cache.type=redis, já monta um RedisCacheManager sozinho.
// O que fazemos aqui é AJUSTAR esse manager que o Boot criou — dois ajustes:
// o formato das chaves e a política de valores nulos.
// =============================================================================

package com.gtech.algashop.infrastructure.config.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;

import java.time.Duration;

import static com.gtech.algashop.infrastructure.config.cache.ProductCacheManager.PRODUCT_CATALOG_API_CACHE_NAME;

// O que este serviço cacheia é diferente do que o product-catalog cacheia, e vale ter
// isso claro: aqui o cache é do LADO CLIENTE. O que entra no Redis é a resposta de uma
// chamada HTTP a outro microsserviço, não uma consulta ao banco próprio.
//
// A diferença prática está em quem invalida. No catálogo, quem escreve o produto é o
// mesmo serviço que o cacheia, então ele sabe exatamente quando a entrada ficou velha e
// usa @CacheEvict. Aqui o dado é de outro serviço — e desde a Fase 39 este serviço FICA
// sabendo quando ele muda: os eventos Kafka de produto disparam o evict programático
// (ProductCacheManager). O TTL curto deixou de ser o único mecanismo de invalidação e
// virou a segunda linha de defesa, para o evento que se perder no caminho.
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheConfig implements CachingConfigurer {

    @Autowired
    private ResilienceCacheErrorHandler resilienceCacheErrorHandler;

    // Sem este bean, uma queda do Redis derruba a criação de pedido — mesmo com o
    // product-catalog de pé. Ver ResilienceCacheErrorHandler.
    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return resilienceCacheErrorHandler;
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        var defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(c -> c + ":")
                .entryTtl(Duration.ofMinutes(1)); // invalidando e apagando dados antigos com TTL

        return (builder) -> builder
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration(PRODUCT_CATALOG_API_CACHE_NAME,
                        defaultCacheConfig.disableCachingNullValues().entryTtl(Duration.ofMinutes(5)));
    }
}
