package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.Specification;
import com.gtech.algashop.domain.model.costumer.Customer;
import com.gtech.algashop.domain.model.costumer.LoyaltyPoints;
import lombok.RequiredArgsConstructor;

import java.time.Year;

// Vantagens do padrão Specification:
// 1. Single Responsibility: a regra de frete grátis fica isolada, sem poluir o domain service (BuyNowService/CheckoutService).
// 2. Reutilização: qualquer serviço pode verificar frete grátis sem duplicar lógica.
// 3. Testabilidade: permite testar a regra de negócio de forma unitária, com valores parametrizáveis.
// 4. Configurabilidade: os limites (pontos, quantidade de vendas) são injetados via construtor,
//    facilitando ajustes sem alterar código — basta mudar a configuração de instanciação.
// 5. Composição: Specifications podem ser combinadas (and/or/not) para formar regras mais complexas.
//
// === REFATORAÇÃO: de monolítica para composição de Specifications ===
//
// ANTES (versão monolítica):
//   - Todos os campos primitivos viviam aqui: Orders orders, int minPoints, long salesQty, int premiumPoints
//   - O isSatisfiedBy() continha uma expressão booleana extensa e difícil de ler:
//       return loyaltyPoints >= 100 && salesQty >= 2 || loyaltyPoints >= 2000
//   - Problemas: violava SRP (múltiplas razões para mudar), impossível reutilizar cada sub-regra isoladamente,
//     difícil de testar cada condição separadamente.
//
// DEPOIS (versão composta — atual):
//   - Cada sub-regra virou uma Specification independente:
//       • CustomerHasEnoughLoyaltyPointsSpecification → verifica pontos de fidelidade (genérica, reutilizável)
//       • CustomerHasOrderedEnoughAtYearSpecification → verifica quantidade de compras no ano
//   - Esta classe agora apenas COMPÕE as sub-regras usando .and() e .or()
//   - Benefícios da refatoração:
//       a) Legibilidade: a regra de negócio se lê como linguagem natural (pontos AND compras OR pontosPremium)
//       b) Reutilização: CustomerHasEnoughLoyaltyPointsSpecification é usada 2x com limiares diferentes (básico e premium)
//       c) Testabilidade: cada sub-regra pode ser testada unitariamente de forma isolada
//       d) Open/Closed: novas regras podem ser adicionadas compondo com .and()/.or() sem modificar as existentes
@RequiredArgsConstructor
public class CustomerHaveFreeShippingSpecification implements Specification<Customer> {

    // Verifica se o cliente fez compras suficientes no ano (delega acesso ao repositório Orders internamente)
    private final CustomerHasOrderedEnoughAtYearSpecification hasOrderedEnoughAtYearSpecification;
    // Verifica pontos de fidelidade no nível básico (ex: >= 100 pontos) — usada na regra 1
    private final CustomerHasEnoughLoyaltyPointsSpecification hasEnoughBasicLoyaltyPointsSpecification;
    // Reutiliza a MESMA classe de pontos com limiar diferente (ex: >= 2000 pontos) — usada na regra 2
    // Isso demonstra o poder de Specifications parametrizáveis: uma classe, múltiplas instâncias com comportamentos distintos.
    private final CustomerHasEnoughLoyaltyPointsSpecification hasEnoughPremiumLoyaltyPointsSpecification;

    // Construtor de conveniência: recebe valores primitivos e monta as sub-Specifications internamente.
    // Isso mantém a API simples para quem instancia (ex: no @Bean ou no setUp() dos testes),
    // enquanto a composição interna fica encapsulada. Passamos os valores internos de spefication dentro do construtor
    public CustomerHaveFreeShippingSpecification(Orders orders,
                                                 LoyaltyPoints basicLoyaltyPoints,
                                                 long salesQuantityForFreeShipping,
                                                 LoyaltyPoints premiumLoyaltyPoints) {
        this.hasOrderedEnoughAtYearSpecification = new CustomerHasOrderedEnoughAtYearSpecification(orders, salesQuantityForFreeShipping);
        this.hasEnoughBasicLoyaltyPointsSpecification = new CustomerHasEnoughLoyaltyPointsSpecification(basicLoyaltyPoints);
        this.hasEnoughPremiumLoyaltyPointsSpecification = new CustomerHasEnoughLoyaltyPointsSpecification(premiumLoyaltyPoints);
    }

    /*
     * Regra de negócio: frete grátis quando:
     *   Regra 1: pontos básicos (>= 100) E compras suficientes no ano (>= 2)
     *   OU
     *   Regra 2: pontos premium (>= 2000), independente de compras
     *
     * ANTES (expressão monolítica — difícil de ler e manter):
     *   return customer.loyaltyPoints().compareTo(new LoyaltyPoints(minPoints)) >= 0
     *       && orders.salesQuantityByCustomerInYear(customer.id(), Year.now()) >= salesQty
     *       || customer.loyaltyPoints().compareTo(new LoyaltyPoints(premiumPoints)) >= 0;
     *
     * DEPOIS (composição fluente — lê-se como a regra de negócio):
     *   pontosBasicos .and(comprasSuficientes) .or(pontosPremium)
     *
     * A diferença é clara: o código ANTES exige que o leitor interprete compareTo, operadores lógicos
     * e precedência (&& antes de ||). O código DEPOIS expressa a intenção diretamente.
     */
    @Override
    public boolean isSatisfiedBy(Customer customer) {
        return hasEnoughBasicLoyaltyPointsSpecification
                .and(hasOrderedEnoughAtYearSpecification)
                .or(hasEnoughPremiumLoyaltyPointsSpecification)
                .isSatisfiedBy(customer);
    }
}
