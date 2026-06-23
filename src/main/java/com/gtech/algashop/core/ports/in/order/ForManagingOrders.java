package com.gtech.algashop.core.ports.in.order;

public interface ForManagingOrders {
    void markAsCanceled(String orderId);
    void markAsPaid(String orderId);
    void markAsReady(String orderId);
}
