-- ⚠️ ATENÇÃO: Este script deleta TODOS os dados relacionados a contratos
-- Faça backup antes de executar!
-- 
-- Como executar:
-- MySQL: mysql -u root -p arirang_db < src/main/resources/cleanup-contratos.sql
-- H2: Execute via console SQL ou Spring Boot

-- Desabilitar verificação de chaves estrangeiras temporariamente (MySQL)
SET FOREIGN_KEY_CHECKS = 0;

-- Deletar em ordem (respeitando dependências)
-- 1. Deletar pagamentos vinculados a parcelas
DELETE FROM pagamentos WHERE parcela_id IN (SELECT id FROM parcelas WHERE contrato_id IS NOT NULL);

-- 2. Deletar comprovantes vinculados a parcelas
DELETE FROM comprovantes_pagamento WHERE parcela_id IN (SELECT id FROM parcelas WHERE contrato_id IS NOT NULL);

-- 3. Deletar receitas vinculadas a parcelas
DELETE FROM receitas WHERE parcela_id IN (SELECT id FROM parcelas WHERE contrato_id IS NOT NULL);

-- 4. Deletar registros financeiros vinculados a parcelas
DELETE FROM financeiro WHERE parcela_id IN (SELECT id FROM parcelas WHERE contrato_id IS NOT NULL);

-- 5. Deletar parcelas vinculadas a contratos
DELETE FROM parcelas WHERE contrato_id IS NOT NULL;

-- 6. Deletar registros financeiros vinculados diretamente a contratos
DELETE FROM financeiro WHERE contrato_id IS NOT NULL;

-- 7. Deletar comprovantes vinculados diretamente a contratos
DELETE FROM comprovantes_pagamento WHERE contrato_id IS NOT NULL;

-- 8. Deletar contratos
DELETE FROM contratos;

-- Reabilitar verificação de chaves estrangeiras (MySQL)
SET FOREIGN_KEY_CHECKS = 1;

-- Verificar limpeza
SELECT COUNT(*) as contratos_restantes FROM contratos;
SELECT COUNT(*) as parcelas_restantes FROM parcelas;
SELECT COUNT(*) as pagamentos_restantes FROM pagamentos WHERE parcela_id IS NOT NULL;

COMMIT;

