# 🚂 Guia Completo: Deploy no Railway

Este guia explica como fazer deploy da **AriranG Plataforma** no Railway, incluindo configuração do MySQL e primeiro deploy.

---

## 📚 Conceitos Básicos do Railway

### O que é Railway?
- **Railway** é uma Plataforma como Serviço (PaaS) que permite fazer deploy de aplicações rapidamente
- Suporta múltiplas linguagens e frameworks
- Oferece banco de dados, Redis, e outros serviços integrados
- Tem plano gratuito com limites generosos

### Conceitos Importantes:
- **Project (Projeto)**: Container para seus serviços
- **Service (Serviço)**: Uma aplicação ou banco de dados
- **Environment Variables (Variáveis de Ambiente)**: Configurações seguras
- **Deploy**: Processo de publicar sua aplicação
- **Domain (Domínio)**: URL pública da aplicação

---

## 🎯 Passo 1: Criar Conta no Railway

1. Acesse: https://railway.app
2. Clique em **"Start a New Project"** ou **"Login"**
3. Escolha fazer login com **GitHub** (recomendado)
4. Autorize o Railway a acessar seus repositórios

---

## 🎯 Passo 2: Criar Novo Projeto

1. No dashboard do Railway, clique em **"New Project"**
2. Escolha **"Deploy from GitHub repo"**
3. Autorize o Railway se necessário
4. Selecione o repositório: `AriranG` (ou o nome do seu repositório)
5. O Railway criará automaticamente um projeto e iniciará o deploy

> ⚠️ **IMPORTANTE**: O Railway detecta automaticamente o `Dockerfile` e tenta fazer deploy. Isso pode falhar na primeira vez porque ainda não configuramos o MySQL. Tudo bem, vamos configurar!

---

## 🎯 Passo 3: Adicionar MySQL Database

> 📚 **Guia Detalhado**: Para explicações mais detalhadas sobre configuração do banco de dados, consulte [`RAILWAY_DATABASE_SETUP.md`](./RAILWAY_DATABASE_SETUP.md)

### 3.1. Adicionar Serviço MySQL

1. No projeto do Railway, clique no botão **"+ New"** (canto inferior direito)
2. Selecione **"Database"** → **"MySQL"**
3. O Railway criará automaticamente um banco MySQL

### 3.2. Obter Credenciais do MySQL

1. Clique no serviço **MySQL** que acabou de ser criado
2. Vá na aba **"Variables"** (ou **"Connect"**)
3. Você verá as seguintes variáveis **AUTOMÁTICAS** criadas pelo Railway:
   - `MYSQL_HOST`
   - `MYSQL_PORT`
   - `MYSQL_DATABASE`
   - `MYSQL_USER`
   - `MYSQL_PASSWORD`
   - `MYSQL_URL` (string de conexão completa)

> 💡 **Dica**: O Railway cria essas variáveis automaticamente e elas já estão disponíveis para outros serviços no mesmo projeto!

### 3.3. Verificar Variáveis Criadas

As variáveis MySQL criadas automaticamente são:
```
MYSQL_HOST=containers-us-west-xxx.railway.app
MYSQL_PORT=3306
MYSQL_DATABASE=railway
MYSQL_USER=root
MYSQL_PASSWORD=senha_aleatoria_gerada
MYSQL_URL=mysql://root:senha@containers-us-west-xxx.railway.app:3306/railway
```

> 📖 **Quer entender melhor?** Veja [`RAILWAY_DATABASE_SETUP.md`](./RAILWAY_DATABASE_SETUP.md) para explicações detalhadas sobre como essas variáveis funcionam e como a aplicação se conecta ao banco.

---

## 🎯 Passo 4: Configurar Variáveis de Ambiente da Aplicação

### 4.1. Acessar Configurações da Aplicação

1. No projeto Railway, clique no serviço da sua **aplicação Spring Boot** (não o MySQL)
2. Vá na aba **"Variables"**

### 4.2. Adicionar Variáveis Obrigatórias

Clique em **"+ New Variable"** e adicione cada uma:

#### **Perfil do Spring Boot**
```
SPRING_PROFILES_ACTIVE=prod
```

#### **Configuração do Banco de Dados**

**Opção 1: Usar MYSQL_URL (Recomendado - Mais Simples)**

O Railway cria automaticamente a variável `MYSQL_URL` com a string de conexão completa no formato `mysql://user:pass@host:port/db`.

**✅ Você NÃO precisa configurar nada!** A aplicação detecta automaticamente a variável `MYSQL_URL` e converte para o formato JDBC necessário.

Apenas certifique-se de que o perfil `prod` está ativo:

```
SPRING_PROFILES_ACTIVE=prod
```

A `RailwayDatabaseConfig` fará a conversão automaticamente.

**Opção 2: Usar Variáveis Individuais**

Se preferir usar variáveis separadas:

```
DB_HOST=${MYSQL_HOST}
DB_PORT=${MYSQL_PORT}
DB_NAME=${MYSQL_DATABASE}
DB_USERNAME=${MYSQL_USER}
DB_PASSWORD=${MYSQL_PASSWORD}
```

> 💡 **IMPORTANTE**: O `${MYSQL_HOST}` é uma referência às variáveis do serviço MySQL. O Railway faz essa substituição automaticamente!
> 
> ⚠️ **NOTA**: A aplicação foi configurada para detectar automaticamente as variáveis MySQL do Railway. Você pode deixar essas variáveis vazias se usar a `MYSQL_URL`!

#### **JWT Secret**
```
JWT_SECRET=<gere-um-secret-aleatorio>
```

Para gerar um JWT secret:
```bash
# No PowerShell:
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))

# Ou no terminal:
openssl rand -base64 64
```

#### **Outras Variáveis Importantes**
```
CACHE_ENABLED=false
```

> ⚠️ **Nota**: Deixamos `CACHE_ENABLED=false` inicialmente porque não configuramos Redis ainda. Depois podemos adicionar Redis se necessário.

### 4.3. Variáveis Opcionais (CORS, Uploads, etc.)

```
APP_CORS_ALLOWED_ORIGINS=https://seu-dominio.up.railway.app
APP_UPLOAD_MAX_FILE_SIZE=5242880
LOG_PATH=/app/logs
LOG_SQL_LEVEL=WARN
```

---

## 🎯 Passo 5: Configurar Build e Deploy

### 5.1. Verificar Dockerfile

O Railway detecta automaticamente o `Dockerfile`. Verifique se está na raiz do projeto `plataforma/`.

### 5.2. Configurar Settings do Serviço

1. No serviço da aplicação, clique em **"Settings"**
2. Verifique as seguintes configurações:

#### **Deploy Settings**
- **Source**: GitHub (ou a fonte que você escolheu)
- **Branch**: `main` (ou sua branch principal)
- **Root Directory**: `plataforma` (se sua aplicação está em uma subpasta)

#### **Health Check**
- **Healthcheck Path**: Deixe vazio por enquanto (ou `/actuator/health` se tiver actuator)
- **Port**: `8080` (porta padrão do Spring Boot)

#### **Expose Port**
- **Port**: `8080`

### 5.3. Configurar Rede

1. Vá em **"Settings"** → **"Networking"**
2. Clique em **"Generate Domain"**
3. O Railway gerará uma URL como: `https://arirang-plataforma-production.up.railway.app`

---

## 🎯 Passo 6: Primeiro Deploy

### 6.1. Iniciar Deploy Manual

1. No serviço da aplicação, vá na aba **"Deployments"**
2. Clique em **"Deploy"** ou aguarde o deploy automático após o push

### 6.2. Acompanhar Logs

1. Durante o deploy, vá na aba **"Deploy Logs"**
2. Acompanhe o progresso em tempo real
3. Aguarde a mensagem: `"Application startup completed"`

### 6.3. Verificar Status

1. No **"Deployments"**, verifique o status:
   - ✅ **Success**: Deploy concluído
   - ❌ **Failed**: Erro no deploy (verifique os logs)

---

## 🎯 Passo 7: Testar a Aplicação

### 7.1. Acessar URL Pública

1. No **"Settings"** → **"Networking"**, copie a URL pública
2. Acesse no navegador: `https://sua-url.up.railway.app`
3. Você deve ver a página de login

### 7.2. Testar Endpoints

#### **Testar Login**
```
POST https://sua-url.up.railway.app/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### **Testar Health Check** (se tiver actuator)
```
GET https://sua-url.up.railway.app/actuator/health
```

### 7.3. Verificar Logs da Aplicação

1. No Railway, vá na aba **"Logs"** do serviço
2. Verifique se não há erros
3. Procure por mensagens de sucesso:
   - `"Started PlataformaApplication"`
   - `"HikariPool-1 - Starting..."`
   - `"Tomcat started on port(s): 8080"`

---

## 🎯 Passo 8: Configurar Domínio Personalizado (Opcional)

1. Vá em **"Settings"** → **"Networking"**
2. Em **"Custom Domain"**, clique em **"Add Domain"**
3. Digite seu domínio (ex: `app.arirang.com.br`)
4. Configure os registros DNS conforme instruções do Railway
5. Aguarde a validação (pode levar alguns minutos)

---

## 🔧 Troubleshooting (Solução de Problemas)

### ❌ Build Falha

**Problema**: Erro durante o build do Docker

**Solução**:
1. Verifique os logs do deploy
2. Certifique-se de que o `Dockerfile` está correto
3. Verifique se todas as dependências estão no `pom.xml`

### ❌ Aplicação não inicia

**Problema**: Deploy concluído, mas aplicação não responde

**Solução**:
1. Verifique os logs da aplicação (aba **"Logs"**)
2. Confirme que a porta está configurada como `8080`
3. Verifique se as variáveis de ambiente estão corretas

### ❌ Erro de Conexão com MySQL

**Problema**: `Access denied` ou `Connection refused`

**Solução**:
1. Verifique se as variáveis `DB_*` estão usando `${MYSQL_*}` corretamente
2. Confirme que o serviço MySQL está rodando (verde no Railway)
3. Verifique os logs do MySQL (no serviço MySQL, aba **"Logs"**)

### ❌ Erro de JWT Secret

**Problema**: `JWT secret is too short`

**Solução**:
1. Gere um novo JWT secret com pelo menos 64 caracteres
2. Atualize a variável `JWT_SECRET` no Railway
3. Faça um novo deploy

### ❌ Erro de Memória

**Problema**: Aplicação crasha por falta de memória

**Solução**:
1. No **"Settings"** → **"Resources"**, aumente a memória (se no plano pago)
2. Ajuste `JAVA_OPTS` no Dockerfile ou variáveis de ambiente
3. Considere otimizar a aplicação

---

## 📋 Checklist Final

Antes de considerar o deploy completo, verifique:

- [ ] MySQL está rodando e acessível
- [ ] Todas as variáveis de ambiente estão configuradas
- [ ] Build do Docker foi concluído com sucesso
- [ ] Aplicação iniciou sem erros (ver logs)
- [ ] URL pública está acessível
- [ ] Login está funcionando
- [ ] Banco de dados está criando as tabelas automaticamente

---

## 🚀 Próximos Passos

1. **Configurar CI/CD**: Deploy automático ao fazer push no GitHub
2. **Adicionar Redis**: Para cache (se necessário)
3. **Configurar Backups**: Backups automáticos do MySQL
4. **Monitoramento**: Configurar alertas e métricas
5. **SSL/HTTPS**: Railway já fornece automaticamente

---

## 💡 Dicas Importantes

1. **Plano Gratuito do Railway**:
   - 500 horas de uso/mês
   - 5$ de crédito grátis (suficiente para testes)
   - MySQL incluído

2. **Variáveis de Ambiente**:
   - Use `${MYSQL_*}` para referenciar variáveis do MySQL
   - Nunca commite senhas no código
   - Use variáveis de ambiente sempre

3. **Logs**:
   - Logs ficam disponíveis por 24 horas no plano gratuito
   - Exporte logs importantes se precisar manter histórico

4. **Deploys Automáticos**:
   - O Railway faz deploy automaticamente ao detectar push no GitHub
   - Configure branches específicas se necessário

---

## 📞 Suporte

- **Documentação Railway**: https://docs.railway.app
- **Discord Railway**: https://discord.gg/railway
- **Status Railway**: https://status.railway.app

---

**Boa sorte com o deploy! 🚀**

