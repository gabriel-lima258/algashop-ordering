package com.gtech.algashop.core.application.shipping;

import com.gtech.algashop.core.domain.model.commons.Address;
import com.gtech.algashop.core.domain.model.commons.ZipCode;
import com.gtech.algashop.core.domain.model.order.shipping.OriginAddressService;
import com.gtech.algashop.core.domain.model.order.shipping.ShippingCostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Consulta de frete ANTES de existir pedido: o cliente digita o CEP na pagina do produto e
// quer saber quanto custa e quando chega.
//
// Sem @Transactional de proposito, e o motivo e o mesmo do InvoicePaymentTransactions no
// billing: este metodo faz uma chamada HTTP a transportadora e NAO escreve nada no banco.
// Uma transacao aqui seguraria uma conexao do pool durante toda a ida a Rapidex - e como
// esta e uma consulta de vitrine, o volume dela e alto justamente quando ninguem comprou
// ainda. Seria o caminho mais rapido para esgotar o pool com trafego que nao gera receita.
//
// A resiliencia nao aparece aqui: ela mora no ResilientShippingCostAPIClient, atras da porta
// ShippingCostService. Vale notar a consequencia, porque ela e contra-intuitiva - aquele
// client tem FALLBACK, entao este metodo nunca ve a Rapidex falhar. Se a transportadora
// estiver fora, o cliente recebe um frete estimado e nao um erro. Ver
// docs/01-arquitetura-design/resiliencia.md
@Service
@RequiredArgsConstructor
public class ShippingApplicationService {

    private final OriginAddressService originAddressService;
    private final ShippingCostService shippingCostService;

    public ShippingCostPreviewOutput previewCost(ShippingCostPreviewInput input) {
        Address originAddress = originAddressService.originAddress();

        var request = ShippingCostService.CalculationRequest.builder()
                .origin(originAddress.zipCode())
                .destination(new ZipCode(input.getZipCode()))
                .build();

        var result = shippingCostService.calculate(request);

        return new ShippingCostPreviewOutput(result.cost().money(), result.expectedDate());
    }
}
