package com.gtech.algashop.core.application.checkout.command;

// Destinatario da entrega (parte do ShippingSnapshot), sem endereco - esse fica no pai.
public record RecipientSnapshot(String firstName, String lastName, String document, String phone) {}
