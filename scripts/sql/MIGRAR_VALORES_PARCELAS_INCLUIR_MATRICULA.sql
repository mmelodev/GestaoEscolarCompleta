-- ============================================
-- Script de Migração: Atualizar Valores de Parcelas para Incluir Matrícula
-- ============================================
-- 
-- Este script atualiza os valores das parcelas PENDENTES dos contratos existentes
-- para refletir a nova lógica:
-- - Primeira parcela: valorMatricula + valorMensalidade
-- - Demais parcelas: apenas valorMensalidade
--
-- ⚠️ ATENÇÃO: 
-- - Apenas parcelas com status 'PENDENTE' serão atualizadas
-- - Parcelas já pagas não serão alteradas (preservação de histórico)
-- - Apenas contratos com valorMatricula e valorMensalidade válidos serão processados
--
-- Data: 2026-01-09
-- ============================================

-- Verificar quantos contratos serão afetados
SELECT 
    c.id AS contrato_id,
    c.numero_contrato,
    c.valor_matricula,
    c.valor_mensalidade,
    COUNT(CASE WHEN p.numero_parcela = 1 AND p.status_parcela = 'PENDENTE' THEN 1 END) AS primeira_parcela_pendente,
    COUNT(CASE WHEN p.numero_parcela > 1 AND p.status_parcela = 'PENDENTE' THEN 1 END) AS demais_parcelas_pendentes
FROM contratos c
INNER JOIN parcelas p ON p.contrato_id = c.id
WHERE c.valor_mensalidade IS NOT NULL 
  AND c.valor_mensalidade > 0
  AND p.status_parcela = 'PENDENTE'
GROUP BY c.id, c.numero_contrato, c.valor_matricula, c.valor_mensalidade
ORDER BY c.id;

-- ============================================
-- ATUALIZAÇÃO DOS VALORES DAS PARCELAS
-- ============================================
-- 
-- Para cada contrato:
-- - Primeira parcela PENDENTE: valorMatricula + valorMensalidade
-- - Demais parcelas PENDENTES: apenas valorMensalidade
--
-- Exemplo:
-- - valorMatricula = R$ 200,00
-- - valorMensalidade = R$ 200,00
-- - Primeira parcela: R$ 400,00 (200 + 200)
-- - Parcelas 2-6: R$ 200,00 cada
-- ============================================

-- Atualizar primeira parcela (incluir matrícula)
UPDATE parcelas p
INNER JOIN contratos c ON p.contrato_id = c.id
SET p.valor_parcela = COALESCE(c.valor_matricula, 0) + c.valor_mensalidade
WHERE p.numero_parcela = 1
  AND p.status_parcela = 'PENDENTE'
  AND c.valor_mensalidade IS NOT NULL
  AND c.valor_mensalidade > 0
  AND p.valor_parcela != (COALESCE(c.valor_matricula, 0) + c.valor_mensalidade);

-- Atualizar demais parcelas (apenas mensalidade)
UPDATE parcelas p
INNER JOIN contratos c ON p.contrato_id = c.id
SET p.valor_parcela = c.valor_mensalidade
WHERE p.numero_parcela > 1
  AND p.status_parcela = 'PENDENTE'
  AND c.valor_mensalidade IS NOT NULL
  AND c.valor_mensalidade > 0
  AND p.valor_parcela != c.valor_mensalidade;

-- Verificar resultado
SELECT 
    c.id AS contrato_id,
    c.numero_contrato,
    p.numero_parcela,
    p.valor_parcela AS novo_valor_parcela,
    p.status_parcela,
    CASE 
        WHEN p.numero_parcela = 1 THEN COALESCE(c.valor_matricula, 0) + c.valor_mensalidade
        ELSE c.valor_mensalidade
    END AS valor_esperado
FROM contratos c
INNER JOIN parcelas p ON p.contrato_id = c.id
WHERE c.valor_mensalidade IS NOT NULL 
  AND c.valor_mensalidade > 0
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
--    Use o endpoint POST /contratos/admin/migrar-valores-parcelas
--    (requer permissão ADMIN)
--
-- 5. IMPORTANTE: Parcelas já pagas NÃO serão alteradas para preservar
--    o histórico financeiro e evitar inconsistências
-- ============================================
