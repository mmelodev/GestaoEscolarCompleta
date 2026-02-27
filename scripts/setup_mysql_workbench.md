# Como Criar o Banco de Dados usando MySQL Workbench

Como o MySQL não está no PATH do Windows, a forma mais fácil é usar o MySQL Workbench.

## Passo a Passo

### 1. Abrir MySQL Workbench

1. Abra o MySQL Workbench
2. Conecte-se ao servidor local (`localhost` ou `127.0.0.1`)
   - Clique em "Local instance MySQL" ou crie uma nova conexão
   - Use usuário: `root`
   - Digite sua senha se tiver configurado

### 2. Executar Script SQL

**Opção A: Usar o arquivo SQL fornecido**

1. No MySQL Workbench, vá em **File → Open SQL Script**
2. Navegue até: `Arirang-plataforma\scripts\create_database.sql`
3. Abra o arquivo
4. Clique no botão **Execute** (raio) ou pressione **Ctrl+Shift+Enter**

**Opção B: Executar comando direto**

1. No MySQL Workbench, abra uma nova query tab
2. Cole o seguinte comando:

```sql
CREATE DATABASE IF NOT EXISTS arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. Execute (botão raio ou Ctrl+Shift+Enter)
4. Verifique se foi criado:
   ```sql
   SHOW DATABASES LIKE 'arirang_db';
   ```

### 3. Verificar se Funcionou

Você deve ver `arirang_db` na lista de bancos de dados no painel esquerdo do Workbench.

### 4. Configurar Variável de Ambiente (Se Necessário)

Se você tem senha no MySQL root, configure antes de executar a aplicação:

**No PowerShell:**
```powershell
$env:DB_PASSWORD="sua_senha_aqui"
```

**No CMD:**
```cmd
set DB_PASSWORD=sua_senha_aqui
```

**Para tornar permanente:**
```cmd
setx DB_PASSWORD "sua_senha_aqui"
```

⚠️ **IMPORTANTE:** Após usar `setx`, feche e reabra o terminal/IDE.

### 5. Executar a Aplicação

Agora você pode executar a aplicação Spring Boot normalmente. O Hibernate criará as tabelas automaticamente.
