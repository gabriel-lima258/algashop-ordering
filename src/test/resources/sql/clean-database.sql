-- ===================================================================================
-- Script de limpeza do banco de dados para testes de integração
-- ===================================================================================
--
-- Este script é executado automaticamente pelo Spring Test Framework através da
-- anotação @Sql configurada nas classes de teste (ex: CustomerQueryServiceIT).
--
-- PROBLEMA QUE RESOLVE:
--   O Flyway executa o afterMigrate.sql ao iniciar o contexto Spring, inserindo
--   dados de exemplo (seed data) no banco. Esses dados poluem os testes de
--   integração, pois os testes esperam começar com o banco limpo para ter
--   controle total sobre o estado dos dados.
--
-- COMO FUNCIONA:
--   - TRUNCATE remove TODOS os registros da tabela de forma rápida (mais eficiente
--     que DELETE pois não gera log de transação por linha)
--   - CASCADE garante que tabelas filhas (com foreign keys) também sejam limpas
--     automaticamente, evitando erros de violação de constraint
--   - A ordem das tabelas não importa graças ao CASCADE
--
-- QUANDO É EXECUTADO:
--   Depende da configuração @Sql na classe de teste:
--   - BEFORE_TEST_CLASS: executa uma vez antes de todos os testes da classe
--   - AFTER_TEST_METHOD: executa após cada método de teste individual
--
-- EXEMPLO DE USO NA CLASSE DE TESTE:
--   @Sql(scripts = "classpath:sql/clean-database.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
--   @Sql(scripts = "classpath:sql/clean-database.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
--
-- ===================================================================================

truncate table customer cascade;
truncate table "order" cascade;
truncate table order_item cascade;
truncate table shopping_cart cascade;
truncate table shopping_cart_item cascade;