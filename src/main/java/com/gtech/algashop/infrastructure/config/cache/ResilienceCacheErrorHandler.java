package com.gtech.algashop.infrastructure.config.cache;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.stereotype.Component;

// O que fazer quando o CACHE falha - pergunta diferente de "o que fazer quando o
// product-catalog falha".
//
// Sem este handler, o padrao do Spring e propagar a excecao. E aqui isso seria pior
// que no lado do catalogo, porque o que esta cacheado e uma CHAMADA HTTP: se o Redis
// cai, a excecao sobe pelo proxy do @Cacheable, passa pelo ProductCatalogServiceHttpImpl
// e chega ao caso de uso que monta o pedido - sem que a chamada ao catalogo tenha
// sequer sido tentada. O catalogo estaria de pe, respondendo normalmente, e o pedido
// falharia mesmo assim.
//
// Um cache indisponivel tem que significar "vou buscar na origem", nao "desisti". Por
// isso os quatro metodos engolem e seguem: a chamada HTTP acontece, so que sem o
// atalho. FAIL-OPEN - degrada em latencia, nao em disponibilidade.
//
// Espelha o ResilienceCacheErrorHandler do product-catalog de proposito: os dois
// servicos compartilham o mesmo Redis, entao uma queda dele atinge os dois ao mesmo
// tempo, e nao faria sentido um degradar e o outro cair.
//
// WARN para falha de infraestrutura (Redis reiniciando, rede oscilando) - some sozinho,
// e stacktrace ali so vira ruido durante a indisponibilidade. ERROR com stacktrace para
// SerializationException no PUT, que e problema de CODIGO: alguem tentou cachear algo
// que nao implementa Serializable. Isso nao melhora sozinho e vai repetir em toda
// escrita ate a classe ser corrigida - e o stacktrace e a unica forma de descobrir qual
// campo da arvore de objetos nao e serializavel.
@Component
@Slf4j
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class ResilienceCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        logWarn(exception, cache, key, "GET");
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, @Nullable Object value) {
        if (exception instanceof SerializationException) {
            logError(exception, cache, key, "PUT");
        } else {
            logWarn(exception, cache, key, "PUT");
        }
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logWarn(exception, cache, key, "EVICT");
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        logWarn(exception, cache, "", "CLEAR");
    }

    private void logWarn(RuntimeException exception, Cache cache, Object key, String method) {
        log.warn("Cache {} error | cache '{}' | key '{}' | cause '{}'",
                method, cache.getName(), key, exception.getClass().getSimpleName());
    }

    private void logError(RuntimeException exception, Cache cache, Object key, String method) {
        log.error("Cache {} error | cache '{}' | key '{}' | cause '{}'",
                method, cache.getName(), key, exception.getClass().getSimpleName(), exception);
    }
}
