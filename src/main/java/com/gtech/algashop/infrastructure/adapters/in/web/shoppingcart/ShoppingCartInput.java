package com.gtech.algashop.infrastructure.adapters.in.web.shoppingcart;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartInput {
    @NotNull
    private UUID customerId;
}
