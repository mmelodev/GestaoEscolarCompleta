-- Script para criar o banco de dados arirang_db
-- Execute este script no MySQL Workbench ou via linha de comando
-- IMPORTANTE: Execute cada comando separadamente (não execute tudo de uma vez)

-- Passo 1: Dropar o banco se existir (resolve problema de diretório corrompido)
DROP DATABASE IF EXISTS arirang_db;

-- Passo 2: Criar o banco de dados novamente
CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Passo 3: Verificar se foi criado
SHOW DATABASES LIKE 'arirang_db';

-- Passo 4: Mostrar mensagem de sucesso
SELECT 'Banco de dados arirang_db criado com sucesso!' AS Mensagem;
