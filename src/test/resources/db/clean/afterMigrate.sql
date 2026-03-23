-- ===================================================================================
-- Flyway afterMigrate.sql — Versão de TESTE (profile test)
-- ===================================================================================
--
-- CONTEXTO:
--   O Flyway possui callbacks automáticos que executam scripts em momentos específicos
--   do ciclo de migração. O "afterMigrate" executa APÓS todas as migrations (V001, V002...)
--   serem aplicadas com sucesso.
--
--   Existem DOIS afterMigrate.sql no projeto, um para cada cenário:
--
--   1) src/main/resources/db/testdata/afterMigrate.sql (dev/produção)
--      → Insere seed data (customers, orders, shopping carts de exemplo)
--      → Configurado via: spring.flyway.locations=classpath:db/migration,classpath:db/testdata
--
--   2) src/test/resources/db/clean/afterMigrate.sql (ESTE ARQUIVO — testes)
--      → Limpa TODAS as tabelas com TRUNCATE CASCADE
--      → Configurado via: spring.flyway.locations=classpath:db/migration,classpath:db/clean
--      → O profile de teste (application-test.yaml) sobrescreve o location do Flyway,
--        fazendo com que ESTE afterMigrate.sql seja executado no lugar do de seed data.
--
-- POR QUE ESTA ABORDAGEM E NÃO @Sql?
--   O Spring Test oferece a anotação @Sql para executar scripts em fases do teste:
--     @Sql(scripts = "classpath:sql/clean.sql", executionPhase = BEFORE_TEST_CLASS)
--     @Sql(scripts = "classpath:sql/clean.sql", executionPhase = AFTER_TEST_METHOD)
--
--   Comparação de performance:
--   ┌──────────────────────┬─────────────────────────────┬──────────────────────────┐
--   │                      │ Flyway afterMigrate (clean)  │ @Sql (AFTER_TEST_METHOD) │
--   ├──────────────────────┼─────────────────────────────┼──────────────────────────┤
--   │ Quando executa       │ 1x na inicialização          │ N vezes (a cada teste)   │
--   │ Seed data inserido?  │ Nunca (substituído por clean) │ Sim, depois limpo        │
--   │ Overhead por teste   │ Zero (rollback do @Transact.) │ TRUNCATE a cada método   │
--   │ Configuração         │ application-test.yaml         │ Anotação em cada classe  │
--   └──────────────────────┴─────────────────────────────┴──────────────────────────┘
--
--   Com Flyway clean: o seed data NUNCA é inserido nos testes. O Flyway já executa
--   este TRUNCATE no lugar. Cada teste usa @Transactional que faz rollback automático,
--   então não há necessidade de limpeza adicional entre testes.
--
--   Com @Sql: o seed data É inserido (pelo afterMigrate.sql de dev), e depois o @Sql
--   limpa. Isso gera trabalho duplicado (inserir + truncar) e executa TRUNCATE após
--   cada método de teste, mesmo quando @Transactional já faria rollback.
--
-- RESULTADO: Flyway clean é mais eficiente, mais simples e elimina a necessidade
-- de anotar cada classe de teste com @Sql.
-- ===================================================================================

truncate table customer cascade;
truncate table "order" cascade;
truncate table order_item cascade;
truncate table shopping_cart cascade;
truncate table shopping_cart_item cascade;