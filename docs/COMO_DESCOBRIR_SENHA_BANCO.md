# 🔍 Como Descobrir a Senha do Banco de Dados

Este guia explica onde encontrar ou recuperar a senha do banco de dados MySQL configurada no projeto.

## 📍 Onde a Senha Pode Estar

A senha do banco de dados pode estar configurada em **3 lugares principais**:

### 1. Arquivo `.env` (Recomendado para desenvolvimento)

O arquivo `.env` na raiz do projeto contém as variáveis de ambiente, incluindo a senha do banco.

**Como verificar:**

**Windows PowerShell:**
```powershell
# Verificar se o arquivo existe
Test-Path .env

# Ver a senha (se o arquivo existir)
Get-Content .env | Select-String -Pattern "^DB_PASSWORD"
```

**Linux/Mac:**
```bash
# Verificar se o arquivo existe
ls -la .env

# Ver a senha (se o arquivo existir)
grep "^DB_PASSWORD" .env
```

**⚠️ IMPORTANTE:** O arquivo `.env` **NÃO** deve ser commitado no Git (está no `.gitignore`).

### 2. Variáveis de Ambiente do Sistema

A senha pode estar configurada como variável de ambiente do Windows.

**Como verificar:**

**Windows PowerShell:**
```powershell
# Ver variável de ambiente da sessão atual
$env:DB_PASSWORD

# Ver variável de ambiente do usuário (permanente)
[System.Environment]::GetEnvironmentVariable("DB_PASSWORD", "User")

# Ver variável de ambiente do sistema (requer admin)
[System.Environment]::GetEnvironmentVariable("DB_PASSWORD", "Machine")
```

**Windows CMD:**
```cmd
echo %DB_PASSWORD%
```

**Linux/Mac:**
```bash
echo $DB_PASSWORD
```

### 3. Serviços de Deploy (Produção)

Se o projeto estiver em produção (Railway, Render, AWS, etc.), a senha está configurada nas variáveis de ambiente do serviço.

**Como verificar:**

- **Railway:** Dashboard → Seu projeto → Variables → `DB_PASSWORD`
- **Render:** Dashboard → Seu serviço → Environment → `DB_PASSWORD`
- **AWS:** AWS Console → Secrets Manager ou Systems Manager Parameter Store

## 🔧 Se Você Não Sabe a Senha

### Opção 1: Verificar no MySQL

Se você tem acesso ao MySQL, pode verificar ou redefinir a senha:

```sql
-- Conectar ao MySQL (sem senha ou com senha que você sabe)
mysql -u root -p

-- Ver usuários e hosts
SELECT user, host FROM mysql.user WHERE user = 'root';

-- Redefinir senha do root (se necessário)
ALTER USER 'root'@'localhost' IDENTIFIED BY 'nova_senha_aqui';
FLUSH PRIVILEGES;
```

### Opção 2: Verificar em Arquivos de Configuração Antigos

Se você salvou a senha em algum lugar:

**Windows:**
```powershell
# Procurar em arquivos de texto
Select-String -Path "*.txt","*.md","*.env*" -Pattern "DB_PASSWORD|senha.*mysql|password.*mysql" -CaseSensitive:$false
```

**Linux/Mac:**
```bash
grep -r "DB_PASSWORD\|senha.*mysql\|password.*mysql" . --include="*.txt" --include="*.md" --include="*.env*" -i
```

### Opção 3: Redefinir a Senha do MySQL

Se você não consegue descobrir a senha, pode redefinir:

**Windows:**

1. Pare o serviço MySQL:
```powershell
Stop-Service MySQL80
# ou
net stop MySQL80
```

2. Inicie o MySQL em modo seguro (sem verificação de senha):
```powershell
mysqld --skip-grant-tables --console
```

3. Em outro terminal, conecte sem senha:
```powershell
mysql -u root
```

4. Redefina a senha:
```sql
USE mysql;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'nova_senha_aqui';
FLUSH PRIVILEGES;
EXIT;
```

5. Pare o MySQL e reinicie normalmente:
```powershell
# Pare o MySQL em modo seguro (Ctrl+C)
# Reinicie o serviço
Start-Service MySQL80
```

**Linux/Mac:**
```bash
# Pare o MySQL
sudo systemctl stop mysql
# ou
sudo service mysql stop

# Inicie em modo seguro
sudo mysqld_safe --skip-grant-tables &

# Conecte sem senha
mysql -u root

# Redefina a senha (mesmo SQL acima)
# Depois reinicie normalmente
sudo systemctl start mysql
```

## ✅ Verificar se a Senha Está Correta

Depois de descobrir ou redefinir a senha, teste a conexão:

**Windows PowerShell:**
```powershell
# Testar conexão
$env:DB_PASSWORD="sua_senha_aqui"
mysql -u root -p$env:DB_PASSWORD -e "SELECT 1"
```

**Linux/Mac:**
```bash
mysql -u root -p"sua_senha_aqui" -e "SELECT 1"
```

## 📝 Configurar a Senha no Projeto

Depois de descobrir ou redefinir a senha, configure no projeto:

### Opção 1: Arquivo `.env` (Recomendado)

1. Copie o arquivo de exemplo:
```powershell
# Windows
Copy-Item env.example .env

# Linux/Mac
cp env.example .env
```

2. Edite o arquivo `.env` e configure:
```env
DB_PASSWORD=sua_senha_aqui
```

### Opção 2: Variável de Ambiente Permanente

**Windows PowerShell:**
```powershell
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "sua_senha_aqui", "User")
```

**Windows CMD:**
```cmd
setx DB_PASSWORD "sua_senha_aqui"
```

**Linux/Mac:**
```bash
echo 'export DB_PASSWORD="sua_senha_aqui"' >> ~/.bashrc
source ~/.bashrc
```

## 🔒 Segurança

⚠️ **IMPORTANTE:**
- **NUNCA** commite a senha no Git
- **NUNCA** compartilhe a senha publicamente
- Use um gerenciador de senhas para armazenar credenciais
- Em produção, use serviços de secrets (AWS Secrets Manager, etc.)

## 📚 Documentação Relacionada

- `docs/deploy/SETUP_ENV.md` - Configuração completa de variáveis de ambiente
- `scripts/CONFIGURAR_SENHA_MYSQL.md` - Guia detalhado de configuração
- `README.md` - Seção "🚀 Como Executar o Projeto"
