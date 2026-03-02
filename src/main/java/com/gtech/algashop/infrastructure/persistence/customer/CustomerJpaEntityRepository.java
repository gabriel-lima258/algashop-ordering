package com.gtech.algashop.infrastructure.persistence.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

// CQRS - Repositório Unificado (Command + Query)
//
// Este repositório combina dois papéis do CQRS:
//
//   1. Command Side (JpaRepository): métodos herdados como save(), delete(), findById()
//      são usados pelo modelo de domínio para persistir e recuperar agregados completos.
//
// Em uma implementação simplificada de CQRS (mesmo banco de dados), é comum
// ter o mesmo repositório atendendo ambos os lados. Em implementações mais avançadas,
// o lado de leitura poderia apontar para um banco separado otimizado para consultas.
public interface CustomerJpaEntityRepository
        extends JpaRepository<CustomerPersistenceEntity, UUID> {
    Optional<CustomerPersistenceEntity> findByEmail(String email);
    boolean existsByEmailAndIdNot(String email, UUID customerId);
}
