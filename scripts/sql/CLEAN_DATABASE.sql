-- ============================================
-- Script para Limpar Dados de Desenvolvimento
-- ============================================
-- ⚠️ ATENÇÃO: Este script DELETA TODOS OS DADOS!
-- Use apenas em ambiente de DESENVOLVIMENTO
-- ============================================

USE arirang_db;

-- Desabilitar verificação de foreign keys temporariamente
SET FOREIGN_KEY_CHECKS = 0;

-- Deletar dados na ordem correta (respeitando foreign keys)
-- Começar pelas tabelas que referenciam outras (filhas)

-- Tabelas de relacionamento Many-to-Many
DELETE FROM aluno_turma;

-- Tabelas que dependem de outras
-- IMPORTANTE: Ordem de deleção respeitando foreign keys
DELETE FROM notas_avaliacao;       -- Depende de avaliacao
DELETE FROM notas;                 -- Depende de boletim
DELETE FROM comprovantes_pagamento; -- Depende de pagamento e receita
DELETE FROM pagamentos;            -- Depende de receita
DELETE FROM receitas;              -- Depende de contrato e aluno
DELETE FROM financeiro;            -- Depende de contrato, parcela, aluno
DELETE FROM parcelas;              -- Depende de contrato
DELETE FROM avaliacoes;            -- Depende de turma (FK turma_id)
DELETE FROM boletins;              -- Depende de turma (FK turma_id) e aluno
DELETE FROM contratos;             -- Depende de aluno e turma (FK turma_id)

-- Tabelas principais
DELETE FROM turma;                 -- Depende de nenhuma (mas é referenciada)
DELETE FROM alunos;                -- Depende de responsavel (mas pode ter relacionamento)
DELETE FROM responsaveis;          -- Depende de nenhuma

-- Manter usuários (para não perder acesso ao sistema)
-- DELETE FROM usuarios;            -- Descomente se quiser deletar usuários também

-- Reabilitar verificação de foreign keys
SET FOREIGN_KEY_CHECKS = 1;

-- Verificar se está limpo
SELECT 'Tabelas limpas com sucesso!' AS Status;
SELECT COUNT(*) AS TotalTurmas FROM turma;
SELECT COUNT(*) AS TotalAlunos FROM alunos;
SELECT COUNT(*) AS TotalContratos FROM contratos;
