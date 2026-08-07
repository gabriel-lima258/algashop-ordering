package com.gtech.algashop.core.application.shipping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// A validacao na BORDA e o que impede uma requisicao malformada de virar chamada de rede.
// Vale contar como parte da resiliencia: CEP invalido rejeitado aqui custa um 400 imediato;
// deixado passar, custa uma ida a Rapidex, um 400 dela, e - se o circuito estiver contando -
// uma falha que nao era culpa da transportadora.
//
// O mesmo @Size(5,5) foi acrescentado ao AddressData nesta leva, pelo mesmo motivo.
@Data
public class ShippingCostPreviewInput {

    @NotBlank
    @Size(min = 5, max = 5)
    private String zipCode;
}
