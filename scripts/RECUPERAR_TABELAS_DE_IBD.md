# Como Recuperar Tabelas a partir de Arquivos .ibd

## ⚠️ Situação Atual

Você tem apenas os arquivos `.ibd` (dados), mas as tabelas não existem no MySQL. Isso acontece porque:
- **Arquivos `.ibd`** = Dados e índices das tabelas
- **Estrutura da tabela** = Definição (CREATE TABLE) que está faltando

## 🎯 Solução Recomendada: Deixar Hibernate Recriar

Como você tem as **entidades JPA** no código, a melhor solução é deixar o Hibernate recriar as tabelas automaticamente.

### Passo 1: Verificar Configuração do Hibernate

No arquivo `application-dev.properties`, você já tem:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Isso significa que o Hibernate vai:
- ✅ Criar tabelas que não existem
- ✅ Atualizar estrutura de tabelas existentes
- ❌ **NÃO deleta dados existentes**

### Passo 2: Deixar Hibernate Criar as Tabelas

1. **Certifique-se de que o banco `arirang_db` existe:**
   ```sql
   CREATE DATABASE IF NOT EXISTS arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Execute a aplicação Spring Boot normalmente**
   - O Hibernate vai criar todas as tabelas automaticamente baseado nas entidades JPA
   - As tabelas serão criadas vazias (sem dados)

### Passo 3: Importar Dados dos Arquivos .ibd (Opcional)

Se você precisa recuperar os dados dos arquivos `.ibd`, você precisará:

#### Opção A: Se você tem backup SQL (Recomendado)

Se você tem um backup SQL (dump), restaure:

```sql
-- No MySQL Workbench ou linha de comando
SOURCE caminho/para/seu/backup.sql;
```

#### Opção B: Importar Tablespace (Avançado)

⚠️ **CUIDADO:** Isso só funciona se:
- Os arquivos `.ibd` estão no diretório correto do MySQL
- Você tem a estrutura exata das tabelas
- Os arquivos não estão corrompidos

**Passos:**

1. **Criar tabelas vazias** (o Hibernate já fez isso)

2. **Descartar tablespace:**
   ```sql
   USE arirang_db;
   ALTER TABLE alunos DISCARD TABLESPACE;
   ```

3. **Copiar arquivo .ibd para o diretório do MySQL:**
   - Localização: `C:\ProgramData\MySQL\MySQL Server 8.0\Data\arirang_db\`
   - Copie `alunos.ibd` para lá

4. **Importar tablespace:**
   ```sql
   ALTER TABLE alunos IMPORT TABLESPACE;
   ```

5. **Repetir para cada tabela**

## 🔄 Solução Mais Simples: Recriar do Zero

Se você não tem backup SQL e os dados não são críticos:

1. **Dropar o banco:**
   ```sql
   DROP DATABASE IF EXISTS arirang_db;
   ```

2. **Recriar:**
   ```sql
   CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

3. **Executar a aplicação:**
   - O Hibernate criará todas as tabelas vazias
   - Você pode inserir dados manualmente ou via interface

## 📋 Verificar Tabelas Criadas

Após executar a aplicação, verifique:

```sql
USE arirang_db;
SHOW TABLES;
```

Você deve ver todas as tabelas:
- `alunos`
- `turmas`
- `contratos`
- `usuarios`
- `professores`
- etc.

## ⚠️ Importante sobre Arquivos .ibd

**Arquivos `.ibd` sozinhos NÃO são suficientes para recuperar tabelas porque:**

1. **Falta a estrutura** (CREATE TABLE statement)
2. **Falta metadados** do MySQL sobre a tabela
3. **Pode estar corrompido** se foi copiado incorretamente

**Para recuperar dados de `.ibd`, você precisa:**
- ✅ Estrutura da tabela (CREATE TABLE)
- ✅ Arquivo `.ibd` intacto
- ✅ Mesma versão do MySQL
- ✅ Mesmo charset e collation

## 🎯 Recomendação Final

**Para seu caso específico:**

1. ✅ Deixe o Hibernate criar as tabelas (já configurado)
2. ✅ Execute a aplicação Spring Boot
3. ✅ As tabelas serão criadas automaticamente
4. ❌ Se você precisa dos dados antigos, você precisaria de um backup SQL completo

**Se você tem backup:**
- Use `mysqldump` para restaurar
- Ou importe via MySQL Workbench

**Se você NÃO tem backup:**
- As tabelas serão criadas vazias
- Você precisará inserir dados manualmente ou via interface da aplicação
