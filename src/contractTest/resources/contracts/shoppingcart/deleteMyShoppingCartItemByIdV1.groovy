package contracts.shoppingcart

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method DELETE()
        urlPath("/api/v1/customers/me/shopping-cart/items/a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    }
    response {
        status 204
    }
}
