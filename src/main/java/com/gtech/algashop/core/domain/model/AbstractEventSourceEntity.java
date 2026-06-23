package com.gtech.algashop.core.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*
 * "Essa entidade gera eventos de domínio."
 * Essa classe é um base class para entidades do domínio que geram eventos.
 * Assim você não precisa reimplementar a lógica de eventos em toda entidade.
 * Por que protected?
 * - Porque só a própria entidade deve gerar eventos.
 * Por abstrata?
 * - Porque ela NÃO faz sentido sozinha, ela foi feita para ser herdada de aggregates
 * - Foi feita para ser herdada e compartilhar partes de codigo, diferente de interface
 */
public abstract class AbstractEventSourceEntity implements DomainEventSource {
    protected final List<Object> domainEvents = new ArrayList<>();

    // Permite que a própria entidade registre um evento.
    protected void publishDomainEvent(Object event) {
        Objects.requireNonNull(event);
        this.domainEvents.add(event);
    }


    @Override
    public List<Object> domainEvents() {
        // Protege a lista contra alterações externas.
        return Collections.unmodifiableList(this.domainEvents);
    }

    @Override
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
}
