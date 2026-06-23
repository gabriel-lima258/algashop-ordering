package com.gtech.algashop.infrastructure.adapters.in.listerner.shoppingcart;

import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartCreatedEvent;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartEmptiedEvent;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartItemAddedEvent;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartItemRemovedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// classe de escuta dos eventos publicos em customer
@Slf4j
@Component
@RequiredArgsConstructor
public class ShoppingCartEventListener {

    @EventListener
    public void listen(ShoppingCartCreatedEvent event) {
        log.info("ShoppingCartCreatedEvent listen 1");
    }

    @EventListener
    public void listen(ShoppingCartEmptiedEvent event) {
        log.info("ShoppingCartEmptiedEvent listen 1");
    }

    @EventListener
    public void listen(ShoppingCartItemAddedEvent event) {
        log.info("ShoppingCartItemAddedEvent listen 1");
    }

    @EventListener
    public void listen(ShoppingCartItemRemovedEvent event) {
        log.info("ShoppingCartItemRemovedEvent listen 1");
    }
}
