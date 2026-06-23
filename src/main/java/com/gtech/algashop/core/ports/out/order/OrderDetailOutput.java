package com.gtech.algashop.core.ports.out.order;

import com.gtech.algashop.core.ports.in.order.BillingData;
import com.gtech.algashop.core.ports.in.order.ShippingData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderDetailOutput {
    private String id;
    private CustomerMinimalOutput customer;
    private Integer totalItems;
    private BigDecimal totalAmount;
    private OffsetDateTime placedAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime readyAt;
    private OffsetDateTime canceledAt;
    private String status;
    private String paymentMethod;
    private UUID creditCardId;
    private ShippingData shipping;
    private BillingData billing;

    private List<OrderItemOutput> items = new ArrayList<>();
}
