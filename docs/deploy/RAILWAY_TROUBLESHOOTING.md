# 🔧 Troubleshooting - Erro de Conexão com Banco de Dados no Railway

## ❌ Erro Encontrado

```
Unable to open JDBC Connection for DDL execution
Could not create connection to database server. Attempted reconnect 3 times. Giving up.
```

## 🔍 Diagnóstico

Este erro indica que a aplicação Spring Boot não consegue se conectar ao banco de dados MySQL no Railway durante a inicialização.

## ✅ Soluções

### 1. Verificar Variáveis de Ambiente no Railway

Acesse o painel do Railway e verifique se as seguintes variáveis estão configuradas:

#### Variáveis OBRIGATÓRIAS:

```bash
SPRING_PROFILES_ACTIVE=prod
```

#### Variáveis do MySQL (devem ser criadas automaticamente ao conectar o serviço MySQL):

O Railway cria automaticamente a variável `MYSQL_URL` quando você conecta o serviço MySQL ao serviço da aplicação. Verifique se:

1. ✅ O serviço MySQL está provisionado
2. ✅ O serviço MySQL está conectado ao serviço da aplicação
3. ✅ A variável `MYSQL_URL` está presente nas variáveis de ambiente

### 2. Conectar Serviço MySQL à Aplicação

**Passo a passo:**

1. No Railway, vá para o serviço da sua **aplicação Spring Boot**
2. Clique na aba **"Variables"**
3. Procure por uma seção **"Service Variables"** ou **"Connected Services"**
4. Se o MySQL não estiver conectado:
   - Clique em **"Connect Service"** ou **"Add Service"**
   - Selecione o serviço MySQL
   - O Railway criará automaticamente a variável `MYSQL_URL`

### 3. Verificar Formato da MYSQL_URL

A variável `MYSQL_URL` deve estar no formato:

```
mysql://usuario:senha@host:porta/database
```

**Exemplo:**
```
mysql://root:MinhaSenha123@containers-us-west-xxx.railway.app:3306/railway
```

### 4. Configuração Manual (Alternativa)

Se a `MYSQL_URL` não estiver disponível, configure manualmente:

```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://[HOST]:3306/[DATABASE]?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Sao_Paulo&autoReconnect=true&failOverReadOnly=false&maxReconnects=3&initialTimeout=2
DB_USERNAME=root
DB_PASSWORD=[SENHA]
```

**⚠️ IMPORTANTE:** Substitua `[HOST]`, `[DATABASE]` e `[SENHA]` pelos valores reais do seu MySQL no Railway.

### 5. Verificar se o MySQL está "Acordado"

No Railway Free Tier, o MySQL pode entrar em "sleep mode" após inatividade. A primeira conexão após o sleep pode demorar alguns segundos.

**Solução:** A configuração já inclui parâmetros de auto-reconexão:
- `autoReconnect=true`
- `maxReconnects=3`
- `initialTimeout=2`

Se o problema persistir, tente:
1. Acessar o serviço MySQL no Railway para "acordá-lo"
2. Aguardar alguns segundos antes de iniciar a aplicação

### 6. Verificar Logs de Inicialização

Procure nos logs por mensagens da `RailwayDatabaseConfig`:

```
=== RailwayDatabaseConfig: Configuração de Database ===
MYSQL_URL presente: SIM/NÃO
```

Se aparecer `MYSQL_URL presente: NÃO`, a variável não está configurada.

### 7. Verificar Perfil Ativo

Certifique-se de que o perfil `prod` está ativo:

```bash
SPRING_PROFILES_ACTIVE=prod
```

A classe `RailwayDatabaseConfig` só é ativada quando o perfil `prod` está ativo.

## 🧪 Teste de Conexão

Após configurar as variáveis, verifique os logs de inicialização. Você deve ver:

```
✅ MYSQL_URL convertida com sucesso!
✅ URL final: jdbc:mysql://host:port/database?...
✅ Username: root
✅ Password: ****
```

## 📋 Checklist Rápido

- [ ] `SPRING_PROFILES_ACTIVE=prod` está configurado
- [ ] Serviço MySQL está provisionado no Railway
- [ ] Serviço MySQL está conectado ao serviço da aplicação
- [ ] Variável `MYSQL_URL` está presente (ou variáveis `DB_*` configuradas manualmente)
- [ ] MySQL não está em "sleep mode" (tente acessar o serviço MySQL primeiro)

## 🆘 Se Nada Funcionar

1. **Verifique os logs completos** do Railway para ver mensagens de erro mais detalhadas
2. **Teste a conexão manualmente** usando as credenciais do MySQL
3. **Verifique se o MySQL está acessível** fora do Railway (pode haver problemas de rede)
4. **Considere usar variáveis individuais** (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`) em vez de `MYSQL_URL`

## 📞 Suporte

Se o problema persistir após seguir todos os passos, forneça:
- Logs completos de inicialização
- Variáveis de ambiente configuradas (sem senhas)
- Status do serviço MySQL no Railway

