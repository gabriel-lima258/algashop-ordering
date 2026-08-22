package contracts.shoppingcart

import org.springframework.cloud.contract.spec.Contract

// O corpo nao carrega shoppingCartId: o controller resolve o carrinho pelo token.
Contract.make {
    request {
        method POST()
        urlPath("/api/v1/customers/me/shopping-cart/items")
        headers {
            contentType("application/json")
        }
        body([
                productId: value(
                        test("a1b2c3d4-e5f6-7890-abcd-ef1234567890"),
                        stub(anyUuid())
                ),
                quantity: value(
                        test(2),
                        stub(anyPositiveInt())
                )
        ])
    }
    response {
        status 204
    }
}
