-- Script para corrigir valores inválidos de Enums no banco de dados
-- Este script corrige valores que foram salvos incorretamente

-- ============================================
-- 1. CORRIGIR MODALIDADE
-- ============================================

-- Verificar valores inválidos de Modalidade
SELECT id, nome_turma, modalidade, formato 
FROM turma 
WHERE modalidade NOT IN ('REGULAR', 'INTENSIVO', 'EXTENSIVO', 'SEMI_INTENSIVO', 
                          'PREPARATORIO', 'CONVERSACAO', 'GRAMATICA', 'BUSINESS', 
                          'ACADEMICO', 'VIAGEM')
   OR modalidade = 'Presencial';  -- "Presencial" é um valor de Formato, não Modalidade

-- Corrigir Modalidade inválida (definir como NULL)
UPDATE turma 
SET modalidade = NULL 
WHERE modalidade NOT IN ('REGULAR', 'INTENSIVO', 'EXTENSIVO', 'SEMI_INTENSIVO', 
                          'PREPARATORIO', 'CONVERSACAO', 'GRAMATICA', 'BUSINESS', 
                          'ACADEMICO', 'VIAGEM')
   OR modalidade = 'Presencial';

-- ============================================
-- 2. CORRIGIR TURNO
-- ============================================

-- Verificar valores inválidos de Turno
SELECT id, nome_turma, turno 
FROM turma 
WHERE turno NOT IN ('MATUTINO', 'VESPERTINO', 'NOTURNO', 'INTEGRAL')
   OR turno LIKE '%Manhã%' 
   OR turno LIKE '%Manha%'
   OR turno LIKE '%Tarde%'
   OR turno LIKE '%Noite%';

-- Corrigir Turno inválido - Mapear valores comuns
UPDATE turma 
SET turno = 'MATUTINO' 
WHERE turno IN ('Manhã', 'Manha', 'MANHÃ', 'MANHA', 'Manhã', 'Manhã');

UPDATE turma 
SET turno = 'VESPERTINO' 
WHERE turno IN ('Tarde', 'TARDE', 'Vespertino');

UPDATE turma 
SET turno = 'NOTURNO' 
WHERE turno IN ('Noite', 'NOITE', 'Noturno');

-- Definir como NULL valores que não puderam ser mapeados
UPDATE turma 
SET turno = NULL 
WHERE turno NOT IN ('MATUTINO', 'VESPERTINO', 'NOTURNO', 'INTEGRAL');

-- ============================================
-- 3. VERIFICAÇÃO FINAL
-- ============================================

-- Verificar turmas com valores NULL após correção
SELECT id, nome_turma, turno, formato, modalidade 
FROM turma 
WHERE turno IS NULL OR modalidade IS NULL;

-- ============================================
-- VALORES VÁLIDOS DOS ENUMS
-- ============================================

-- Turno: MATUTINO, VESPERTINO, NOTURNO, INTEGRAL
-- Formato: PRESENCIAL, ONLINE, HIBRIDO, SEMIPRESENCIAL
-- Modalidade: REGULAR, INTENSIVO, EXTENSIVO, SEMI_INTENSIVO, 
--             PREPARATORIO, CONVERSACAO, GRAMATICA, BUSINESS, 
--             ACADEMICO, VIAGEM

