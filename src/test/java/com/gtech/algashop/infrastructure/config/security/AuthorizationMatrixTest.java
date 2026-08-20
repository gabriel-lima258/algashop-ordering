package com.gtech.algashop.infrastructure.config.security;

import com.gtech.algashop.core.application.shipping.ShippingApplicationService;
import com.gtech.algashop.core.ports.in.checkout.ForBuyingProduct;
import com.gtech.algashop.core.ports.in.checkout.ForBuyingWithShoppingCart;
import com.gtech.algashop.core.ports.in.customer.ForManagingCustomer;
import com.gtech.algashop.core.ports.in.customer.ForQueryCustomers;
import com.gtech.algashop.core.ports.in.order.ForQueryOrders;
import com.gtech.algashop.core.ports.in.shoppingcart.ForManagingShoppingCarts;
import com.gtech.algashop.core.ports.in.shoppingcart.ForQueryShoppingCarts;
import com.gtech.algashop.infrastructure.adapters.in.web.customer.CustomerController;
import com.gtech.algashop.infrastructure.adapters.in.web.order.OrderController;
import com.gtech.algashop.infrastructure.adapters.in.web.shipping.ShippingCostController;
import com.gtech.algashop.infrastructure.adapters.in.web.shoppingcart.ShoppingCartController;
import com.gtech.algashop.utils.AlgaShopResourceUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

/**
 * A matriz de autorizacao do servico: para CADA rota anotada, tres perguntas.
 *
 *   sem token           -> 401 (nao autenticado)
 *   escopo errado       -> 403 (autenticado, sem permissao)
 *   escopo correto      -> passa pela seguranca
 *
 * Tres decisoes de desenho que valem ser lidas antes de mexer aqui:
 *
 * 1. @Import(OrderingSecurityConfig.class) NAO e opcional. Sem ele, o @WebMvcTest
 *    autoconfigura a cadeia de filtros PADRAO do Spring Boot - e o teste passaria a
 *    afirmar sobre uma configuracao que este projeto nao usa. O @EnableMethodSecurity
 *    que liga o @PreAuthorize tambem vem de la.
 *
 * 2. O @MockitoBean JwtDecoder existe so para o contexto subir: oauth2ResourceServer().jwt()
 *    exige o bean. Ele nunca e chamado, porque o post-processor jwt() do spring-security-test
 *    injeta a autenticacao ja pronta. Consequencia: este teste NAO cobre validacao de
 *    token (assinatura, iss, exp, aud) - so autorizacao. Ver MockJwtFactory.
 *
 * 3. O caso positivo afirma "nao e 401 nem 403", e nao "e 200". Um teste de seguranca
 *    deve falhar quando a seguranca muda, nao quando o controller passa a devolver 400
 *    por causa de um corpo vazio ou 404 por causa de um id inexistente. Acoplar ao
 *    resultado de negocio transforma esta classe numa fonte de falha por motivo errado.
 */
@WebMvcTest(controllers = {
        OrderController.class,
        CustomerController.class,
        ShoppingCartController.class,
        ShippingCostController.class
})
@Import(OrderingSecurityConfig.class)
class AuthorizationMatrixTest {

    // Escopo que nenhuma rota exige. Serve para provar que estar AUTENTICADO nao basta:
    // o token e valido, o portador e conhecido, e ainda assim leva 403.
    private static final String UNRELATED_SCOPE = "SCOPE_totally:unrelated";

    private static final String ORDER_WITH_PRODUCT = "application/vnd.order-with-product.v1+json";
    private static final String ORDER_WITH_CART = "application/vnd.order-with-shopping-cart.v1+json";
    private static final String JSON = "application/json";

    // Corpos que PASSAM na bean validation. Sao necessarios por causa da ordem em que o
    // Spring executa as coisas - ver shouldValidateBodyBeforeCheckingScope() no fim da
    // classe. Com corpo invalido a requisicao morre em 400 antes de o @PreAuthorize rodar,
    // e o caso de 403 nunca seria exercitado.
    private static final String ADDRESS = """
            {"street":"Elm Street","number":"456","complement":"","neighborhood":"Central Park",
             "city":"Springfield","state":"Illinois","zipCode":"62704"}""";

    private static final String CUSTOMER_BODY = """
            {"firstName":"John","lastName":"Doe","birthDate":"1990-01-01","document":"255-08-0578",
             "phone":"478-256-2604","email":"john.doe@email.com","promotionNotificationsAllowed":false,
             "address":%s}""".formatted(ADDRESS);

    private static final String CUSTOMER_UPDATE_BODY = """
            {"firstName":"John","lastName":"Doe","phone":"478-256-2604",
             "promotionNotificationsAllowed":false,"address":%s}""".formatted(ADDRESS);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean private ForQueryOrders forQueryOrders;
    @MockitoBean private ForBuyingProduct forBuyingProduct;
    @MockitoBean private ForBuyingWithShoppingCart forBuyingWithShoppingCart;
    @MockitoBean private ForManagingCustomer forManagingCustomer;
    @MockitoBean private ForQueryCustomers forQueryCustomers;
    @MockitoBean private ForQueryShoppingCarts forQueryShoppingCarts;
    @MockitoBean private ForManagingShoppingCarts forManagingShoppingCarts;
    @MockitoBean private ShippingApplicationService shippingApplicationService;

    private static final String CART_ID = "3a2b1c4d-0000-0000-0000-000000000000";
    private static final String CUSTOMER_ID = "41cdc65c-6158-48b0-a8e6-34c0ff8fd74e";

    /**
     * (metodo, caminho, escopo exigido, content-type). Cada linha aqui espelha uma
     * anotacao de SecurityAnnotations num controller - e e essa correspondencia que
     * a classe existe para travar.
     */
    static Stream<Arguments> routes() {
        return Stream.of(
                // ORDERS
                Arguments.of(HttpMethod.GET, "/api/v1/orders", "SCOPE_orders:read", null, null),
                Arguments.of(HttpMethod.GET, "/api/v1/orders/0R8PSRVRB4WQH", "SCOPE_orders:read", null, null),
                Arguments.of(HttpMethod.POST, "/api/v1/orders", "SCOPE_orders:write", ORDER_WITH_PRODUCT, orderWithProductBody()),
                Arguments.of(HttpMethod.POST, "/api/v1/orders", "SCOPE_orders:write", ORDER_WITH_CART, checkoutBody()),

                // CUSTOMERS
                Arguments.of(HttpMethod.GET, "/api/v1/customers", "SCOPE_customers:read", null, null),
                Arguments.of(HttpMethod.GET, "/api/v1/customers/" + CUSTOMER_ID, "SCOPE_customers:read", null, null),
                Arguments.of(HttpMethod.POST, "/api/v1/customers", "SCOPE_customers:write", JSON, CUSTOMER_BODY),
                Arguments.of(HttpMethod.PUT, "/api/v1/customers/" + CUSTOMER_ID, "SCOPE_customers:write", JSON, CUSTOMER_UPDATE_BODY),
                Arguments.of(HttpMethod.DELETE, "/api/v1/customers/" + CUSTOMER_ID, "SCOPE_customers:write", null, null),

                // O carrinho do cliente mora sob /customers mas exige escopo de CARRINHO,
                // nao de cliente. E deliberado, e o tipo de coisa que so um teste fixa.
                Arguments.of(HttpMethod.GET, "/api/v1/customers/" + CUSTOMER_ID + "/shopping-cart",
                        "SCOPE_shopping-carts:read", null, null),

                // SHOPPING CARTS
                Arguments.of(HttpMethod.POST, "/api/v1/shopping-carts", "SCOPE_shopping-carts:write", JSON, "{\"customerId\":\"" + CUSTOMER_ID + "\"}"),
                Arguments.of(HttpMethod.GET, "/api/v1/shopping-carts/" + CART_ID, "SCOPE_shopping-carts:read", null, null),
                Arguments.of(HttpMethod.GET, "/api/v1/shopping-carts/" + CART_ID + "/items", "SCOPE_shopping-carts:read", null, null),
                Arguments.of(HttpMethod.DELETE, "/api/v1/shopping-carts/" + CART_ID, "SCOPE_shopping-carts:write", null, null),
                Arguments.of(HttpMethod.DELETE, "/api/v1/shopping-carts/" + CART_ID + "/items", "SCOPE_shopping-carts:write", null, null),
                Arguments.of(HttpMethod.POST, "/api/v1/shopping-carts/" + CART_ID + "/items", "SCOPE_shopping-carts:write", JSON, "{\"productId\":\"" + CART_ID + "\",\"quantity\":1}"),
                Arguments.of(HttpMethod.DELETE, "/api/v1/shopping-carts/" + CART_ID + "/items/" + CART_ID, "SCOPE_shopping-carts:write", null, null),

                // SHIPPING
                Arguments.of(HttpMethod.POST, "/api/v1/shipping-cost-previews", "SCOPE_shipping-costs:preview", JSON, "{\"zipCode\":\"62704\"}")
        );
    }

    @ParameterizedTest(name = "{0} {1} sem token -> 401")
    @MethodSource("routes")
    void shouldRejectRequestWithoutToken(HttpMethod method, String path, String scope, String contentType, String body)
            throws Exception {
        mockMvc.perform(request(method, path, contentType, body))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .as("rota sem token deveria ser 401")
                        .isEqualTo(401));
    }

    @ParameterizedTest(name = "{0} {1} com escopo errado -> 403")
    @MethodSource("routes")
    void shouldRejectRequestWithUnrelatedScope(HttpMethod method, String path, String scope, String contentType, String body)
            throws Exception {
        mockMvc.perform(request(method, path, contentType, body)
                        .with(jwt().authorities(new SimpleGrantedAuthority(UNRELATED_SCOPE))))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .as("token autenticado sem o escopo %s deveria ser 403", scope)
                        .isEqualTo(403));
    }

    @ParameterizedTest(name = "{0} {1} com {2} -> passa pela seguranca")
    @MethodSource("routes")
    void shouldAllowRequestWithRequiredScope(HttpMethod method, String path, String scope, String contentType, String body)
            throws Exception {
        mockMvc.perform(request(method, path, contentType, body)
                        .with(jwt().authorities(new SimpleGrantedAuthority(scope))))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .as("com o escopo %s a requisicao nao deveria parar na seguranca", scope)
                        .isNotIn(401, 403));
    }

    private static MockHttpServletRequestBuilder request(HttpMethod method, String path,
                                                        String contentType, String body) {
        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders.request(method, path);
        if (contentType != null) {
            builder.contentType(contentType).content(body == null ? "{}" : body);
        }
        return builder;
    }

    private static String orderWithProductBody() {
        return AlgaShopResourceUtils.readContent("json/create-order-with-product.json");
    }

    private static String checkoutBody() {
        return AlgaShopResourceUtils.readContent("json/create-order-with-shopping-cart.json");
    }

    /**
     * A ordem em que o Spring decide, e que explica por que a matriz acima precisa de
     * corpos validos.
     *
     *   1. cadeia de filtros (autenticacao)  -> 401
     *   2. resolucao de argumentos + @Valid  -> 400
     *   3. @PreAuthorize (metodo)            -> 403
     *
     * A consequencia pratica e desconfortavel: um chamador SEM permissao nenhuma
     * consegue descobrir o formato esperado do payload, porque a validacao responde
     * antes de a autorizacao ser consultada. Nao e vazamento grave - a mensagem fala de
     * campos, nao de dados -, mas contraria a expectativa de que 403 vem primeiro.
     *
     * Mover a decisao para authorizeHttpRequests (no filtro, por path) inverteria a
     * ordem. Fica como pendencia registrada, nao como correcao desta fase.
     */
    @org.junit.jupiter.api.Test
    void shouldValidateBodyBeforeCheckingScope() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/customers")
                        .contentType(JSON)
                        .content("{}")
                        .with(jwt().authorities(new SimpleGrantedAuthority(UNRELATED_SCOPE))))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .as("corpo invalido responde 400 ANTES de o @PreAuthorize negar com 403")
                        .isEqualTo(400));
    }

    /**
     * O que a correcao da Fase 21 garante: /actuator/health/** e publico. Com o
     * requestMatchers de caminho literal que existia antes, /readiness e /liveness
     * caiam em anyRequest().authenticated() e o probe do orquestrador levava 401.
     *
     * Aqui os endpoints do Actuator nao estao no contexto da fatia, entao o que se
     * afirma e o que importa: a cadeia de filtros nao recusa por falta de token.
     */
    @ParameterizedTest(name = "{0} e publico")
    @org.junit.jupiter.params.provider.ValueSource(strings = {
            "/actuator/health", "/actuator/health/readiness", "/actuator/health/liveness"
    })
    void shouldNotRequireTokenOnHealthEndpoints(String path) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(path))
                .andExpect(result -> assertThat(result.getResponse().getStatus())
                        .as("%s nao deveria exigir token", path)
                        .isNotIn(401, 403));
    }
}
