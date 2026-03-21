package contracts.order

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    request {
        method GET()
        headers {
            accept "application/json"
        }
        url("/api/v1/orders") {
            queryParameters {
                parameter("size", value(
                        stub(optional(anyNumber())),
                        test(10)
                ))
                parameter("number", value(
                        stub(optional(anyNumber())),
                        test(0)
                ))
            }
        }
        response {
            status 200
            headers {
                contentType "application/json"
            }
            body([
                    size: fromRequest().query("size"),
                    number: 0,
                    totalElements: 2,
                    totalPages: 1,
                    content: [
                        [
                            id: "01226N0640J7Q",
                            customer: [
                                    id: anyUuid(),
                                    firstName: "John",
                                    lastName: "Doe",
                                    document: "12345",
                                    email: "johndoe@email.com",
                                    phone: "1191234564"
                            ],
                            totalItems: 2,
                            totalAmount: 41.98,
                            placedAt: anyIso8601WithOffset(),
                            canceledAt: null,
                            paidAt: null,
                            readyAt: null,
                            status: "PLACED",
                            paymentMethod: "GATEWAY_BALANCE"
                        ]
                    ]
            ])
        }
    }
}
