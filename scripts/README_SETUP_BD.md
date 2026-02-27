# Guia de Configuração do Banco de Dados MySQL

Este guia ajuda você a configurar o banco de dados MySQL local para o projeto Arirang.

## Pré-requisitos

1. MySQL Server instalado (MySQL 8.0 ou superior recomendado)
2. MySQL Workbench instalado (opcional, mas recomendado)
3. MySQL CLI (linha de comando) acessível via PATH

## Problema Comum

Se você receber o erro:
```
Communications link failure
The last packet sent successfully to the server was 0 milliseconds ago
```

Isso significa que o Spring Boot não consegue se conectar ao MySQL.

## Solução: Criar o Banco de Dados

### Opção 1: Usando Script Automatizado (Windows)

1. Abra o PowerShell ou CMD como Administrador
2. Navegue até a pasta do projeto:
   ```powershell
   cd D:\Desktop\codespace\arirang\Arirang-plataforma\scripts
   ```
3. Execute o script:
   ```cmd
   setup_mysql.bat
   ```
4. Quando solicitado, digite a senha do MySQL root (ou pressione Enter se não tiver senha)

### Opção 2: Usando MySQL Workbench

1. Abra o MySQL Workbench
2. Conecte-se ao servidor local (localhost)
3. Abra o arquivo `scripts/create_database.sql`
4. Execute o script (Ctrl+Shift+Enter ou botão Execute)

### Opção 3: Usando Linha de Comando (CMD/PowerShell)

#### Se você NÃO tem senha no MySQL root:

```cmd
mysql -u root -e "CREATE DATABASE IF NOT EXISTS arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

#### Se você TEM senha no MySQL root:

```cmd
mysql -u root -p
```

Depois, digite sua senha quando solicitado e execute:

```sql
CREATE DATABASE IF NOT EXISTS arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### Opção 4: Verificar se MySQL está Rodando

Se o erro persistir, verifique se o MySQL está rodando:

**Windows:**
```cmd
# Verificar se o serviço MySQL está rodando
sc query MySQL80

# Se não estiver rodando, inicie:
net start MySQL80
```

**Ou via PowerShell:**
```powershell
Get-Service -Name MySQL*
```

Se o serviço estiver parado, inicie pelo Gerenciador de Serviços do Windows ou:

```powershell
Start-Service MySQL80
```

## Configurar Variável de Ambiente (Se Necessário)

Se você configurou uma senha para o MySQL root, defina a variável de ambiente:

### Windows PowerShell:
```powershell
$env:DB_PASSWORD="sua_senha_aqui"
```

### Windows CMD:
```cmd
set DB_PASSWORD=sua_senha_aqui
```

### Para Tornar Permanente (Windows):
```cmd
setx DB_PASSWORD "sua_senha_aqui"
```

⚠️ **IMPORTANTE:** Após usar `setx`, feche e reabra o terminal/IDE.

## Verificar Conexão

Após criar o banco, você pode verificar se está tudo certo:

```cmd
mysql -u root -p -e "SHOW DATABASES LIKE 'arirang_db';"
```

Você deve ver `arirang_db` na lista.

## Executar a Aplicação

Depois de criar o banco:

1. Certifique-se de que o MySQL está rodando
2. Se você tem senha, configure a variável de ambiente `DB_PASSWORD`
3. Execute a aplicação Spring Boot normalmente
4. O Hibernate criará as tabelas automaticamente (devido a `spring.jpa.hibernate.ddl-auto=update`)

## Configuração Atual do Projeto

- **Banco:** `arirang_db`
- **Host:** `localhost`
- **Porta:** `3306`
- **Usuário:** `root`
- **Senha:** Configurada via variável de ambiente `DB_PASSWORD` (vazio por padrão)

## Troubleshooting

### Erro: "MySQL não encontrado no PATH"

Adicione o MySQL ao PATH do Windows:
1. Copie o caminho do MySQL (geralmente: `C:\Program Files\MySQL\MySQL Server 8.0\bin`)
2. Adicione nas variáveis de ambiente do Windows:
   - Painel de Controle → Sistema → Configurações Avançadas do Sistema
   - Variáveis de Ambiente → Path → Editar → Novo
   - Cole o caminho e salve

### Erro: "Access denied for user 'root'@'localhost'"

1. Verifique se a senha está correta
2. Configure a variável de ambiente `DB_PASSWORD` com a senha correta
3. Ou reset a senha do MySQL se necessário

### Erro: "Can't connect to MySQL server"

1. Verifique se o MySQL está rodando:
   ```cmd
   sc query MySQL80
   ```
2. Se não estiver, inicie o serviço:
   ```cmd
   net start MySQL80
   ```
3. Verifique se a porta 3306 está disponível
