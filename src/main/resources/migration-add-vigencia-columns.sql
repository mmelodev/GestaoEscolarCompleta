-- Script para adicionar colunas de vigência na tabela contratos
-- Execute este script se as colunas ainda não existirem

-- MySQL: Adicionar colunas (se não existirem)
-- Verificar se as colunas existem antes de adicionar
SET @dbname = DATABASE();
SET @tablename = "contratos";
SET @columnname1 = "data_inicio_vigencia";
SET @columnname2 = "data_fim_vigencia";

SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname1)
  ) > 0,
  "SELECT 'Coluna data_inicio_vigencia já existe' AS resultado;",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname1, " DATE;")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (TABLE_SCHEMA = @dbname)
      AND (TABLE_NAME = @tablename)
      AND (COLUMN_NAME = @columnname2)
  ) > 0,
  "SELECT 'Coluna data_fim_vigencia já existe' AS resultado;",
  CONCAT("ALTER TABLE ", @tablename, " ADD COLUMN ", @columnname2, " DATE;")
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- Atualizar contratos existentes com datas da turma (se necessário)
UPDATE contratos c
INNER JOIN turma t ON c.turma_id = t.id
SET c.data_inicio_vigencia = COALESCE(c.data_inicio_vigencia, t.inicio_turma),
    c.data_fim_vigencia = COALESCE(c.data_fim_vigencia, t.termino_turma)
WHERE c.data_inicio_vigencia IS NULL OR c.data_fim_vigencia IS NULL;

-- Verificar resultado
SELECT 
    COUNT(*) AS total_contratos,
    COUNT(data_inicio_vigencia) AS contratos_com_inicio,
    COUNT(data_fim_vigencia) AS contratos_com_fim
FROM contratos;

