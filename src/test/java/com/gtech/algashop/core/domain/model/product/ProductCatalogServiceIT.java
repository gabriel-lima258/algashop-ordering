package com.gtech.algashop.core.domain.model.product;

import com.gtech.algashop.infrastructure.adapters.in.web.utils.TestContainerPostgresSQLConfig;
import com.gtech.algashop.infrastructure.adapters.out.web.product.client.http.ProductCatalogApiClient;
import com.gtech.algashop.infrastructure.adapters.out.web.product.client.http.ProductResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

// teste de integracao do ProductCatalogService: sobe o contexto do Spring (sem servidor web)
// para que os proxies de resiliencia estejam ativos - hoje o @ConcurrencyLimit(10) do
// ResilientProductCatalogAPIClient, que e o bulkhead por onde toda chamada passa.
//
// O que este teste garante: sob concorrencia, TODAS as chamadas completam com o mesmo
// resultado. O bulkhead serializa o excedente, nao descarta ninguem.
//
// Os cenarios de falha (404, 4xx, 5xx, timeout, circuito abrindo) ficam no
// ResilientProductCatalogAPIClientIT, que usa WireMock em vez de mock do client.
//
// O QUE ESTE TESTE NAO PROVA, e vale ser honesto: ele NAO satura o bulkhead. Sao 6 chamadas
// contra um limite de 10, num pool de 10 threads - nenhuma thread chega a esperar na fila.
// O que ele garante e mais modesto que o nome sugere: que o proxy do @ConcurrencyLimit nao
// quebra nem perde chamada sob concorrencia.
//
// Para provar o bulkhead de verdade seria preciso CONCURRENT_CALLS > 10 e uma forma de medir
// que a 11a esperou - por exemplo, um mock que segura a resposta e conta quantas threads
// entraram simultaneamente. Fica registrado como pendencia em
// docs/01-arquitetura-design/resiliencia.md
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Import(TestContainerPostgresSQLConfig.class) // banco real em container, so para o contexto subir
class ProductCatalogServiceIT {

    private static final int CONCURRENT_CALLS = 6;

    @Autowired
    private ProductCatalogService productCatalogService;

    // troca o client HTTP real por um mock: nenhuma chamada sai para o micro serviço de products
    @MockitoBean
    private ProductCatalogApiClient productCatalogApiClient;

    @Test
    void shouldLoadProductForEveryConcurrentCall() throws Exception {
        UUID rawProductId = UUID.randomUUID();
        ProductId productId = new ProductId(rawProductId);

        // ATENCAO: nao devolva null aqui. O client trata corpo nulo como resposta invalida
        // do upstream (BadGatewayException), entao um mock com thenReturn(null) testaria o
        // caminho de erro sem querer - e ainda abriria o circuito para os testes seguintes.
        Mockito.when(productCatalogApiClient.getById(rawProductId)).thenReturn(
                new ProductResponse(rawProductId, "Notebook X11", new BigDecimal("1000.00"), true));

        List<Future<Optional<Product>>> results = new ArrayList<>();

        try (ExecutorService executorService = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < CONCURRENT_CALLS; i++) {
                results.add(executorService.submit(() -> productCatalogService.ofId(productId)));
            }
            // o close() do try-with-resources e quem espera as tarefas terminarem
        }

        for (Future<Optional<Product>> result : results) {
            assertThat(result.get()).isPresent();
            assertThat(result.get().get().productName().name()).isEqualTo("Notebook X11");
        }
    }

}
