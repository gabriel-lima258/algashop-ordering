package com.gtech.algashop.core.ports.in.shoppingcart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingCartItemInput {
    @NotNull
    @Positive
    private Integer quantity;
    @NotNull
    private UUID productId;
    @JsonIgnore
    private UUID shoppingCartId;
}
