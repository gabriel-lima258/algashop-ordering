package contracts.shoppingcart

import org.springframework.cloud.contract.spec.Contract

// O carrinho e sempre o do cliente autenticado: nenhum id no path.
// O customerId da resposta e o sub do token, que a base mocka fixo.
Contract.make {
    request {
        method GET()
        urlPath("/api/v1/customers/me/shopping-cart")
    }
    response {
        status 200
        headers {
            contentType('application/json')
        }
        body([
                id: anyUuid(),
                customerId: "6e148bd5-47f6-4022-b9da-07cfaa294f7a",
                totalItems: 3,
                totalAmount: 1250.00,
                items: [
                        [
                                id: anyUuid(),
                                productId: anyUuid(),
                                name: "Notebook",
                                price: 500.00,
                                quantity: 2,
                                totalAmount: 1000.00,
                                available: anyBoolean()
                        ],
                        [
                                id: anyUuid(),
                                productId: anyUuid(),
                                name: "Mouse pad",
                                price: 250.00,
                                quantity: 1,
                                totalAmount: 250.00,
                                available: anyBoolean()
                        ]
                ]
        ])
    }
}
