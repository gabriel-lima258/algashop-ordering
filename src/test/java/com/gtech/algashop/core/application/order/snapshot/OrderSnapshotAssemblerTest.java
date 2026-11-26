package com.gtech.algashop.core.application.order.snapshot;

import com.gtech.algashop.core.domain.model.commons.Address;
import com.gtech.algashop.core.domain.model.order.Billing;
import com.gtech.algashop.core.domain.model.order.Order;
import com.gtech.algashop.core.domain.model.order.OrderStatus;
import com.gtech.algashop.core.domain.model.order.OrderTestDataBuilder;
import com.gtech.algashop.core.domain.model.order.Shipping;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

// Unitario puro: os snapshots sao records com varios String em sequencia, entao o
// compilador nao percebe um argumento trocado de lugar. Este teste confere CAMPO A CAMPO
// contra o agregado - foi assim que o telefone chegou ao billing no campo de e-mail e o
// complemento no lugar do numero, e o IssueInvoice caiu na DLT por validacao.
class OrderSnapshotAssemblerTest {

    private final OrderSnapshotAssembler assembler = new OrderSnapshotAssembler();

    @Test
    void shouldMapBillingFieldsToTheirOwnPlaces() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        Billing billing = order.billing();

        BillingSnapshot snapshot = assembler.toSnapshot(order).billing();

        Assertions.assertThat(snapshot.firstName()).isEqualTo(billing.fullName().firstName());
        Assertions.assertThat(snapshot.lastName()).isEqualTo(billing.fullName().lastName());
        Assertions.assertThat(snapshot.document()).isEqualTo(billing.document().document());
        Assertions.assertThat(snapshot.email()).isEqualTo(billing.email().email());
        Assertions.assertThat(snapshot.phone()).isEqualTo(billing.phone().phone());
        assertAddress(snapshot.address(), billing.address());
    }

    @Test
    void shouldMapShippingFieldsToTheirOwnPlaces() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        Shipping shipping = order.shipping();

        ShippingSnapshot snapshot = assembler.toSnapshot(order).shipping();

        Assertions.assertThat(snapshot.cost()).isEqualTo(shipping.cost().money());
        Assertions.assertThat(snapshot.expectedDate()).isEqualTo(shipping.expectedDate());
        Assertions.assertThat(snapshot.recipient().firstName()).isEqualTo(shipping.recipient().fullName().firstName());
        Assertions.assertThat(snapshot.recipient().lastName()).isEqualTo(shipping.recipient().fullName().lastName());
        Assertions.assertThat(snapshot.recipient().document()).isEqualTo(shipping.recipient().document().document());
        Assertions.assertThat(snapshot.recipient().phone()).isEqualTo(shipping.recipient().phone().phone());
        assertAddress(snapshot.address(), shipping.address());
    }

    // O mesmo Bean Validation que o billing aplica ao receber o comando: um snapshot
    // montado de um pedido valido nao pode ter violacao (e-mail mal formado, numero nulo...).
    @Test
    void shouldProduceSnapshotsThatPassBeanValidation() {
        Order order = OrderTestDataBuilder.anOrder().status(OrderStatus.PLACED).build();
        OrderSnapshot snapshot = assembler.toSnapshot(order);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Assertions.assertThat(validator.validate(snapshot.billing())).isEmpty();
            Assertions.assertThat(validator.validate(snapshot.shipping())).isEmpty();
        }
    }

    private void assertAddress(AddressSnapshot snapshot, Address address) {
        Assertions.assertThat(snapshot.street()).isEqualTo(address.street());
        Assertions.assertThat(snapshot.number()).isEqualTo(address.number());
        Assertions.assertThat(snapshot.complement()).isEqualTo(address.complement());
        Assertions.assertThat(snapshot.neighborhood()).isEqualTo(address.neighborhood());
        Assertions.assertThat(snapshot.city()).isEqualTo(address.city());
        Assertions.assertThat(snapshot.state()).isEqualTo(address.state());
        Assertions.assertThat(snapshot.zipCode()).isEqualTo(address.zipCode().zipcode());
    }
}
