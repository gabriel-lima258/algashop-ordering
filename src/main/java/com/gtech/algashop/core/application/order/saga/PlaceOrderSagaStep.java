package com.gtech.algashop.core.application.order.saga;

import com.gtech.algashop.core.application.saga.SagaStatus;

import java.util.Arrays;
import java.util.List;

public enum PlaceOrderSagaStep {
    PLACING_ORDER,
    INVOICING(PLACING_ORDER),
    RESERVING_STOCK(INVOICING),
    APPROVING_ORDER(RESERVING_STOCK),
    REFUNDING_INVOICE(RESERVING_STOCK),
    CANCELLING_ORDER(INVOICING, REFUNDING_INVOICE);

    private final List<PlaceOrderSagaStep> previousStatus;

    // pode receber 0 ou vários status
    PlaceOrderSagaStep(PlaceOrderSagaStep... previousStatus) {
        // converte os valores em lista
        this.previousStatus = Arrays.asList(previousStatus);
    }

    // status atual possui permissao para troca de status?
    public boolean canChangeTo(PlaceOrderSagaStep newStatus) {
        // Pega o status atual (this)
        PlaceOrderSagaStep currentStatus = this;
        // Olha a lista de previousStatus do novo status e verifica
        return newStatus.previousStatus.contains(currentStatus);
    }

    public boolean canNotChangeTo(PlaceOrderSagaStep newStatus) {
        return !canChangeTo(newStatus);
    }
}
