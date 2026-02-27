-- Script para corrigir valores inválidos de Modalidade no banco de dados
-- Este script corrige valores que foram salvos incorretamente (ex: "Presencial" que é um valor de Formato, não Modalidade)

-- Verificar valores inválidos antes de corrigir
SELECT id, nome_turma, modalidade, formato 
FROM turma 
WHERE modalidade NOT IN ('REGULAR', 'INTENSIVO', 'EXTENSIVO', 'SEMI_INTENSIVO', 
                          'PREPARATORIO', 'CONVERSACAO', 'GRAMATICA', 'BUSINESS', 
                          'ACADEMICO', 'VIAGEM')
   OR modalidade IS NULL;

-- Opção 1: Definir como NULL (recomendado se não souber qual valor correto)
UPDATE turma 
SET modalidade = NULL 
WHERE modalidade NOT IN ('REGULAR', 'INTENSIVO', 'EXTENSIVO', 'SEMI_INTENSIVO', 
                          'PREPARATORIO', 'CONVERSACAO', 'GRAMATICA', 'BUSINESS', 
                          'ACADEMICO', 'VIAGEM')
   OR modalidade = 'Presencial';  -- "Presencial" é um valor de Formato, não Modalidade

-- Opção 2: Definir como REGULAR (valor padrão mais comum)
-- UPDATE turma 
-- SET modalidade = 'REGULAR' 
-- WHERE modalidade NOT IN ('REGULAR', 'INTENSIVO', 'EXTENSIVO', 'SEMI_INTENSIVO', 
--                           'PREPARATORIO', 'CONVERSACAO', 'GRAMATICA', 'BUSINESS', 
--                           'ACADEMICO', 'VIAGEM')
--    OR modalidade = 'Presencial';

-- Verificar após correção
SELECT id, nome_turma, modalidade, formato 
FROM turma 
WHERE modalidade IS NULL;

-- Valores válidos de Modalidade:
-- REGULAR, INTENSIVO, EXTENSIVO, SEMI_INTENSIVO, PREPARATORIO, 
-- CONVERSACAO, GRAMATICA, BUSINESS, ACADEMICO, VIAGEM

-- Nota: "Presencial" é um valor válido de FORMATO, não de MODALIDADE
-- Valores válidos de Formato: PRESENCIAL, ONLINE, HIBRIDO, SEMIPRESENCIAL

