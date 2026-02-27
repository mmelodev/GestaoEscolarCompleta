# Como Resolver o Erro: "Schema directory already exists"

Este erro ocorre quando o diretório do banco de dados existe no sistema de arquivos, mas está corrompido ou incompleto.

## Solução 1: Dropar e Recriar (Recomendado)

Execute no MySQL Workbench, **uma linha por vez**:

### Passo 1: Dropar o banco existente
```sql
DROP DATABASE IF EXISTS arirang_db;
```

Execute esta linha e aguarde a confirmação.

### Passo 2: Criar o banco novamente
```sql
CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Passo 3: Verificar
```sql
SHOW DATABASES LIKE 'arirang_db';
```

## Solução 2: Se a Solução 1 não funcionar

Se o erro persistir, o diretório físico ainda existe. Você precisará:

### Opção A: Via MySQL Workbench (Mais Seguro)

1. Feche o MySQL Workbench completamente
2. Abra o MySQL Workbench novamente
3. Conecte-se ao servidor
4. Execute novamente o script `fix_database.sql` (uma linha por vez)

### Opção B: Remover Manualmente o Diretório (Avançado)

⚠️ **CUIDADO:** Só faça isso se você tiver certeza de que não precisa dos dados!

1. Pare o serviço MySQL:
   ```powershell
   Stop-Service MySQL80
   ```
   (ou pelo Gerenciador de Serviços do Windows)

2. Navegue até o diretório de dados do MySQL:
   - Geralmente: `C:\ProgramData\MySQL\MySQL Server 8.0\Data\`
   - Ou: `C:\Program Files\MySQL\MySQL Server 8.0\data\`

3. Procure pela pasta `arirang_db`

4. **DELETE a pasta `arirang_db`** (se existir)

5. Inicie o serviço MySQL novamente:
   ```powershell
   Start-Service MySQL80
   ```

6. Abra o MySQL Workbench e execute:
   ```sql
   CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

## Solução 3: Usar Comando SQL com FORCE (MySQL 8.0+)

Se você tem MySQL 8.0 ou superior, pode tentar:

```sql
DROP DATABASE IF EXISTS arirang_db;
```

Aguarde alguns segundos, depois:

```sql
CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## Verificar se Funcionou

Após criar o banco, verifique:

```sql
USE arirang_db;
SHOW TABLES;
```

Se não houver erro, o banco está pronto para uso!
