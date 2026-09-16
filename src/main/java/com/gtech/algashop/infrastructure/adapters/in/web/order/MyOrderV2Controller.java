package com.gtech.algashop.infrastructure.adapters.in.web.order;

import com.gtech.algashop.core.application.security.SecurityCheckApplicationService;
import com.gtech.algashop.core.domain.model.costumer.CustomerNotFoundException;
import com.gtech.algashop.core.domain.model.product.ProductNotFoundException;
import com.gtech.algashop.core.domain.model.shoppingcart.ShoppingCartNotFound;
import com.gtech.algashop.core.ports.in.checkout.*;
import com.gtech.algashop.core.ports.in.order.ForQueryOrders;
import com.gtech.algashop.core.ports.in.order.OrderFilter;
import com.gtech.algashop.core.ports.out.order.OrderDetailOutput;
import com.gtech.algashop.core.ports.out.order.OrderSummaryOutput;
import com.gtech.algashop.infrastructure.adapters.in.web.PageModel;
import com.gtech.algashop.infrastructure.adapters.in.web.exceptionhandler.UnprocessableEntityException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.CanReadMyOrders;
import static com.gtech.algashop.infrastructure.config.security.check.SecurityAnnotations.CanWriteMyOrders;

// A borda do checkout ASSINCRONO. O POST nao cria o pedido: valida, monta o agregado
// em memoria, grava o COMANDO na outbox (mesma transacao) e responde 202 Accepted com
// o orderId futuro - o pedido passa a existir quando o comando for consumido (ate
// ~1min depois, o poll-interval do dispatcher). Consequencia honesta: um GET logo apos
// o 202 devolve 404 - e o contrato do assincrono, o cliente consulta pelo id ate
// aparecer. Os GETs sao iguais aos do V1 (leem o banco). As validacoes que DAO para
// falhar cedo (cliente/produto/carrinho inexistentes, seguranca) ainda falham no POST,
// como 422/403 - so a persistencia foi para o outro lado do topico.

@RestController
@RequestMapping(path = "/api/v2/customers/me/orders")
@RequiredArgsConstructor
public class MyOrderV2Controller {

    private final ForQueryOrders forQueryOrders;

    private final ForBuyingProductAsync forBuyingProduct;
    private final ForBuyingWithShoppingCartAsync forBuyingWithShoppingCart;

    private final SecurityCheckApplicationService securityCheck;

    @CanReadMyOrders
    @GetMapping
    public PageModel<OrderSummaryOutput> findAll(OrderFilter filter) {
        // o dono nao vem da query string: sem esta linha, ?customerId=<alheio> listaria
        // pedidos de outros clientes - o mesmo IDOR que este controller existe para evitar
        filter.setCustomerId(securityCheck.getAuthenticatedUserId());
        return PageModel.of(forQueryOrders.filter(filter));
    }

    @CanReadMyOrders
    @GetMapping("/{orderId}")
    public OrderDetailOutput findById(@PathVariable String orderId) {
        return forQueryOrders.findByIdAndCustomerId(orderId, securityCheck.getAuthenticatedUserId());
    }

    @CanWriteMyOrders
    @PostMapping(consumes = "application/vnd.order-with-product.v2+json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderAcceptedOutput createWithProduct(@RequestBody @Valid BuyNowInput input) {
        // setando o customer fixo via body, e não ser setado na requisição
        input.setCustomerId(securityCheck.getAuthenticatedUserId());
        String orderId;

        try {
            orderId = forBuyingProduct.buyNow(input);
        } catch (CustomerNotFoundException | ProductNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }

        return new OrderAcceptedOutput(orderId);
    }

    @CanWriteMyOrders
    @PostMapping(consumes = "application/vnd.order-with-shopping-cart.v2+json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderAcceptedOutput createWithShoppingCart(@Valid @RequestBody CheckoutInput input) {
        input.setCustomerId(securityCheck.getAuthenticatedUserId());
        String orderId;

        try {
            orderId = forBuyingWithShoppingCart.checkout(input);
        } catch (CustomerNotFoundException | ShoppingCartNotFound e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }

        return new OrderAcceptedOutput(orderId);
    }
}
