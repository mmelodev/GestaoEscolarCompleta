-- ============================================
-- Script Alternativo: TRUNCATE (Mais Rápido)
-- ============================================
-- ⚠️ ATENÇÃO: Este script DELETA TODOS OS DADOS!
-- TRUNCATE é mais rápido que DELETE e reseta AUTO_INCREMENT
-- Use apenas em ambiente de DESENVOLVIMENTO
-- ============================================

USE arirang_db;

-- Desabilitar verificação de foreign keys
SET FOREIGN_KEY_CHECKS = 0;

-- TRUNCATE remove todos os dados e reseta AUTO_INCREMENT
-- Ordem: primeiro tabelas filhas (com foreign keys), depois tabelas pai

-- Tabela de relacionamento Many-to-Many
TRUNCATE TABLE aluno_turma;

-- Tabelas que dependem de outras (filhas)
TRUNCATE TABLE notas_avaliacao;      -- Depende de avaliacao
TRUNCATE TABLE notas;                -- Depende de boletim
TRUNCATE TABLE comprovantes_pagamento; -- Depende de pagamento e receita
TRUNCATE TABLE pagamentos;           -- Depende de receita
TRUNCATE TABLE receitas;             -- Depende de contrato e aluno
TRUNCATE TABLE financeiro;           -- Depende de contrato, parcela, aluno
TRUNCATE TABLE parcelas;             -- Depende de contrato
TRUNCATE TABLE avaliacoes;           -- Depende de turma (FK turma_id)
TRUNCATE TABLE boletins;             -- Depende de turma (FK turma_id) e aluno
TRUNCATE TABLE contratos;            -- Depende de turma (FK turma_id) e aluno

-- Tabelas principais (pais)
TRUNCATE TABLE turma;                -- Referenciada por boletins, contratos, avaliacoes
TRUNCATE TABLE alunos;               -- Referenciado por contratos, boletins, etc.
TRUNCATE TABLE responsaveis;         -- Referenciado por alunos
-- TRUNCATE TABLE usuarios;  -- Descomente se quiser limpar usuários

-- Reabilitar verificação de foreign keys
SET FOREIGN_KEY_CHECKS = 1;

-- Verificar limpeza
SELECT 'Dados truncados com sucesso!' AS Status;
SELECT 
    (SELECT COUNT(*) FROM turma) AS Turmas,
    (SELECT COUNT(*) FROM alunos) AS Alunos,
    (SELECT COUNT(*) FROM contratos) AS Contratos,
    (SELECT COUNT(*) FROM boletins) AS Boletins;

