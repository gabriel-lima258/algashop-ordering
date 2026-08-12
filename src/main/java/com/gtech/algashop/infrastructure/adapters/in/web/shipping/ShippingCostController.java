package com.gtech.algashop.infrastructure.adapters.in.web.shipping;

import com.gtech.algashop.core.application.shipping.ShippingApplicationService;
import com.gtech.algashop.core.application.shipping.ShippingCostPreviewInput;
import com.gtech.algashop.core.application.shipping.ShippingCostPreviewOutput;
import com.gtech.algashop.infrastructure.config.security.SecurityAnnotations;
import com.gtech.algashop.infrastructure.config.security.SecurityAnnotations.CanPreviewShippingCosts;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// POST, e nao GET, para uma consulta que nao muda nada. A escolha e discutivel e vale
// declarar: um GET /shipping-cost-previews?zipCode=12345 seria cacheavel pelo navegador e
// mais idiomatico para leitura. O POST foi mantido por consistencia com o corpo validado por
// @Valid - e fica registrado como decisao, nao como descuido.
//
// Sem @RequestMapping na classe: e o unico endpoint deste controller, entao o path completo
// fica no metodo.
@RestController
@RequiredArgsConstructor
public class ShippingCostController {

    private final ShippingApplicationService shippingApplicationService;

    @CanPreviewShippingCosts
    @PostMapping("/api/v1/shipping-cost-previews")
    public ShippingCostPreviewOutput previewShippingCost(@RequestBody @Valid ShippingCostPreviewInput input) {
        return shippingApplicationService.previewCost(input);
    }
}
