package com.gtech.algashop.domain.model.order;

import com.gtech.algashop.domain.model.commons.*;
import com.gtech.algashop.domain.model.product.ProductTestDataBuilder;
import com.gtech.algashop.domain.model.costumer.CustomerId;

import java.time.LocalDate;

import static com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder.DEFAULT_CUSTOMER_ID;

public class OrderTestDataBuilder {

    private CustomerId customerId = DEFAULT_CUSTOMER_ID;
    private PaymentMethod paymentMethod = PaymentMethod.GATEWAY_BALANCE;

    private Shipping shipping = aShipping();
    private Billing billing = aBilling();

    private boolean withItems = true;
    private OrderStatus status = OrderStatus.DRAFT;

    private CreditCardId creditCardId;

    private OrderTestDataBuilder() {
    }

    /////////////////////////////////////
    ///  BUILDER
    ////////////////////////////////////

    public static OrderTestDataBuilder anOrder() {
        return new OrderTestDataBuilder();
    }

    public Order build() {
        Order order =  Order.draft(customerId);
        order.changeShipping(shipping);
        order.changeBilling(billing);
        order.changePaymentMethod(paymentMethod, creditCardId);

        if (withItems) {
            order.addItem(ProductTestDataBuilder.aProduct().build(), new Quantity(1));
            order.addItem(ProductTestDataBuilder.aProductRamMemory().build(), new Quantity(1));
        }

        switch (this.status) {
            case DRAFT -> {
            }
            case PLACED -> {
                order.markAsPlaced();
            }
            case PAID -> {
                order.markAsPlaced();
                order.markAsPaid();
            }
            case READY -> {
                order.markAsPlaced();
                order.markAsPaid();
                order.markAsReady();
            }
            case CANCELED -> {
                order.markAsCanceled();
            }
        }

        return order;
    }

    /////////////////////////////////////
    ///  HELPERS
    ////////////////////////////////////

    public static Billing aBilling() {
        return Billing.builder()
                .address(anAddress())
                .fullName(new FullName("John", "Doe"))
                .phone(new Phone("123-111-9911"))
                .email(new Email("johndoe@gmail.com"))
                .document(new Document("255-09-1992"))
                .build();
    }

    public static Shipping aShipping() {
        return Shipping.builder()
                .address(anAddress())
                .cost(new Money("10"))
                .expectedDate(LocalDate.now().plusWeeks(1))
                .recipient(Recipient.builder()
                        .fullName(new FullName("John", "Doe"))
                        .phone(new Phone("123-111-9911"))
                        .document(new Document("255-09-1992"))
                        .build())
                .build();
    }

    public static Shipping aShippingAlt() {
        return Shipping.builder()
                .address(anAddressAlt())
                .cost(new Money("30"))
                .expectedDate(LocalDate.now().plusWeeks(2))
                .recipient(Recipient.builder()
                        .fullName(new FullName("Angel", "Robert"))
                        .phone(new Phone("123-111-0000"))
                        .document(new Document("210-01-1999"))
                        .build())
                .build();
    }

    public static Address anAddress() {
        return Address.builder()
                .street("Bourbon Street")
                .number("1134")
                .neighborhood("North Ville")
                .city("York")
                .state("South California")
                .zipCode(new ZipCode("12345"))
                .complement("Apt. 114")
                .build();
    }

    public static Address anAddressAlt() {
        return Address.builder()
                .street("German Street")
                .number("1234")
                .neighborhood("Green fields")
                .city("Napoli")
                .state("Rome")
                .zipCode(new ZipCode("12225"))
                .complement("House. 10")
                .build();
    }

    /////////////////////////////////////
    ///  SETTERS
    ////////////////////////////////////

    public OrderTestDataBuilder customerId(CustomerId customerId) {
        this.customerId = customerId;
        return this;
    }

    public OrderTestDataBuilder paymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public OrderTestDataBuilder shipping(Shipping shipping) {
        this.shipping = shipping;
        return this;
    }

    public OrderTestDataBuilder billing(Billing billing) {
        this.billing = billing;
        return this;
    }

    public OrderTestDataBuilder withItems(boolean withItems) {
        this.withItems = withItems;
        return this;
    }

    public OrderTestDataBuilder status(OrderStatus status) {
        this.status = status;
        return this;
    }

    public OrderTestDataBuilder creditCardId(CreditCardId creditCardId) {
        this.creditCardId = creditCardId;
        return this;
    }
}
