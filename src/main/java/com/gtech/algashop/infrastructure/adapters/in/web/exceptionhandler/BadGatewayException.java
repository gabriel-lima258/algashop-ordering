package com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler;

// As duas aninhadas ESTENDEM BadGatewayException - antes estendiam RuntimeException direto.
// Parecia detalhe, mas quebrava dois pontos ao mesmo tempo:
//   1. o @ExceptionHandler(BadGatewayException.class) do ApiExceptionHandler nao pegava
//      nenhuma das duas, entao um 5xx do catalogo caia no handler generico e virava 500;
//   2. o desempacotamento do ResilientProductCatalogAPIClient testa
//      "instanceof BadGatewayException" - tambem nao casava.
// Resultado: o cenario MAIS comum de falha (catalogo respondendo 5xx) era o unico mal
// mapeado. Agora as tres formas viram 502.
//
// A separacao em Server/Client continua valendo porque a RetryPolicy do
// SpringCircuitBreakerConfig so retenta ServerErrorException: repetir um 401 ou um 400
// daria o mesmo resultado quatro vezes.
public class BadGatewayException extends RuntimeException {
    public BadGatewayException() {
    }

    public BadGatewayException(String message) {
        super(message);
    }

    public BadGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class ServerErrorException extends BadGatewayException {
        public ServerErrorException() {
        }

        public ServerErrorException(String message) {
            super(message);
        }

        public ServerErrorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ClientErrorException extends BadGatewayException {
        public ClientErrorException() {
        }

        public ClientErrorException(String message) {
            super(message);
        }

        public ClientErrorException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
