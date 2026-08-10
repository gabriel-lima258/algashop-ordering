# algashop-ordering

Serviço de pedidos do AlgaShop: carrinho de compras, checkout e ciclo de vida do pedido.

É o serviço onde o domínio é levado a sério — **DDD tático com arquitetura hexagonal**, e a implementação mais "purista" dos quatro microsserviços do projeto. O contraponto deliberado é o [`algashop-billing`](https://github.com/gabriel-lima258/algashop-billing), que resolve um domínio igualmente rico de forma pragmática.

---

## O problema

Um pedido não é um formulário salvo no banco. Ele tem regras que precisam valer **sempre**: não se altera item de pedido já pago, não se calcula frete sem endereço de destino, cliente com pontos suficientes tem frete grátis, o total precisa bater com a soma dos itens.

O caminho fácil é espalhar esses `if` pelos serviços de aplicação e torcer para ninguém esquecer um. Aqui a escolha foi outra: as regras moram **dentro dos agregados**, e o resto do sistema não consegue alcançá-las por fora. O preço é mais indireção — e o benefício é que uma regra de negócio tem um único lugar onde pode ser violada.

---

## Stack

| | |
|---|---|
| **Java** | 25 |
| **Spring Boot** | 4.0.1 |
| **Banco** | PostgreSQL 17 (`ordering`) |
| **Cache** | Redis 8 (banco lógico 1) |
| **Porta** | 8081 |
| **Pacote raiz** | `com.gtech.algashop` |
| **Schema** | Flyway — 5 migrations |
| **Build** | Gradle 9.2.1 |

---

## Arquitetura — ports & adapters

O código é dividido em duas metades, e a direção das dependências é o ponto:

```
core/                              o que o negócio é
├── domain/model/                  agregados, VOs, eventos, specifications
├── application/                   casos de uso, orquestração
└── ports/
    ├── in/                        o que o mundo pode PEDIR ao domínio
    └── out/                       o que o domínio PRECISA do mundo

infrastructure/                    como as coisas acontecem
├── adapters/in/                   web (controllers), listeners
├── adapters/out/                  persistência, notificação, clients HTTP
└── config/
```

O `core` não conhece o `infrastructure`. Quem quer falar com o domínio passa por uma **porta de entrada**; quando o domínio precisa de algo de fora, declara uma **porta de saída** e alguém do `infrastructure` a implementa.

As portas são nomeadas por intenção, com prefixo `For…`:

| Porta | Tipo | Papel |
|---|---|---|
| `ForBuyingWithShoppingCart` | entrada | fechar o carrinho e gerar o pedido |
| `ForManagingOrders` | entrada | marcar pedido como pago, pronto ou cancelado |
| `ForQueryOrders` | entrada | consulta (lado de leitura, separado do comando) |
| `ForObtainingOrder` | saída | de onde vêm os dados de leitura |
| `ForNotifyingCustomers` | saída | como o cliente é avisado |

A separação entre `ForManagingOrders` e `ForQueryOrders` não é acidental: é **CQS** — comando e consulta entram por portas diferentes, com modelos de saída próprios.

---

## Modelo de domínio

### Agregados

Três raízes, cada uma implementando `AggregateRoot<ID>`:

| Agregado | Identidade | Responsabilidade |
|---|---|---|
| `Customer` | `CustomerId` | cadastro, pontos de fidelidade, arquivamento |
| `Order` | `OrderId` | itens, cobrança, entrega, status |
| `ShoppingCart` | `ShoppingCartId` | itens antes do checkout |

`OrderItem` e `ShoppingCartItem` são entidades filhas — só existem dentro da raiz e só são alteradas por ela.

### Value objects

Tipos que carregam validação e não têm identidade própria, todos `record`:

`Money` · `Email` · `Document` · `FullName` · `Phone` · `Quantity` · `ZipCode` · `Address` · `BirthDate` · `LoyaltyPoints` · `ProductName` · `Billing` · `Recipient` · `Shipping`

O ganho aparece na assinatura dos métodos: `Money` em vez de `BigDecimal` torna impossível somar um preço com uma quantidade por engano.

### Eventos de domínio

Publicados pelos agregados e consumidos por listeners in-process:

| Agregado | Eventos |
|---|---|
| `Customer` | `CustomerRegisteredEvent`, `CustomerArchivedEvent` |
| `Order` | `OrderPlacedEvent`, `OrderPaidEvent`, `OrderReadyEvent`, `OrderCanceledEvent` |
| `ShoppingCart` | `ShoppingCartCreatedEvent`, `ShoppingCartItemAddedEvent`, `ShoppingCartItemRemovedEvent`, `ShoppingCartEmptiedEvent` |

### Specifications

Regras de negócio como objetos combináveis, em vez de `if` aninhado:

- `CustomerHasEnoughLoyaltyPointsSpecification`
- `CustomerHasOrderedEnoughAtYearSpecification`
- `CustomerHaveFreeShippingSpecification`

Combináveis por `and`, `or`, `not` e `andNot` — a última é a composição das duas primeiras.

---

## API

### Customers — `/api/v1/customers`

| Verbo | Path | O que faz |
|---|---|---|
| `GET` | `/api/v1/customers` | lista paginada, com filtro |
| `GET` | `/api/v1/customers/{customerId}` | detalhe |
| `GET` | `/api/v1/customers/{customerId}/shopping-cart` | carrinho do cliente |
| `POST` | `/api/v1/customers` | cadastra → `201` |
| `PUT` | `/api/v1/customers/{customerId}` | atualiza |
| `DELETE` | `/api/v1/customers/{customerId}` | arquiva → `204` |

### Orders — `/api/v1/orders`

| Verbo | Path | Content-Type | O que faz |
|---|---|---|---|
| `GET` | `/api/v1/orders` | — | lista paginada, com filtro |
| `GET` | `/api/v1/orders/{orderId}` | — | detalhe |
| `POST` | `/api/v1/orders` | `application/vnd.order-with-product.v1+json` | compra direta de um produto → `201` |
| `POST` | `/api/v1/orders` | `application/vnd.order-with-shopping-cart.v1+json` | checkout do carrinho → `201` |

> Os dois `POST` compartilham a mesma URL e são distinguidos pelo **content-type versionado**. São dois casos de uso diferentes — comprar agora × fechar o carrinho — que produzem o mesmo recurso, então ganham o mesmo endereço e corpos distintos. É também onde a versão da API vive, em vez de num `/v2` na URL.

### Shipping — `/api/v1/shipping-cost-previews`

| Verbo | Path | O que faz |
|---|---|---|
| `POST` | `/api/v1/shipping-cost-previews` | consulta frete por CEP, antes de existir pedido |

Chama a Rapidex, e por isso é o endpoint onde o fallback aparece: se a transportadora estiver fora, a resposta vem estimada em vez de falhar.

### Shopping carts — `/api/v1/shopping-carts`

| Verbo | Path | O que faz |
|---|---|---|
| `POST` | `/api/v1/shopping-carts` | cria o carrinho com o primeiro item → `201` |
| `GET` | `/api/v1/shopping-carts/{id}` | detalhe |
| `GET` | `/api/v1/shopping-carts/{id}/items` | itens |
| `POST` | `/api/v1/shopping-carts/{id}/items` | adiciona item → `204` |
| `DELETE` | `/api/v1/shopping-carts/{id}` | remove o carrinho → `204` |
| `DELETE` | `/api/v1/shopping-carts/{id}/items` | esvazia → `204` |
| `DELETE` | `/api/v1/shopping-carts/{id}/items/{itemId}` | remove um item → `204` |

Erros seguem **RFC 7807** (`ProblemDetail`), com tipos `/errors/*` — incluindo `502` e `504` para falha de integração.

---

## Integrações

Duas chamadas de saída, ambas por interface declarativa (`@HttpExchange` sobre `RestClient`):

| Destino | Para quê | Propriedade |
|---|---|---|
| `product-catalog` | dados do produto ao montar o pedido | `algashop.integrations.product-catalog.url` |
| Rapidex | cálculo do frete | `algashop.integrations.rapidex.url` |

Cada uma tem implementação **real e fake**, trocáveis por configuração (`algashop.integrations.shipping.provider` = `RAPIDEX` \| `FAKE`). É o que permite rodar a suíte sem nenhum serviço externo de pé.

Em desenvolvimento o Rapidex aponta para o **WireMock** (`localhost:8787`); o `product-catalog` passou a apontar para o serviço real (`localhost:8083`).

### Cache client-side

A chamada ao catálogo é cacheada — no Redis, na **interface do client HTTP**:

```java
@Cacheable(cacheNames = "algashop:product-catalog-api:v1", key = "#productId")
@GetExchange(value = "/api/v1/products/{productId}", accept = "application/json")
ProductResponse getById(@PathVariable UUID productId);
```

Funciona porque o bean é um proxy JDK criado por `HttpServiceProxyFactory`, e o auto-proxy do `@EnableCaching` o envelopa lendo a anotação da interface. Ninguém que chama esse client precisa saber que existe cache no meio.

> **Cachear dado dos outros só tem TTL como invalidação.** O produto é do catálogo, e este serviço **não fica sabendo quando ele muda** — não há evento, não há callback, não há `@CacheEvict` possível. O TTL curto não é escolha de performance: é a única ferramenta disponível.

E o cache **não** reduz o acoplamento: um miss ainda depende do catálogo estar de pé. O que ele compra é fôlego, não independência.

O `ResilienceCacheErrorHandler` faz *fail-open* — sem ele, uma queda do Redis derrubaria a criação de pedido **com o catálogo respondendo normalmente**, porque a exceção subiria do proxy de cache antes de a chamada HTTP ser sequer tentada.

### Resiliência

As duas chamadas de saída são envolvidas por timeout, retry, bulkhead e circuit breaker:

```
@ConcurrencyLimit(10) → @Cacheable → circuitBreaker.run → retry → timeout 3s/7s → rede
```

A ordem é o que importa: **sem timeout o circuito nunca abre**, porque ele só reage a falhas que terminaram — uma chamada pendurada não é falha, é uma chamada em andamento.

E os dois clients decidem **o oposto** sobre fallback, de propósito:

| Dependência | Se cair |
|---|---|
| `product-catalog` | **falha** — 502 ou 504, o pedido não é criado |
| Rapidex (frete) | **degrada** — devolve estimativa de R$ 20,00 em 10 dias |

Não dá para inventar o preço de um produto; dá para estimar um frete. A pergunta que decide um fallback é *"existe resposta aproximada aceitável?"* — e ela é de negócio, não de engenharia.

> O custo do fallback do frete, dito por inteiro: como ele existe, uma queda da transportadora **nunca vira erro**. O cliente recebe um valor que não veio de lugar nenhum, e só um `log.warn` registra isso.

A biblioteca não é Resilience4j — é **Spring Cloud CircuitBreaker** com a implementação `framework-retry`, sobre o `org.springframework.core.retry` do Spring 7, mais `@ConcurrencyLimit` nativo.

---

## Como rodar

Suba a infraestrutura a partir do repositório [`algashop-meta`](https://github.com/gabriel-lima258/algashop-meta):

```bash
docker compose -f docker-compose.tools.yml up -d
```

Isso dá PostgreSQL na **5433** (o deslocamento é proposital, para não colidir com uma instalação nativa na 5432), WireMock na **8787** e Redis na **6379**.

> ⚠️ O Redis precisa do `.env` na raiz do meta (`REDIS_PASSWORD=algashop`). Sem ele o Compose resolve a senha para string vazia e o cache nunca funciona — silenciosamente, porque o error handler engole a falha de conexão.

Para exercitar o cache client-side é preciso o `product-catalog` de pé na **8083**, já que é a resposta dele que fica cacheada.

```bash
./gradlew bootRun
```

O serviço responde em `http://localhost:8081`. O Flyway aplica as 5 migrations e carrega massa de teste na subida.

---

## Testes

Três suítes separadas, para falha barata aparecer antes da cara:

```bash
./gradlew test              # unitários e de fatia — não precisam de infra
./gradlew integrationTest   # classes *IT — precisam do Postgres de pé
./gradlew contractTest      # gerados a partir dos contratos .groovy
./gradlew check             # as três
```

| Suíte | Volume |
|---|---|
| `test` | 36 classes `*Test` |
| `integrationTest` | 30 classes `*IT` |
| `contractTest` | 13 contratos — 5 de `order`, 8 de `shopping-cart` |

Os contratos em `src/contractTest/resources/contracts/` servem duas pontas: geram os testes que verificam **este** serviço e o stub WireMock que os **consumidores** usam para testar sem subir o `ordering`.

---

## Imagem Docker

```bash
./gradlew bootJar
docker build -t algashop/ordering:dev .
```

Ou, para multi-arquitetura (arm64 + amd64) com push:

```bash
./gradlew dockerBuild
```

A imagem base é `eclipse-temurin:25-jre` — precisa acompanhar o toolchain do `build.gradle`, senão o container morre no start com `UnsupportedClassVersionError`.


### Health check

```bash
curl -s localhost:8081/actuator/health | jq            # tudo
curl -s localhost:8081/actuator/health/readiness | jq  # só o essencial
```

O grupo `readiness` inclui **apenas o banco** — cache ou circuito fora do ar não tira a instância de rotação, só marca o serviço como `DEGRADED`. É um status inventado pelo projeto, posicionado entre `UNKNOWN` e `UP` no `status.order`.

> ⚠️ `DEGRADED` devolve **HTTP 200**: só `DOWN` e `OUT_OF_SERVICE` viram 503 por padrão. Um probe que olhe o código de status não vê diferença.

Detalhes em [Health check e degradação](https://github.com/gabriel-lima258/algashop-docs/blob/main/04-infraestrutura/health-checks.md).


### Teste de carga

```bash
k6 run etc/k6/buy-now.js                      # do repositório meta
```

O `POST /api/v1/orders` é o caminho mais caro do sistema: abre transação no Postgres, chama o **product-catalog** e a **Rapidex** por HTTP e só então grava — as duas chamadas de rede **dentro** da transação.

No compose ele sobe com o Tomcat limitado a 10 threads, de propósito. Medido: **1156 req/s** sustentados, `p(95)` de 2,44s e zero erros — exatamente `10 threads ÷ 8,6ms`, a Lei de Little. Ligando threads virtuais a vazão **caiu para 127 req/s** e o serviço travou de vez, porque o teto do Tomcat era o único controle de admissão que existia.

Detalhes em [Threads e concorrência](https://github.com/gabriel-lima258/algashop-docs/blob/main/04-infraestrutura/threads-e-concorrencia.md).

---

## Documentação

O projeto tem um caderno de estudos separado, em [`algashop-docs`](https://github.com/gabriel-lima258/algashop-docs). Os documentos que tocam este serviço:

- [Arquitetura](https://github.com/gabriel-lima258/algashop-docs/blob/main/00-visao-geral/arquitetura.md) — como os quatro serviços se conectam
- [Ports & Adapters](https://github.com/gabriel-lima258/algashop-docs/blob/main/01-arquitetura-design/ports-hexagonal.md) — por que `ports/in` e `ports/out` são separados
- [Specification Pattern](https://github.com/gabriel-lima258/algashop-docs/blob/main/01-arquitetura-design/specification.md) — regra de negócio como objeto combinável
- [CQS e CQRS](https://github.com/gabriel-lima258/algashop-docs/blob/main/01-arquitetura-design/cqrs.md) — separar comando de consulta
- [Cache](https://github.com/gabriel-lima258/algashop-docs/blob/main/01-arquitetura-design/cache.md) — client-side × server-side, e por que quem cacheia dado dos outros só tem TTL
- [Resiliência](https://github.com/gabriel-lima258/algashop-docs/blob/main/01-arquitetura-design/resiliencia.md) — os cinco padrões, a ordem em que se aninham e quando um fallback mente
- [Resiliência na prática](https://github.com/gabriel-lima258/algashop-docs/blob/main/04-infraestrutura/resiliencia-config.md) — parâmetros, biblioteca e como testar retry
- [Redis na prática](https://github.com/gabriel-lima258/algashop-docs/blob/main/04-infraestrutura/redis.md) — eviction, TTL e a armadilha da senha vazia
- [Contract tests](https://github.com/gabriel-lima258/algashop-docs/blob/main/03-testes-integracao/stubs-contract-tests.md) — testar integração sem subir o outro serviço
- [Health check e degradação](https://github.com/gabriel-lima258/algashop-docs/blob/main/04-infraestrutura/health-checks.md) — liveness × readiness e o status DEGRADED
- [Threads e concorrência](https://github.com/gabriel-lima258/algashop-docs/blob/main/04-infraestrutura/threads-e-concorrencia.md) — onde o serviço satura, medido sob carga
- [Tratamento de erros](https://github.com/gabriel-lima258/algashop-docs/blob/main/03-testes-integracao/tratamento-erros-api.md) — `ProblemDetail` e quando usar 404, 422 ou 502
- [Paginação](https://github.com/gabriel-lima258/algashop-docs/blob/main/02-persistencia/paginacao.md) · [Flyway](https://github.com/gabriel-lima258/algashop-docs/blob/main/02-persistencia/flyway.md) · [Ambiente local](https://github.com/gabriel-lima258/algashop-docs/blob/main/04-infraestrutura/ambiente-local.md)
