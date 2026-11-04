package com.gtech.algashop.core.application.saga;


import java.util.Arrays;
import java.util.List;

// Status de vida de uma saga
public enum SagaStatus {
    RUNNING,
    COMPENSATING(RUNNING),
    SUCCEEDED(RUNNING),
    COMPENSATED(COMPENSATING);

    private final List<SagaStatus> previousStatus;

    // pode receber 0 ou vários status
    SagaStatus(SagaStatus... previousStatus) {
        // converte os valores em lista
        this.previousStatus = Arrays.asList(previousStatus);
    }

    // status atual possui permissao para troca de status?
    public boolean canChangeTo(SagaStatus newStatus) {
        // Pega o status atual (this)
        SagaStatus currentStatus = this;
        // Olha a lista de previousStatus do novo status e verifica
        return newStatus.previousStatus.contains(currentStatus);
    }

    public boolean canNotChangeTo(SagaStatus newStatus) {
        return !canChangeTo(newStatus);
    }
}
