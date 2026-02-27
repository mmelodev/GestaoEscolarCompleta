# 🔧 Configuração de Variáveis de Ambiente - Windows

## ⚠️ PROBLEMA: Erro de Conexão com MySQL

**Erro:** `Access denied for user 'root'@'localhost' (using password: NO)`

**Causa:** A senha do banco de dados não está configurada como variável de ambiente.

## 📋 SOLUÇÕES

### Opção 1: Configurar via PowerShell (Temporária - apenas para a sessão atual)

Execute no PowerShell (dentro da pasta `plataforma`):

```powershell
# ⚠️ IMPORTANTE: Substitua os valores abaixo pelas suas credenciais reais
# ⚠️ NUNCA commite estas credenciais no repositório Git!
$env:DB_PASSWORD="sua_senha_mysql_aqui"
$env:JWT_SECRET="sua_chave_jwt_secreta_minimo_32_caracteres_aqui"
$env:APP_DEFAULT_ADMIN_PASSWORD="senha_admin_desejada"
$env:APP_DEFAULT_TEST_PASSWORD="senha_teste_desejada"

# Depois execute a aplicação
mvn spring-boot:run
```

### Opção 2: Configurar via PowerShell (Permanente - apenas para o usuário)

```powershell
# ⚠️ IMPORTANTE: Substitua os valores abaixo pelas suas credenciais reais
# ⚠️ NUNCA commite estas credenciais no repositório Git!
[System.Environment]::SetEnvironmentVariable("DB_PASSWORD", "sua_senha_mysql_aqui", "User")
[System.Environment]::SetEnvironmentVariable("JWT_SECRET", "sua_chave_jwt_secreta_minimo_32_caracteres_aqui", "User")
[System.Environment]::SetEnvironmentVariable("APP_DEFAULT_ADMIN_PASSWORD", "senha_admin_desejada", "User")
[System.Environment]::SetEnvironmentVariable("APP_DEFAULT_TEST_PASSWORD", "senha_teste_desejada", "User")
```

**Nota:** Feche e reabra o terminal após executar estes comandos.

### Opção 3: Criar arquivo `.env` (Recomendado para desenvolvimento)

**⚠️ IMPORTANTE:** Spring Boot não lê arquivos `.env` nativamente. Você precisa de uma biblioteca adicional ou configurar manualmente via script.

#### Usando um script PowerShell para carregar .env

1. Crie um arquivo `.env` na pasta `plataforma/`:

```env
# ⚠️ IMPORTANTE: Substitua os valores abaixo pelas suas credenciais reais
# ⚠️ NUNCA commite este arquivo .env no repositório Git!
DB_PASSWORD=sua_senha_mysql_aqui
JWT_SECRET=sua_chave_jwt_secreta_minimo_32_caracteres_aqui
APP_DEFAULT_ADMIN_PASSWORD=senha_admin_desejada
APP_DEFAULT_TEST_PASSWORD=senha_teste_desejada
```

2. Crie um script `run.ps1` na pasta `plataforma/`:

```powershell
# Carregar variáveis do arquivo .env
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}

# Executar aplicação
mvn spring-boot:run
```

3. Execute o script:

```powershell
.\run.ps1
```

### Opção 4: Configurar via IDE (Eclipse/IntelliJ)

#### IntelliJ IDEA:
1. Run → Edit Configurations
2. Selecione sua configuração Spring Boot
3. Em "Environment variables", adicione:
   - `DB_PASSWORD=sua_senha_mysql_aqui`
   - `JWT_SECRET=sua_chave_jwt_secreta_minimo_32_caracteres_aqui`
   - `APP_DEFAULT_ADMIN_PASSWORD=senha_admin_desejada`
   - `APP_DEFAULT_TEST_PASSWORD=senha_teste_desejada`
   
   ⚠️ **IMPORTANTE**: Substitua pelos valores reais. NUNCA commite estas credenciais!

#### Eclipse:
1. Run → Run Configurations
2. Selecione sua configuração Java Application
3. Aba "Environment"
4. Adicione as variáveis de ambiente manualmente

## ✅ VERIFICAÇÃO

Após configurar, verifique se as variáveis estão definidas:

```powershell
echo $env:DB_PASSWORD
echo $env:JWT_SECRET
```

## 📝 VARIÁVEIS NECESSÁRIAS

⚠️ **IMPORTANTE**: As credenciais devem ser configuradas através de variáveis de ambiente ou arquivo `.env` (que NÃO deve ser commitado).

Consulte `env.example` na raiz do projeto para ver o formato esperado:

- **DB_PASSWORD**: Senha do MySQL (obrigatório)
- **JWT_SECRET**: Secret para assinatura de tokens JWT (obrigatório, mínimo 32 caracteres)
- **APP_DEFAULT_ADMIN_PASSWORD**: Senha do usuário admin padrão (opcional, apenas desenvolvimento)
- **APP_DEFAULT_TEST_PASSWORD**: Senha do usuário teste padrão (opcional, apenas desenvolvimento)
- **LOG_PATH**: Diretório onde os arquivos de log serão gravados (padrão: `logs`)
- **LOG_SQL_LEVEL**: Nível de log das consultas SQL (`WARN`, `INFO`, `DEBUG`)
- **CACHE_ENABLED**: Habilita/desabilita o uso do Redis como cache (`true`/`false`)
- **CACHE_DEFAULT_TTL**: Tempo padrão de expiração do cache (ex.: `PT10M` = 10 minutos)
- **CACHE_ALLOW_NULL**: Permite armazenar valores `null` em cache (`false` recomendado)
- **CACHE_KEY_PREFIX**: Prefixo aplicado aos nomes de caches no Redis (padrão: `plataforma::`)

## 🚀 EXECUTAR APLICAÇÃO

Após configurar as variáveis:

```powershell
cd plataforma
mvn spring-boot:run
```

Ou se estiver usando o script:

```powershell
cd plataforma
.\run.ps1
```

## 🔒 SEGURANÇA

- **NUNCA** commite arquivos `.env` ou com senhas no repositório
- Use variáveis de ambiente ou serviços de secrets em produção
- Em produção, gere um `JWT_SECRET` forte (mínimo 64 caracteres)
