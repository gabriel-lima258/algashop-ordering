package com.gtech.algashop.application.customer.query;

import com.gtech.algashop.domain.model.commons.*;
import com.gtech.algashop.domain.model.costumer.*;
import com.gtech.algashop.domain.model.customer.CustomerTestDataBuilder;
import com.gtech.algashop.domain.model.product.ProductCatalogService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CustomerQueryServiceIT {

    @Autowired
    private CustomerQueryService customerQueryService;

    @Autowired
    private Customers customers;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    private Customer alice;
    private Customer bob;
    private Customer charlie;
    private Customer aliceJohnson;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now();

        alice = buildAndAddCustomer("Alice", "Smith", "alice.smith@gmail.com",
                now.minus(4, ChronoUnit.DAYS));

        bob = buildAndAddCustomer("Bob", "Jones", "bob.jones@hotmail.com",
                now.minus(3, ChronoUnit.DAYS));

        charlie = buildAndAddCustomer("Charlie", "Brown", "charlie.brown@gmail.com",
                now.minus(2, ChronoUnit.DAYS));

        aliceJohnson = buildAndAddCustomer("Alice", "Johnson", "alice.johnson@outlook.com",
                now.minus(1, ChronoUnit.DAYS));
    }

    @Test
    void shouldFindById() {
        Customer customer = CustomerTestDataBuilder.existingCustomer().build();
        customers.add(customer);

        CustomerOutput output = customerQueryService.findById(customer.id().value());

        Assertions.assertThat(output)
                .extracting(
                        CustomerOutput::getId,
                        CustomerOutput::getFirstName,
                        CustomerOutput::getLastName,
                        CustomerOutput::getEmail,
                        CustomerOutput::getPhone,
                        CustomerOutput::getDocument,
                        CustomerOutput::getBirthDate,
                        CustomerOutput::getPromotionNotificationsAllowed,
                        CustomerOutput::getLoyaltyPoints,
                        CustomerOutput::getRegisteredAt,
                        CustomerOutput::getArchivedAt,
                        CustomerOutput::getArchived
                ).containsExactly(
                        customer.id().value(),
                        customer.fullName().firstName(),
                        customer.fullName().lastName(),
                        customer.email().email(),
                        customer.phone().phone(),
                        customer.document().document(),
                        customer.birthDate().birthDate(),
                        customer.isPromotionNotificationsAllowed(),
                        customer.loyaltyPoints().point(),
                        customer.registeredAt(),
                        customer.archivedAt(),
                        customer.isArchived()
                );
    }

    // =====================================================
    //  Busca por nome (firstName) — case insensitive, LIKE
    // =====================================================

    @Test
    void shouldFilterByFirstNameIgnoringCase() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setFirstName("alice");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(CustomerSummaryOutput::getFirstName)
                .containsExactlyInAnyOrder("Alice", "Alice");
    }

    @Test
    void shouldFilterByFirstNameWithUpperCase() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setFirstName("ALICE");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(CustomerSummaryOutput::getEmail)
                .containsExactlyInAnyOrder("alice.smith@gmail.com", "alice.johnson@outlook.com");
    }

    @Test
    void shouldFilterByFirstNameContainingPartialTerm() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setFirstName("li");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        // "Ali" contém "li" → Alice Smith e Alice Johnson
        // "Charlie" contém "li" → Charlie Brown
        assertThat(result.getContent())
                .hasSize(3)
                .extracting(CustomerSummaryOutput::getFirstName)
                .containsExactlyInAnyOrder("Alice", "Alice", "Charlie");
    }

    // =====================================================
    //  Busca por e-mail — case insensitive, LIKE
    // =====================================================

    @Test
    void shouldFilterByEmailIgnoringCase() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setEmail("ALICE");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(CustomerSummaryOutput::getEmail)
                .containsExactlyInAnyOrder("alice.smith@gmail.com", "alice.johnson@outlook.com");
    }

    @Test
    void shouldFilterByEmailContainingDomainName() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setEmail("gmail");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(CustomerSummaryOutput::getEmail)
                .containsExactlyInAnyOrder("alice.smith@gmail.com", "charlie.brown@gmail.com");
    }

    @Test
    void shouldFilterByEmailContainingPartialTerm() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setEmail("john");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        // "alice.johnson@outlook.com" contém "john"
        assertThat(result.getContent())
                .hasSize(1)
                .extracting(CustomerSummaryOutput::getEmail)
                .containsExactly("alice.johnson@outlook.com");
    }

    // =====================================================
    //  Filtros combinados (firstName AND email)
    // =====================================================

    @Test
    void shouldFilterByFirstNameAndEmailCombined() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setFirstName("alice");
        filter.setEmail("gmail");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        // Alice Smith (gmail) ✓  |  Alice Johnson (outlook) ✗
        assertThat(result.getContent())
                .hasSize(1)
                .extracting(CustomerSummaryOutput::getEmail)
                .containsExactly("alice.smith@gmail.com");
    }

    @Test
    void shouldReturnEmptyWhenCombinedFiltersMatchNoOne() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setFirstName("bob");
        filter.setEmail("gmail");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        // Bob usa hotmail, não gmail
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // =====================================================
    //  Paginação
    // =====================================================

    @Test
    void shouldReturnFirstPageWithCorrectSize() {
        CustomerFilter filter = new CustomerFilter(2, 0);

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isZero();
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isFalse();
    }

    @Test
    void shouldReturnSecondPageWithRemainingElements() {
        CustomerFilter filter = new CustomerFilter(3, 1);

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isTrue();
    }

    @Test
    void shouldReturnEmptyPageWhenPageExceedsTotalPages() {
        CustomerFilter filter = new CustomerFilter(15, 5);

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(4);
    }

    // =====================================================
    //  Ordenação
    // =====================================================

    @Test
    void shouldSortByRegisteredAtAscending() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setSortByProperty(CustomerFilter.SortType.REGISTERED_AT);
        filter.setSortDirection(Sort.Direction.ASC);

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent())
                .hasSize(4)
                .extracting(CustomerSummaryOutput::getFirstName)
                .containsExactly("Alice", "Bob", "Charlie", "Alice");

        assertThat(result.getContent())
                .extracting(CustomerSummaryOutput::getLastName)
                .containsExactly("Smith", "Jones", "Brown", "Johnson");
    }

    @Test
    void shouldSortByRegisteredAtDescending() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setSortByProperty(CustomerFilter.SortType.REGISTERED_AT);
        filter.setSortDirection(Sort.Direction.DESC);

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent())
                .hasSize(4)
                .extracting(CustomerSummaryOutput::getLastName)
                .containsExactly("Johnson", "Brown", "Jones", "Smith");
    }

    @Test
    void shouldSortByFirstNameAscending() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setSortByProperty(CustomerFilter.SortType.FIRST_NAME);
        filter.setSortDirection(Sort.Direction.ASC);

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent())
                .extracting(CustomerSummaryOutput::getFirstName)
                .containsExactly("Alice", "Alice", "Bob", "Charlie");
    }

    @Test
    void shouldSortByFirstNameDescending() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setSortByProperty(CustomerFilter.SortType.FIRST_NAME);
        filter.setSortDirection(Sort.Direction.DESC);

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent())
                .extracting(CustomerSummaryOutput::getFirstName)
                .containsExactly("Charlie", "Bob", "Alice", "Alice");
    }

    @Test
    void shouldUseDefaultSortWhenNoneSpecified() {
        CustomerFilter filter = new CustomerFilter(15, 0);

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        // Default: REGISTERED_AT ASC (Alice Smith → Bob → Charlie → Alice Johnson)
        assertThat(result.getContent())
                .extracting(CustomerSummaryOutput::getLastName)
                .containsExactly("Smith", "Jones", "Brown", "Johnson");
    }

    // =====================================================
    //  Cenário sem resultados
    // =====================================================

    @Test
    void shouldReturnEmptyPageWhenNoCustomerMatchesFirstName() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setFirstName("Zephyr");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getTotalPages()).isZero();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void shouldReturnEmptyPageWhenNoCustomerMatchesEmail() {
        CustomerFilter filter = new CustomerFilter(15, 0);
        filter.setEmail("nonexistent@xyz.com");

        Page<CustomerSummaryOutput> result = customerQueryService.filter(filter);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // =====================================================
    //  Helper
    // =====================================================

    private Customer buildAndAddCustomer(String firstName, String lastName,
                                         String email, OffsetDateTime registeredAt) {
        Customer customer = Customer.existing()
                .id(new CustomerId())
                .fullName(new FullName(firstName, lastName))
                .birthDate(new BirthDate(LocalDate.of(1990, 1, 1)))
                .email(new Email(email))
                .phone(new Phone("000-000-0000"))
                .document(new Document("000-00-0000"))
                .promotionNotificationsAllowed(true)
                .archived(false)
                .registeredAt(registeredAt)
                .archivedAt(null)
                .loyaltyPoints(LoyaltyPoints.ZERO)
                .address(Address.builder()
                        .street("Test Street")
                        .number("100")
                        .neighborhood("Test")
                        .city("Test City")
                        .state("TS")
                        .zipCode(new ZipCode("00000"))
                        .build())
                .build();

        customers.add(customer);
        return customer;
    }
}