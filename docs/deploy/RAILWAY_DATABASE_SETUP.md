# 🗄️ Guia Detalhado: Configuração do Banco de Dados no Railway

Este guia explica **passo a passo** como configurar o banco de dados MySQL no Railway e conectar sua aplicação Spring Boot a ele.

---

## 📚 Conceitos Básicos

### Como o Railway gerencia bancos de dados?

1. **Serviços Separados**: O Railway trata o banco de dados como um **serviço separado** da aplicação
2. **Variáveis Automáticas**: Quando você cria um MySQL, o Railway cria **automaticamente** variáveis de ambiente com as credenciais
3. **Conexão Interna**: Serviços no mesmo projeto podem se comunicar usando essas variáveis
4. **Segurança**: As credenciais são gerenciadas automaticamente e nunca precisam ser expostas

---

## 🎯 Passo 1: Adicionar MySQL ao Projeto

### 1.1. Abrir o Projeto no Railway

1. Acesse https://railway.app
2. Faça login
3. Clique no seu projeto (ou crie um novo)

### 1.2. Adicionar Serviço MySQL

1. No seu projeto Railway, localize o botão **"+ New"** (canto inferior direito ou superior)
2. Clique em **"+ New"**
3. Selecione **"Database"**
4. Escolha **"MySQL"**

> 💡 **O que acontece?**
> - O Railway cria automaticamente uma instância MySQL
> - Gera credenciais seguras automaticamente
> - Cria variáveis de ambiente com essas credenciais
> - Faz tudo isso em alguns segundos!

### 1.3. Aguardar Criação

Aguarde alguns segundos enquanto o Railway cria o banco. Você verá:
- ⏳ Status: "Provisioning..." (provisionando)
- ✅ Status: "Active" (ativo) quando estiver pronto

---

## 🎯 Passo 2: Entender as Variáveis Automáticas

### 2.1. Visualizar Variáveis do MySQL

1. Clique no serviço **MySQL** que acabou de ser criado
2. Vá na aba **"Variables"** (ou **"Connect"**)

Você verá estas variáveis **criadas automaticamente**:

```
MYSQL_HOST=containers-us-west-xxx.railway.app
MYSQL_PORT=3306
MYSQL_DATABASE=railway
MYSQL_USER=root
MYSQL_PASSWORD=senha_aleatoria_gerada_12345
MYSQL_URL=mysql://root:senha@containers-us-west-xxx.railway.app:3306/railway
```

### 2.2. O que cada variável significa?

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `MYSQL_HOST` | Endereço do servidor MySQL | `containers-us-west-xxx.railway.app` |
| `MYSQL_PORT` | Porta do MySQL | `3306` |
| `MYSQL_DATABASE` | Nome do banco de dados | `railway` |
| `MYSQL_USER` | Usuário do banco | `root` |
| `MYSQL_PASSWORD` | Senha do banco (gerada automaticamente) | `abc123xyz...` |
| `MYSQL_URL` | String de conexão completa | `mysql://root:senha@host:3306/railway` |

### 2.3. Importante sobre `MYSQL_URL`

A variável `MYSQL_URL` contém **tudo que você precisa** em uma única string:

```
mysql://[usuário]:[senha]@[host]:[porta]/[database]
```

**Exemplo real:**
```
mysql://root:MinhaSenha123@containers-us-west-xxx.railway.app:3306/railway
```

---

## 🎯 Passo 3: Conectar a Aplicação Spring Boot

### 3.1. Acessar Variáveis da Aplicação

1. No projeto Railway, clique no serviço da sua **aplicação Spring Boot** (não o MySQL)
2. Vá na aba **"Variables"**

### 3.2. Opção 1: Usar MYSQL_URL (Recomendado - Mais Simples) ⭐

A aplicação **AriranG Plataforma** foi configurada para detectar automaticamente a `MYSQL_URL` do Railway e converter para o formato JDBC necessário.

#### O que você precisa fazer:

**Apenas adicione estas variáveis:**

```bash
SPRING_PROFILES_ACTIVE=prod
```

**Isso é tudo!** 🎉

A aplicação irá:
1. ✅ Detectar automaticamente a variável `MYSQL_URL` do serviço MySQL
2. ✅ Converter para formato JDBC: `jdbc:mysql://host:port/database`
3. ✅ Extrair usuário e senha
4. ✅ Conectar ao banco automaticamente

#### Como funciona por trás dos panos?

A classe `RailwayDatabaseConfig` (que já está no projeto) faz isso automaticamente:

```java
// Railway detecta MYSQL_URL
MYSQL_URL=mysql://root:senha@host:3306/railway

// Converte automaticamente para:
spring.datasource.url=jdbc:mysql://host:3306/railway?useSSL=false&...
spring.datasource.username=root
spring.datasource.password=senha
```

### 3.3. Opção 2: Usar Variáveis Individuais (Alternativa)

Se preferir configurar manualmente (ou se a Opção 1 não funcionar):

**Adicione estas variáveis na aplicação:**

```bash
SPRING_PROFILES_ACTIVE=prod
DB_HOST=${MYSQL_HOST}
DB_PORT=${MYSQL_PORT}
DB_NAME=${MYSQL_DATABASE}
DB_USERNAME=${MYSQL_USER}
DB_PASSWORD=${MYSQL_PASSWORD}
```

> 💡 **O que é `${MYSQL_HOST}`?**
> - É uma **referência** às variáveis do serviço MySQL
> - O Railway **substitui automaticamente** esse valor
> - Você não precisa copiar os valores manualmente!

#### Como funciona?

```
Serviço MySQL tem:  MYSQL_HOST=containers-us-west-xxx.railway.app

Serviço App tem:    DB_HOST=${MYSQL_HOST}

Resultado final:    DB_HOST=containers-us-west-xxx.railway.app
                    (Railway substitui automaticamente)
```

---

## 🎯 Passo 4: Verificar Conexão

### 4.1. Fazer Deploy da Aplicação

1. Vá na aba **"Deployments"** do serviço da aplicação
2. Clique em **"Deploy"** (ou aguarde deploy automático)
3. Acompanhe os logs

### 4.2. Verificar Logs

Nos logs da aplicação, procure por:

**✅ Conexão bem-sucedida:**
```
INFO  [main] com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Starting...
INFO  [main] com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Added connection
INFO  [main] com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Start completed.
```

**❌ Erro de conexão:**
```
ERROR [main] com.zaxxer.hikari.pool.HikariPool - HikariPool-1 - Exception during pool initialization.
SQLException: Access denied for user 'root'@'...'
```

### 4.3. Verificar Tabelas Criadas

1. No Railway, clique no serviço **MySQL**
2. Vá na aba **"Connect"** ou **"Query"**
3. Execute:

```sql
SHOW TABLES;
```

Você deve ver as tabelas criadas pelo Hibernate:
- `alunos`
- `turma`
- `contratos`
- `usuarios`
- etc.

---

## 🔍 Entendendo a Arquitetura

### Diagrama Simplificado

```
┌─────────────────────────────────────────────┐
│         Projeto Railway                      │
│                                              │
│  ┌──────────────────┐  ┌─────────────────┐ │
│  │  Serviço MySQL   │  │  Serviço App    │ │
│  │                  │  │  Spring Boot    │ │
│  │  Variáveis:      │  │                 │ │
│  │  - MYSQL_HOST    │  │  Variáveis:     │ │
│  │  - MYSQL_PORT    │  │  - SPRING_...   │ │
│  │  - MYSQL_DATABASE│  │  - JWT_SECRET   │ │
│  │  - MYSQL_USER    │  │                 │ │
│  │  - MYSQL_PASSWORD│  │  Referencia:    │ │
│  │  - MYSQL_URL     │  │  ${MYSQL_URL}   │ │
│  │                  │  │      ↓          │ │
│  │                  │◄─┼─── Conecta ────┼─┘ │
│  └──────────────────┘  └─────────────────┘ │
└─────────────────────────────────────────────┘
```

### Fluxo de Conexão

1. **MySQL é criado** → Railway gera credenciais e variáveis
2. **App detecta** → Lê `MYSQL_URL` do ambiente
3. **App converte** → `RailwayDatabaseConfig` converte para JDBC
4. **App conecta** → HikariCP cria pool de conexões
5. **App inicia** → Spring Boot cria tabelas (se `ddl-auto=update`)

---

## ❓ FAQ - Perguntas Frequentes

### 1. Preciso copiar as credenciais manualmente?

**Não!** O Railway gerencia isso automaticamente. Use `${MYSQL_*}` para referenciar.

### 2. A senha do MySQL é segura?

**Sim!** A Railway gera senhas aleatórias e seguras automaticamente. Não precisa se preocupar.

### 3. Posso mudar o nome do banco de dados?

Sim, mas por padrão o Railway cria como `railway`. Você pode criar outro banco se necessário via SQL.

### 4. Como acessar o banco via cliente externo?

1. No serviço MySQL, vá em **"Connect"**
2. Copie a string de conexão JDBC
3. Use uma ferramenta como MySQL Workbench ou DBeaver

**Nota**: No plano gratuito, o acesso externo pode ser limitado.

### 5. O que acontece se eu deletar o serviço MySQL?

⚠️ **Todos os dados serão perdidos!** Faça backup antes se tiver dados importantes.

### 6. Posso usar múltiplos bancos de dados?

Sim! Você pode criar vários serviços MySQL no mesmo projeto.

### 7. Como funciona o backup automático?

O Railway faz backups automáticos, mas no plano gratuito há limitações. Consulte a documentação do Railway para detalhes.

### 8. A conexão é segura?

Sim! A comunicação entre serviços no Railway é interna e segura. Não passa pela internet pública.

---

## 🔧 Troubleshooting

### ❌ Erro: "Access denied for user"

**Causa**: Variáveis de ambiente não estão configuradas corretamente.

**Solução**:
1. Verifique se as variáveis usam `${MYSQL_*}` (não valores hardcoded)
2. Confirme que o serviço MySQL está ativo (status verde)
3. Verifique os logs do MySQL no Railway

### ❌ Erro: "Connection refused"

**Causa**: Serviço MySQL não está rodando ou host/porta incorretos.

**Solução**:
1. Verifique o status do serviço MySQL (deve estar "Active")
2. Confirme que está usando `${MYSQL_HOST}` e `${MYSQL_PORT}`
3. Aguarde alguns segundos após criar o MySQL

### ❌ Erro: "Unknown database"

**Causa**: Nome do banco está incorreto.

**Solução**:
1. Use `${MYSQL_DATABASE}` (não hardcode "railway")
2. Ou verifique o nome exato do banco nas variáveis do MySQL

### ❌ Tabelas não são criadas

**Causa**: `spring.jpa.hibernate.ddl-auto` pode estar desabilitado.

**Solução**:
1. Verifique `application-prod.properties`:
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   ```
2. Verifique os logs para erros de schema

---

## 📋 Checklist de Configuração

Antes de fazer deploy, verifique:

- [ ] Serviço MySQL foi criado e está ativo
- [ ] Variáveis do MySQL estão visíveis (abrir MySQL → Variables)
- [ ] Variável `SPRING_PROFILES_ACTIVE=prod` está na aplicação
- [ ] Se usando Opção 2: variáveis `DB_*` estão configuradas com `${MYSQL_*}`
- [ ] `JWT_SECRET` está configurado (obrigatório)
- [ ] Aplicação fez deploy com sucesso
- [ ] Logs mostram conexão bem-sucedida
- [ ] Tabelas foram criadas no banco

---

## 🎓 Próximos Passos

Após configurar o banco:

1. **Testar a aplicação**: Acesse a URL pública e teste login
2. **Verificar dados**: Execute queries no banco para confirmar dados
3. **Configurar backups**: Se necessário, configure backups automáticos
4. **Monitorar**: Acompanhe métricas do banco no Railway

---

## 📚 Referências

- **Railway Docs - Databases**: https://docs.railway.app/databases
- **Railway Docs - Environment Variables**: https://docs.railway.app/develop/variables
- **Guia Completo Railway**: Veja `RAILWAY_GUIDE.md` para deploy completo

---

**Ainda com dúvidas?** Consulte os logs da aplicação e do MySQL no Railway, ou veja `RAILWAY_GUIDE.md` para o guia completo de deploy.

