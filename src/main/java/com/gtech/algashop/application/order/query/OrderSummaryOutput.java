package com.gtech.algashop.application.order.query;

import com.gtech.algashop.domain.model.order.OrderId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSummaryOutput {
    private String id;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private OffsetDateTime placedAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime readyAt;
    private OffsetDateTime canceledAt;
    private String status;
    private String paymentMethod;
    private CustomerMinimalOutput customer;

    // conversor de TSID long para String do output usando construtor

    public OrderSummaryOutput(Long id, Integer totalItems, BigDecimal totalAmount,
                              OffsetDateTime placedAt, OffsetDateTime paidAt, OffsetDateTime readyAt,
                              OffsetDateTime canceledAt, String status, String paymentMethod,
                              CustomerMinimalOutput customer) {
        this.id = new OrderId(id).toString();
        this.customer = customer;
        this.totalItems = totalItems;
        this.totalAmount = totalAmount;
        this.placedAt = placedAt;
        this.paidAt = paidAt;
        this.readyAt = readyAt;
        this.canceledAt = canceledAt;
        this.status = status;
        this.paymentMethod = paymentMethod;
    }
}
