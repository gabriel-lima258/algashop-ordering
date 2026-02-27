package com.gtech.algashop.domain.model.service;

import com.gtech.algashop.domain.model.entity.VO.Product;
import com.gtech.algashop.domain.model.entity.VO.id.ProductId;

import java.util.Optional;

// aqui definimos como interface pois poderar ser usada em infra tambem!
// fazemos isso para que não precisamos implementar um repositorio para value object
public interface ProductCatalogService {
    Optional<Product> ofId(ProductId productId);
}
