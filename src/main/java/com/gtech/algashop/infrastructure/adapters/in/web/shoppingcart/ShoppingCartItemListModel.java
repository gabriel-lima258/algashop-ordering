package com.gtech.algashop.infrastructure.adapters.in.web.shoppingcart;

import com.gtech.algashop.core.ports.in.shoppingcart.ShoppingCartItemOutput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingCartItemListModel {
    private List<ShoppingCartItemOutput> items = new ArrayList<>();
}
