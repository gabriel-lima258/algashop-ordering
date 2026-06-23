package com.gtech.algashop.core.domain.model;

// essa classe indica que todos aggregate root devem expor suas identidades
// aggregate é a porta, repository salva e busca aggregate
// nunca persiste diretamente uma entidade
public interface AggregateRoot<ID> extends DomainEventSource {
    ID id();
}
