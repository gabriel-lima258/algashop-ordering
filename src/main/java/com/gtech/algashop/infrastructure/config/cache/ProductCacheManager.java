package com.gtech.algashop.infrastructure.config.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

// Invalidacao do cache client-side de produto dirigida por EVENTO: quando o catalogo
// avisa via Kafka que o produto mudou, a entrada cacheada da API (o @Cacheable do
// ResilientProductCatalogAPIClient - mesmo cache name, key = productId) e removida, e
// a proxima leitura busca o dado novo. O TTL curto deixou de ser o unico mecanismo de
// invalidacao e virou a segunda linha de defesa, para o evento que se perder.
@Component
@Slf4j
@RequiredArgsConstructor
public class ProductCacheManager {

    public static final String PRODUCT_CATALOG_API_CACHE_NAME = "algashop:product-catalog-api:v1";

    private final CacheManager cacheManager;

    public void evict(UUID productId) {
        // a API programatica do Cache NAO passa pelo CacheInterceptor, entao o
        // ResilienceCacheErrorHandler do RedisCacheConfig nao protege esta chamada -
        // ele so intercepta as anotacoes @Cacheable/@CacheEvict. O try/catch cumpre
        // aqui o mesmo papel: Redis fora do ar nao pode derrubar o handler Kafka;
        // cache e otimizacao, e o TTL curto corrige a entrada que ficou velha
        try {
            Optional.ofNullable(cacheManager.getCache(PRODUCT_CATALOG_API_CACHE_NAME))
                    .ifPresent(cache -> cache.evictIfPresent(productId));
        } catch (Exception e) {
            log.warn("Cache evict failed for product {} | {}", productId, e.toString());
        }
    }
}
