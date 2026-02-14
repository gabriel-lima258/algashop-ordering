package com.gtech.algashop.domain.entity;

import java.util.Arrays;
import java.util.List;

// enum com status de transição
public enum OrderStatus {
    DRAFT,
    PLACED(DRAFT), // so pode vim de draft
    PAID(PLACED), // so pode vim de placed
    READY(PAID), // so pode vim de paid
    CANCELED(DRAFT, PLACED, PAID, READY); // pode vim de todos

    private final List<OrderStatus> previousStatus;

    // pode receber 0 ou vários status
    OrderStatus(OrderStatus... previousStatus) {
        // converte os valores em lista
        this.previousStatus = Arrays.asList(previousStatus);
    }

    /*
    * status atual possui permissao para troca de status?
    */
    public boolean canChangeTo(OrderStatus newStatus) {
        // Pega o status atual (this)
        OrderStatus currentStatus = this;
        // Olha a lista de previousStatus do novo status e verifica
        return newStatus.previousStatus.contains(currentStatus);
    }

    public boolean canNotChangeTo(OrderStatus newStatus) {
        return !canChangeTo(newStatus);
    }
}
