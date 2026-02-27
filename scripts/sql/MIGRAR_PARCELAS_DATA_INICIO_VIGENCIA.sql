-- ============================================
-- Script de Migração: Atualizar Parcelas para usar dataInicioVigencia
-- ============================================
-- 
-- Este script atualiza as datas de vencimento das parcelas PENDENTES
-- para usar dataInicioVigencia ao invés de dataContrato como base de cálculo.
--
-- ⚠️ ATENÇÃO: 
-- - Apenas parcelas com status 'PENDENTE' serão atualizadas
-- - Parcelas já pagas não serão alteradas
-- - Apenas contratos com dataInicioVigencia diferente de dataContrato serão processados
--
-- Data: 2026-01-09
-- ============================================

-- Verificar quantos contratos serão afetados
SELECT 
    c.id AS contrato_id,
    c.numero_contrato,
    c.data_contrato,
    c.data_inicio_vigencia,
    COUNT(p.id) AS parcelas_pendentes
FROM contratos c
INNER JOIN parcelas p ON p.contrato_id = c.id
WHERE c.data_inicio_vigencia IS NOT NULL 
  AND c.data_inicio_vigencia != c.data_contrato
  AND p.status_parcela = 'PENDENTE'
GROUP BY c.id, c.numero_contrato, c.data_contrato, c.data_inicio_vigencia
ORDER BY c.id;

-- ============================================
-- ATUALIZAÇÃO DAS PARCELAS
-- ============================================
-- 
-- Para cada contrato com dataInicioVigencia diferente de dataContrato:
-- - Primeira parcela: dataInicioVigencia + 1 mês
-- - Parcelas subsequentes: dataInicioVigencia + (numeroParcela) meses
--
-- Exemplo:
-- - dataInicioVigencia = 2024-01-15
-- - Parcela 1: 2024-02-15
-- - Parcela 2: 2024-03-15
-- - Parcela 3: 2024-04-15
-- ============================================

UPDATE parcelas p
INNER JOIN contratos c ON p.contrato_id = c.id
SET p.data_vencimento = DATE_ADD(
    DATE_ADD(c.data_inicio_vigencia, INTERVAL 1 MONTH),
    INTERVAL (p.numero_parcela - 1) MONTH
)
WHERE c.data_inicio_vigencia IS NOT NULL
  AND c.data_inicio_vigencia != c.data_contrato
  AND p.status_parcela = 'PENDENTE'
  AND p.data_vencimento != DATE_ADD(
      DATE_ADD(c.data_inicio_vigencia, INTERVAL 1 MONTH),
      INTERVAL (p.numero_parcela - 1) MONTH
  );

-- Verificar resultado
SELECT 
    c.id AS contrato_id,
    c.numero_contrato,
    p.numero_parcela,
    p.data_vencimento AS nova_data_vencimento,
    p.status_parcela
FROM contratos c
INNER JOIN parcelas p ON p.contrato_id = c.id
WHERE c.data_inicio_vigencia IS NOT NULL 
  AND c.data_inicio_vigencia != c.data_contrato
  AND p.status_parcela = 'PENDENTE'
ORDER BY c.id, p.numero_parcela;

-- ============================================
-- NOTAS IMPORTANTES:
-- ============================================
-- 1. Este script pode ser executado múltiplas vezes de forma segura
--    (é idempotente - não altera se já estiver correto)
--
-- 2. Para reverter (se necessário):
--    Execute o script reverso ou restaure do backup
--
-- 3. Recomendação: Execute primeiro em ambiente de homologação
--    e verifique os resultados antes de aplicar em produção
--
-- 4. Alternativa via código Java:
--    Use o endpoint POST /contratos/admin/migrar-parcelas
--    (requer permissão ADMIN)
-- ============================================
