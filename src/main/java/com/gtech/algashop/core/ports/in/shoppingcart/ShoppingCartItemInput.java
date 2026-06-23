package com.gtech.algashop.core.ports.in.shoppingcart;

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
    private Integer quantity;
    private UUID productId;
    private UUID shoppingCartId;
}
