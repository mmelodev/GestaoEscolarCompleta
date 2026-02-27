-- Script para alterar o tamanho da coluna orgaoExpeditorRg na tabela alunos
-- De: 50 caracteres
-- Para: 200 caracteres
-- 
-- ATENÇÃO: Este script é opcional. Se você estiver usando Hibernate com ddl-auto=update,
-- a coluna será atualizada automaticamente ao iniciar a aplicação.
--
-- Execute este script apenas se:
-- 1. Não estiver usando ddl-auto=update, OU
-- 2. Precisar fazer a alteração manualmente no banco de dados

USE arirang_db;

-- Alterar coluna orgaoExpeditorRg para permitir até 200 caracteres
ALTER TABLE alunos 
MODIFY COLUMN orgaoExpeditorRg VARCHAR(200);

-- Verificar se a alteração foi aplicada
DESCRIBE alunos;
