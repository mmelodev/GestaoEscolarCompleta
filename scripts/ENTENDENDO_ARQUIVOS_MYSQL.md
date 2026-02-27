# Entendendo os Arquivos do MySQL

## Arquivos .ibd (InnoDB Data Files)

Os arquivos `.ibd` são **arquivos de dados do InnoDB**, o engine de armazenamento padrão do MySQL/MariaDB.

### O que são?

- **`.ibd`** = **InnoDB Data File**
- Cada arquivo `.ibd` representa uma **tabela** do banco de dados
- Contém os **dados** e **índices** da tabela
- Formato: `nome_da_tabela.ibd`

### Estrutura da Pasta do Banco

Quando você olha dentro de `arirang_db`, você verá:

```
arirang_db/
├── aluno.ibd              ← Tabela "aluno"
├── turma.ibd               ← Tabela "turma"
├── contrato.ibd            ← Tabela "contrato"
├── professor.ibd           ← Tabela "professor"
├── db.opt                  ← Opções do banco (charset, collation)
└── ... (outros arquivos .ibd)
```

### Outros Arquivos Comuns

- **`.frm`** (MySQL 5.7 e anteriores): Estrutura da tabela
- **`.ibd`**: Dados e índices da tabela (InnoDB)
- **`db.opt`**: Configurações do banco (charset, collation)
- **`.par`**: Arquivos de partição (se a tabela for particionada)

### Por que existem?

1. **Isolamento**: Cada tabela tem seu próprio arquivo de dados
2. **Performance**: Permite otimizações por tabela
3. **Manutenção**: Facilita backup e restauração individual
4. **Espaço**: Permite gerenciar espaço por tabela

### Posso Deletar?

⚠️ **NUNCA delete arquivos .ibd manualmente!**

- Esses arquivos contêm os **dados reais** das suas tabelas
- Deletar manualmente pode corromper o banco de dados
- Use sempre comandos SQL (`DROP TABLE`, `DROP DATABASE`)

### Quando Ver Arquivos .ibd Corrompidos?

Se você vê arquivos `.ibd` órfãos (sem tabela correspondente), pode indicar:

1. **Tabela foi deletada incorretamente**
2. **Banco foi corrompido**
3. **Migração incompleta**

### Como Limpar Corretamente?

Se você precisa limpar o banco:

```sql
-- Remover todas as tabelas
DROP DATABASE arirang_db;

-- Recriar o banco
CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

O MySQL removerá automaticamente todos os arquivos `.ibd` quando você dropar o banco.

### Verificar Tabelas no Banco

Para ver quais tabelas existem:

```sql
USE arirang_db;
SHOW TABLES;
```

Cada tabela listada terá um arquivo `.ibd` correspondente.

### Backup e Restauração

- **Backup**: Use `mysqldump` ou ferramentas de backup do MySQL
- **NÃO copie arquivos .ibd manualmente** - isso não funciona corretamente
- Use sempre ferramentas oficiais do MySQL

### Resumo

| Arquivo | O que é | Posso deletar? |
|---------|---------|----------------|
| `.ibd` | Dados da tabela | ❌ NUNCA manualmente |
| `.frm` | Estrutura (MySQL antigo) | ❌ NUNCA manualmente |
| `db.opt` | Configurações do banco | ❌ NUNCA manualmente |

**Regra de Ouro**: Sempre use comandos SQL para gerenciar o banco, nunca manipule arquivos diretamente!
