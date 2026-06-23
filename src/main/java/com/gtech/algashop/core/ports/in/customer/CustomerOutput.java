package com.gtech.algashop.core.ports.in.customer;

import com.gtech.algashop.core.ports.in.commons.AddressData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

// CQRS - Read Model (DTO de Projeção)
//
// Esta classe é o modelo de leitura do CQRS para Customer.
// Diferente da entidade de domínio (Customer), que carrega regras de negócio,
// validações e comportamento, este DTO é uma estrutura plana e simples,
// projetada exclusivamente para atender consultas da camada de apresentação.
//
// Características do Read Model no CQRS:
//   - Sem lógica de negócio: apenas dados para exibição
//   - Pode ser populado diretamente via JPQL (projeção no SELECT)
//   - A estrutura reflete o que o cliente (API/UI) precisa, não o modelo de domínio
//   - @AllArgsConstructor é essencial para que o JPA consiga instanciar via "SELECT new ..."
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerOutput {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String document;
    private LocalDate birthDate;
    private Boolean promotionNotificationsAllowed;
    private Integer loyaltyPoints;
    private OffsetDateTime registeredAt;
    private OffsetDateTime archivedAt;
    private Boolean archived;
    private AddressData address;
}
