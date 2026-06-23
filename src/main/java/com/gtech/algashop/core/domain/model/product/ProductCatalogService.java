package com.gtech.algashop.core.domain.model.product;

import java.util.Optional;

// aqui definimos como interface pois poderar ser usada em infra tambem!
// fazemos isso para que não precisamos implementar um repositorio para value object
public interface ProductCatalogService {
    Optional<Product> ofId(ProductId productId);
}
