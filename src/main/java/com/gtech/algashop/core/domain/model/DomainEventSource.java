package com.gtech.algashop.core.domain.model;

import java.util.List;

/*
* "Essa entidade gera eventos de domínio."
* Ou seja, qualquer classe que implementar isso:
* - Pode expor os eventos que gerou
* - Pode limpar esses eventos depois que forem publicados
*/
public interface DomainEventSource {
    List<Object> domainEvents();
    // Serve para evitar publicar o mesmo evento duas vezes.
    void clearDomainEvents();
}
