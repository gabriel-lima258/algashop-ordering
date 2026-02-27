package com.gtech.algashop.domain.model.shoppingcart;

public class ShoppingCartNotFound extends RuntimeException {
  public ShoppingCartNotFound(String message) {
    super(message);
  }
}
