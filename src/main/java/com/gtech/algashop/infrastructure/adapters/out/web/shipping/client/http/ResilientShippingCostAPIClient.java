package com.gtech.algashop.infrastructure.adapters.out.web.shipping.client.http;

import com.gtech.algashop.core.domain.model.order.shipping.ShippingCostService.*;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.BadGatewayException;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.GatewayTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryCircuitBreaker;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfig;
import org.springframework.cloud.circuitbreaker.retry.FrameworkRetryConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.core.retry.RetryException;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.net.SocketTimeoutException;

import static com.gtech.algashop.infrastructure.config.resilience.SpringCircuitBreakerConfig.rapidexCBId;

@Component
@Slf4j
public class ResilientShippingCostAPIClient {

    private final RapiDexAPICLient rapiDexAPICLient;
    private final FrameworkRetryCircuitBreaker circuitBreaker;

    public ResilientShippingCostAPIClient(RapiDexAPICLient rapiDexAPICLient,
                                            CircuitBreakerFactory<FrameworkRetryConfig, FrameworkRetryConfigBuilder> circuitBreakerFactory) {
        this.rapiDexAPICLient = rapiDexAPICLient;
        this.circuitBreaker = (FrameworkRetryCircuitBreaker) circuitBreakerFactory.create(rapidexCBId);
    }

    @ConcurrencyLimit(15) // bulkhead: no maximo 15 threads aqui dentro; as demais BLOQUEIAM
    public DeliveryCostResponse calculate(DeliveryCostRequest request) {
        log.info("Trying connection with rapidex");
        log.info("RapidexAPI CircuitBreaker state is {}", circuitBreaker.getCircuitBreakerPolicy().getState());

        try {
            DeliveryCostResponse response = circuitBreaker.run(
                    () -> doCalculate(request),
                    ex -> doInternalFallback(request, ex)
            );
            if (response == null) {
                throw new BadGatewayException.ClientErrorException("Invalid zip code provided");
            }
            return response;
        } catch (NoFallbackAvailableException e) {
            throw unwrapException(e);
        }
    }

    // resposta do fallback
    private DeliveryCostResponse doInternalFallback(DeliveryCostRequest request, Throwable exThrowable) {
        log.warn("Rapidex API Client failed for request {}", request, exThrowable);
        return new DeliveryCostResponse("20.0", 10L);
    }

    private RuntimeException unwrapException(NoFallbackAvailableException e) {
        Throwable cause = (e.getCause() instanceof RetryException re) ? re.getCause() : e.getCause();

        return switch (cause) {
            case GatewayTimeoutException gte -> gte;
            case BadGatewayException bge -> bge; // pega tambem Server/ClientErrorException
            case null, default -> e;
        };
    }

    private DeliveryCostResponse doCalculate(DeliveryCostRequest request) {
        log.info("Loading rapidex api {}", request);
        try {
            return rapiDexAPICLient.calculate(request);
        }
        catch (HttpClientErrorException e) {
            if (!(e instanceof HttpClientErrorException.NotFound)) {
                log.warn("Client Error when loading delivery cost {}", request, e);
            }
            return null;
        } catch (RestClientException e) {
            throw translateException(e);
        }
    }

    private RuntimeException translateException(RestClientException e) {
        if (e.getCause() instanceof SocketTimeoutException
                || e instanceof ResourceAccessException) {
            return new GatewayTimeoutException("Rapidex API Timeout", e);
        }

        if (e instanceof HttpClientErrorException) {
            return new BadGatewayException.ClientErrorException("Rapidex API Bad Gateway", e);
        }

        if (e instanceof HttpServerErrorException) {
            return new BadGatewayException.ServerErrorException("Rapidex API Bad Gateway", e);
        }

        return new BadGatewayException("Rapidex API Bad Gateway", e);
    }



}
