-- Script para recriar o banco de dados e deixar Hibernate criar as tabelas
-- Execute este script no MySQL Workbench ANTES de executar a aplicação Spring Boot

-- Passo 1: Dropar o banco existente (isso remove todos os arquivos .ibd também)
DROP DATABASE IF EXISTS arirang_db;

-- Passo 2: Criar o banco novamente
CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Passo 3: Verificar se foi criado
SHOW DATABASES LIKE 'arirang_db';

-- Passo 4: Selecionar o banco
USE arirang_db;

-- Passo 5: Verificar tabelas (deve estar vazio agora)
SHOW TABLES;

-- ============================================
-- PRÓXIMOS PASSOS:
-- ============================================
-- 1. Execute a aplicação Spring Boot
-- 2. O Hibernate criará todas as tabelas automaticamente
-- 3. Verifique novamente: SHOW TABLES;
-- ============================================

SELECT 'Banco recriado com sucesso! Execute a aplicação Spring Boot para criar as tabelas.' AS Mensagem;
