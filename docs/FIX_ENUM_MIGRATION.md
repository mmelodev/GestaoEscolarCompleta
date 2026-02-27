# 🔧 Correção de Migração de ENUM

## Problema

Ao iniciar a aplicação, aparecem warnings sobre migração de ENUM:

```
Data truncated for column 'modalidade' at row 1
Data truncated for column 'turno' at row 1
```

**Causa:** Existem dados na tabela `turma` com valores que não correspondem aos novos valores do ENUM.

## Solução

### Opção 1: Limpar dados de desenvolvimento (Mais Simples)

Se você está em ambiente de **desenvolvimento** e pode perder os dados:

**⚠️ IMPORTANTE:** Não pode simplesmente deletar `turma` porque outras tabelas referenciam ela!

**Use um dos scripts SQL fornecidos:**

#### Script 1: DELETE (Respeita foreign keys)
```bash
# Execute o arquivo CLEAN_DATABASE.sql
mysql -u root -p arirang_db < CLEAN_DATABASE.sql
```

Ou copie e cole o conteúdo do arquivo `CLEAN_DATABASE.sql` no MySQL Workbench/CLI.

#### Script 2: TRUNCATE (Mais rápido, reseta AUTO_INCREMENT)
```bash
# Execute o arquivo CLEAN_DATABASE_TRUNCATE.sql
mysql -u root -p arirang_db < CLEAN_DATABASE_TRUNCATE.sql
```

**Ambos os scripts:**
- ✅ Desabilitam temporariamente foreign keys
- ✅ Deletam dados na ordem correta
- ✅ Mantêm a estrutura das tabelas intacta
- ✅ Reabilitam foreign keys após a limpeza

**Alternativa: Resetar completamente o banco**
```sql
DROP DATABASE arirang_db;
CREATE DATABASE arirang_db;
```

### Opção 2: Atualizar dados existentes

Se você quer manter os dados e apenas corrigir os valores:

```sql
-- Conectar ao banco
mysql -u root -p arirang_db

-- Ver valores atuais
SELECT id, nome_turma, turno, modalidade FROM turma;

-- Atualizar valores inválidos para valores válidos do ENUM
UPDATE turma 
SET turno = 'MATUTINO' 
WHERE turno NOT IN ('MATUTINO', 'VESPERTINO', 'NOTURNO', 'INTEGRAL');

UPDATE turma 
SET modalidade = 'REGULAR' 
WHERE modalidade NOT IN ('REGULAR', 'INTENSIVO', 'EXTENSIVO', 'SEMI_INTENSIVO', 
                          'PREPARATORIO', 'CONVERSACAO', 'GRAMATICA', 'BUSINESS', 
                          'ACADEMICO', 'VIAGEM');
```

### Opção 3: Usar `validate` em vez de `update` (Recomendado para Produção)

Para produção, use `spring.jpa.hibernate.ddl-auto=validate` em vez de `update`.

---

## Valores Válidos dos ENUMs

### Turno
- `MATUTINO`
- `VESPERTINO`
- `NOTURNO`
- `INTEGRAL`

### Modalidade
- `REGULAR`
- `INTENSIVO`
- `EXTENSIVO`
- `SEMI_INTENSIVO`
- `PREPARATORIO`
- `CONVERSACAO`
- `GRAMATICA`
- `BUSINESS`
- `ACADEMICO`
- `VIAGEM`

---

## Após a Correção

Após corrigir os dados, a aplicação deve iniciar sem warnings de migração de ENUM.

