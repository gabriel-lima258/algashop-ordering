package com.gtech.algashop.application.order.query;

import com.gtech.algashop.application.util.SortablePageFilter;
import lombok.*;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Filtro concreto para consultas paginadas de pedidos.
// Herda page/size de PageFilter e sortByProperty/sortDirection de SortablePageFilter.
// Os campos abaixo representam critérios opcionais de busca — cada campo não-nulo
// se transforma em uma cláusula WHERE na Criteria API (AND entre si).
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderFilter extends SortablePageFilter<OrderFilter.SortType> {

    // Filtros opcionais de busca — campos nulos são ignorados na construção da query
    private String status;              // filtra por status do pedido (ex: PLACED, PAID, CANCELED)
    private String orderId;
    private UUID customerId;
    private OffsetDateTime placedAtFrom; // limite inferior do intervalo de data de criação
    private OffsetDateTime placedAtTo;   // limite superior do intervalo de data de criação
    private BigDecimal totalAmountFrom;  // valor mínimo do pedido
    private BigDecimal totalAmountTo;    // valor máximo do pedido

    // construtor do super class
    public OrderFilter(int size, int page) {
        super(size, page);
    }

    // Se nenhum sort for informado pelo cliente, ordena por data de criação (ASC) como padrão
    @Override
    public SortType getSortByPropertyOrDefault() {
        return getSortByProperty() == null ? SortType.PLACED_AT : getSortByProperty();
    }

    @Override
    public Sort.Direction getSortDirectionOrDefault() {
        return getSortDirection() == null ? Sort.Direction.ASC : getSortDirection();
    }

    // Enum type-safe que mapeia opções de ordenação para nomes de propriedades JPA.
    // O propertyName corresponde ao nome do campo na entidade de persistência,
    // usado diretamente no builder.asc(root.get(sortType.getPropertyName())).
    @Getter
    @RequiredArgsConstructor
    public enum SortType {
        PLACED_AT("placedAt"),
        PAID_AT("paidAt"),
        READY_AT("readyAt"),
        CANCELED_AT("canceledAt"),
        PAYMENT_METHOD("paymentMethod"),
        STATUS("status");

        private final String propertyName;
    }
}
