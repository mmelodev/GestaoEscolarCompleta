-- Script para corrigir problema de banco de dados existente
-- Execute este script no MySQL Workbench

-- Primeiro, tentar dropar o banco se existir (mesmo que corrompido)
DROP DATABASE IF EXISTS arirang_db;

-- Aguardar um momento para garantir que o sistema liberou o diretório
-- (No MySQL Workbench, você pode precisar executar isso em duas etapas)

-- Agora criar o banco novamente
CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verificar se foi criado com sucesso
SHOW DATABASES LIKE 'arirang_db';

-- Mostrar mensagem de sucesso
SELECT 'Banco de dados arirang_db criado com sucesso!' AS Mensagem;
