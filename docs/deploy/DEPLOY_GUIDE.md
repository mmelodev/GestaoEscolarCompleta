# 🚀 Guia de Deploy - AriranG Plataforma

> Manual completo de operações para deploy do projeto AriranG Plataforma

## 📋 Índice

1. [Estratégia de Hospedagem](#1-estratégia-de-hospedagem)
2. [Configuração de Ambientes](#2-configuração-de-ambientes)
3. [Fluxo de Git (Git Flow Simplificado)](#3-fluxo-de-git-git-flow-simplificado)
4. [Automação (CI/CD Básico)](#4-automação-cicd-básico)
5. [Regras de Ouro (Checklist de Deploy)](#5-regras-de-ouro-checklist-de-deploy)
6. [Deploy Manual com Docker](#6-deploy-manual-com-docker)
7. [Troubleshooting](#7-troubleshooting)

---

## 1. Estratégia de Hospedagem

### Comparação de Abordagens

#### PaaS (Platform as a Service) - ⭐ **Recomendado**

**Vantagens:**
- ✅ Configuração simples e rápida
- ✅ Gerenciamento automático de infraestrutura
- ✅ Escalabilidade automática
- ✅ Integração nativa com Git (deploy automático)
- ✅ Suporte a múltiplos serviços (MySQL, Redis) na mesma plataforma
- ✅ SSL/HTTPS configurado automaticamente
- ✅ Backups automáticos (na maioria dos serviços)

**Desvantagens:**
- ❌ Custo mais alto para aplicações com muito tráfego
- ❌ Menos controle sobre o ambiente

**Plataformas Recomendadas:**
- **Railway** (https://railway.app) - Excelente para Java, MySQL e Redis
- **Render** (https://render.com) - Suporte robusto a Spring Boot
- **Heroku** (https://heroku.com) - Tradicional, porém mais caro

#### VPS (Virtual Private Server)

**Vantagens:**
- ✅ Controle total sobre o ambiente
- ✅ Custo fixo mensal (geralmente mais barato)
- ✅ Flexibilidade para instalar qualquer software

**Desvantagens:**
- ❌ Requer conhecimento de administração de servidor
- ❌ Você é responsável por segurança, backups, atualizações
- ❌ Configuração inicial mais complexa

**Quando usar:** Para equipes com experiência em DevOps ou quando há requisitos específicos de infraestrutura.

#### Containers (Docker + Kubernetes/Orquestração)

**Vantagens:**
- ✅ Isolamento completo da aplicação
- ✅ Portabilidade entre ambientes
- ✅ Escalabilidade horizontal avançada

**Desvantagens:**
- ❌ Curva de aprendizado mais íngreme
- ❌ Requer orquestração (Kubernetes, Docker Swarm)
- ❌ Configuração mais complexa

**Quando usar:** Para aplicações grandes, com múltiplos serviços, ou quando já existe infraestrutura de containers.

### 🎯 Recomendação para AriranG Plataforma

**PaaS é a melhor escolha** para este projeto porque:

1. **Simplicidade**: A aplicação Spring Boot funciona "out of the box" em plataformas PaaS
2. **Serviços Integrados**: Plataformas como Railway oferecem MySQL e Redis como serviços gerenciados
3. **Deploy Automático**: Integração nativa com Git permite deploy automático ao fazer push
4. **Custo-Benefício**: Para aplicações de pequeno/médio porte, o custo é competitivo
5. **Manutenção Reduzida**: Você foca no código, não na infraestrutura

### Próximos Passos

Este guia assume o uso de **Railway** como exemplo, mas os conceitos se aplicam a qualquer PaaS.

---

## 2. Configuração de Ambientes

### Perfis Spring Boot

O Spring Boot usa **perfis (profiles)** para separar configurações por ambiente. Isso é controlado pela variável de ambiente `SPRING_PROFILES_ACTIVE`.

### Ambientes Definidos

#### Ambiente de Homologação (Testes)

**Profile:** `homolog`  
**Banco de Dados:** `arirang_db_test`  
**Arquivo de Configuração:** `application-homolog.properties`

**Características:**
- Validações menos restritivas para facilitar testes
- Logs mais verbosos (SQL, debug)
- Swagger habilitado para documentação da API
- Permite criação automática de usuários padrão (se configurado)
- CORS permite `localhost:*`

**Variáveis de Ambiente Necessárias:**
```bash
SPRING_PROFILES_ACTIVE=homolog
DB_URL=jdbc:mysql://host:3306/arirang_db_test?...
DB_USERNAME=usuario_homolog
DB_PASSWORD=senha_homolog
JWT_SECRET=secret_para_homologacao_min_32_chars
REDIS_HOST=host_redis_homolog
REDIS_PORT=6379
```

#### Ambiente de Produção (Real)

**Profile:** `prod`  
**Banco de Dados:** `arirang_db_prod`  
**Arquivo de Configuração:** `application-prod.properties`

**Características:**
- Configurações de segurança máximas
- Logs otimizados (menos verbosos)
- Swagger desabilitado
- Não permite criação automática de usuários
- CORS restrito às origens configuradas
- `spring.jpa.hibernate.ddl-auto=none` (migrações manuais)

**Variáveis de Ambiente Necessárias:**
```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://host:3306/arirang_db_prod?...
DB_USERNAME=usuario_prod
DB_PASSWORD=senha_prod_segura
JWT_SECRET=secret_super_seguro_min_32_chars_256_bits
REDIS_HOST=host_redis_prod
REDIS_PORT=6379
REDIS_PASSWORD=senha_redis_segura
CORS_ALLOWED_ORIGIN_PATTERNS=https://*.meudominio.com
# ou
CORS_ALLOWED_ORIGINS=https://app.meudominio.com
```

### Como Funciona a Ativação de Perfis

1. **Local (Desenvolvimento):**
   - O arquivo `application.properties` define `spring.profiles.active=dev`
   - Usa configurações de desenvolvimento local

2. **Homologação:**
   - A plataforma PaaS define `SPRING_PROFILES_ACTIVE=homolog`
   - Spring Boot carrega `application-homolog.properties`

3. **Produção:**
   - A plataforma PaaS define `SPRING_PROFILES_ACTIVE=prod`
   - Spring Boot carrega `application-prod.properties`

### Criando o Profile de Homologação

Se ainda não existe, crie o arquivo `src/main/resources/application-homolog.properties`:

```properties
spring.application.name=plataforma

# Database Configuration (Homologação)
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=${DB_DRIVER:com.mysql.cj.jdbc.Driver}

# JPA/Hibernate Configuration (Homologação)
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION:86400000}
jwt.secret.min-length=32

# Redis Configuration
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}

# Cache Configuration
spring.cache.type=redis
app.cache.enabled=${CACHE_ENABLED:true}
app.cache.default-ttl=${CACHE_DEFAULT_TTL:PT10M}

# Swagger habilitado para testes
springdoc.swagger-ui.enabled=true

# CORS mais permissivo em homologação
app.cors.allowed-origin-patterns=${CORS_ALLOWED_ORIGIN_PATTERNS:http://localhost:*,https://*.railway.app}
```

---

## 3. Fluxo de Git (Git Flow Simplificado)

### Estrutura de Branches

```
main (produção)
  ↑
  └── develop (homologação)
       ↑
       └── feature/* (desenvolvimento)
```

### Branches Principais

#### `main` - Produção

- ✅ Representa o código em **Produção**
- ✅ **NUNCA** recebe commits diretos
- ✅ Apenas recebe merges de `develop` após testes em homologação
- ✅ Deve estar sempre estável e funcional
- ✅ Protegida contra push direto (configurar no GitHub/GitLab)

#### `develop` - Homologação

- ✅ Representa o código em **Homologação/Staging**
- ✅ Integra todas as features antes de ir para produção
- ✅ Serve como ambiente de teste para validações finais
- ✅ Recebe Pull Requests de `feature/*`

### Ciclo de Vida de uma Modificação

#### 1. Criar Feature Branch

```bash
# Certifique-se de estar em develop atualizado
git checkout develop
git pull origin develop

# Crie uma nova branch para sua feature
git checkout -b feature/minha-mudanca

# Desenvolva sua feature...
# Faça commits descritivos
git add .
git commit -m "feat: adiciona funcionalidade X"
```

**Nomenclatura de Branches:**
- `feature/nome-da-feature` - Nova funcionalidade
- `fix/nome-do-bug` - Correção de bug
- `refactor/nome-da-refatoracao` - Refatoração de código
- `docs/nome-da-documentacao` - Documentação

#### 2. Abrir Pull Request para `develop`

```bash
# Push da branch feature
git push origin feature/minha-mudanca

# No GitHub/GitLab, abra um Pull Request:
# Base: develop ← Compare: feature/minha-mudanca
```

**Checklist do PR para `develop`:**
- ✅ Código compila sem erros
- ✅ Testes passam (se existirem)
- ✅ Sem conflitos com `develop`
- ✅ Code review aprovado (se aplicável)
- ✅ Descrição clara do que foi alterado

**Após aprovação:**
- ✅ Merge o PR para `develop`
- ✅ Delete a branch `feature/*` após o merge
- ✅ O deploy automático para homologação é disparado

#### 3. Testar em Homologação

Após o merge em `develop`:
1. A aplicação é automaticamente deployada em **Homologação**
2. Teste todas as funcionalidades afetadas
3. Verifique logs e métricas
4. Valide integrações (MySQL, Redis)

#### 4. Deploy para Produção

```bash
# Quando homologação estiver validada, abra PR para main:
# Base: main ← Compare: develop
```

**Checklist do PR para `main`:**
- ✅ ✅ Testes em homologação passaram
- ✅ ✅ Variáveis de ambiente atualizadas (se necessário)
- ✅ ✅ Migrações de banco aplicadas (se houver)
- ✅ ✅ Documentação atualizada
- ✅ ✅ Aprovação explícita de um revisor sênior

**Após aprovação:**
- ✅ Merge o PR para `main`
- ✅ O deploy automático para produção é disparado
- ✅ Monitore a aplicação após o deploy

### Boas Práticas

1. **Commits Descritivos:**
   ```
   feat: adiciona endpoint de busca de alunos
   fix: corrige cálculo de média no boletim
   refactor: melhora estrutura do service de alunos
   docs: atualiza guia de deploy
   ```

2. **Branchs Curtas:**
   - Mantenha branches de feature ativas por no máximo alguns dias
   - Evite branches gigantes com muitas mudanças

3. **Sincronização:**
   - Sempre sincronize `develop` antes de criar uma nova feature
   - Faça rebase (ou merge) de `develop` na sua feature regularmente

4. **Revisão de Código:**
   - Sempre peça revisão antes de mergear
   - Responda aos comentários de revisão antes de mergear

---

## 4. Automação (CI/CD Básico)

### O Que é CI/CD?

- **CI (Continuous Integration)**: Integração contínua - código é testado automaticamente
- **CD (Continuous Deployment)**: Deploy contínuo - código é deployado automaticamente após testes

### Configuração com GitHub Actions

Crie o arquivo `.github/workflows/deploy.yml`:

```yaml
name: Build and Deploy

on:
  push:
    branches:
      - main       # Deploy para produção
      - develop    # Deploy para homologação

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: |
          mvn clean package -DskipTests
      
      - name: Build Docker image
        run: |
          docker build -t arirang-plataforma:${{ github.sha }} .
      
      # Para Railway/Render (exemplo)
      - name: Deploy to Railway (Homolog)
        if: github.ref == 'refs/heads/develop'
        run: |
          # Configurar Railway CLI ou usar webhook
          echo "Deploy para homologação..."
      
      - name: Deploy to Railway (Prod)
        if: github.ref == 'refs/heads/main'
        run: |
          # Configurar Railway CLI ou usar webhook
          echo "Deploy para produção..."
```

### Integração com PaaS (Railway/Render)

#### Railway

1. **Conectar Repositório:**
   - No dashboard da Railway, conecte seu repositório GitHub
   - Selecione a branch `develop` para homologação
   - Selecione a branch `main` para produção

2. **Configurar Deploy Automático:**
   - Railway detecta automaticamente mudanças nas branches
   - Faz build da imagem Docker ou executa `mvn clean package`
   - Deploy automático quando há push

3. **Variáveis de Ambiente:**
   - Configure todas as variáveis necessárias no dashboard
   - Diferentes valores para cada serviço (homolog vs prod)

#### Render

1. **Criar Web Service:**
   - Conecte o repositório
   - Selecione a branch (`develop` ou `main`)
   - Configure o build command: `mvn clean package -DskipTests`
   - Configure o start command: `java -jar target/*.jar`

2. **Auto-Deploy:**
   - Render faz deploy automático ao detectar push
   - Configura variáveis de ambiente por serviço

### Exemplo de Fluxo Completo

1. **Desenvolvedor faz push para `feature/nova-funcionalidade`:**
   ```bash
   git push origin feature/nova-funcionalidade
   ```

2. **Abre PR para `develop`:**
   - CI roda testes (se configurado)
   - Code review necessário

3. **Merge para `develop`:**
   - CI faz build
   - Deploy automático para **Homologação**
   - Equipe testa em homologação

4. **Após validação, abre PR para `main`:**
   - CI valida novamente
   - Aprovação explícita necessária

5. **Merge para `main`:**
   - CI faz build
   - Deploy automático para **Produção**
   - Monitoramento pós-deploy

---

## 5. Regras de Ouro (Checklist de Deploy)

Antes de fazer deploy em **Produção**, SEMPRE verifique:

### ✅ Regra 1: Variáveis de Ambiente

**Pergunta:** *"A nova senha da API foi adicionada ao painel da nuvem?"*

**Checklist:**
- [ ] Todas as variáveis de ambiente necessárias estão configuradas no serviço de produção
- [ ] Nenhuma variável está usando valores padrão inseguros
- [ ] `JWT_SECRET` tem pelo menos 32 caracteres e é único para produção
- [ ] `DB_PASSWORD` e `REDIS_PASSWORD` são senhas fortes e diferentes de desenvolvimento
- [ ] `CORS_ALLOWED_ORIGINS` ou `CORS_ALLOWED_ORIGIN_PATTERNS` estão configurados corretamente
- [ ] Se há novas variáveis de ambiente no código, elas foram adicionadas ao painel da PaaS

**Onde verificar:**
- Dashboard da PaaS (Railway/Render)
- Seção de Environment Variables do serviço de produção

**Como corrigir:**
1. Acesse o dashboard da PaaS
2. Vá em Settings → Environment Variables
3. Adicione/atualize as variáveis necessárias
4. Reinicie o serviço se necessário

### ✅ Regra 2: Migrações de Banco de Dados

**Pergunta:** *"A mudança no código Java (ex: nova coluna) já foi aplicada no banco de dados de Produção?"*

**Checklist:**
- [ ] Se o código adiciona/modifica entidades JPA, as migrações SQL foram criadas
- [ ] Se `spring.jpa.hibernate.ddl-auto=update` não está em produção, as migrações foram aplicadas manualmente
- [ ] Scripts de migração foram testados em homologação
- [ ] Backup do banco de produção foi feito antes de aplicar migrações
- [ ] Migrações foram testadas em ambiente similar a produção

**Cenários Comuns:**

1. **Nova Coluna em Entidade:**
   ```sql
   ALTER TABLE usuarios ADD COLUMN telefone VARCHAR(20);
   ```

2. **Nova Tabela:**
   ```sql
   CREATE TABLE nova_tabela (...);
   ```

3. **Modificação de Tipo:**
   ```sql
   ALTER TABLE turmas MODIFY COLUMN status ENUM('ATIVA', 'FECHADA', 'REABERTA');
   ```

**Processo Recomendado:**

1. **Desenvolvimento:**
   - Use `spring.jpa.hibernate.ddl-auto=update` em dev
   - Hibernate cria as mudanças automaticamente

2. **Homologação:**
   - Teste as migrações manualmente
   - Crie scripts SQL reutilizáveis

3. **Produção:**
   - Desabilite `ddl-auto` (`none`)
   - Execute scripts SQL manualmente ou via ferramenta de migração (Flyway/Liquibase)

**Ferramentas de Migração (Opcional, mas Recomendado):**

- **Flyway**: Integração nativa com Spring Boot
- **Liquibase**: Alternativa popular

### ✅ Regra 3: Rollback

**Pergunta:** *"Lembre-se de que a plataforma PaaS tem um botão de 'Rollback' para reverter o deploy em caso de falha."*

**Checklist:**
- [ ] Você sabe onde está o botão de rollback na plataforma
- [ ] Você sabe qual versão anterior estava funcionando
- [ ] Há um plano de ação caso algo dê errado
- [ ] A equipe sabe quem pode fazer rollback

**Como Fazer Rollback:**

#### Railway
1. Acesse o dashboard do serviço
2. Vá em **Deployments**
3. Selecione uma versão anterior que estava funcionando
4. Clique em **Redeploy**

#### Render
1. Acesse o dashboard do serviço
2. Vá em **Manual Deploy**
3. Selecione um commit anterior
4. Clique em **Deploy**

#### Docker (Manual)
```bash
# Voltar para uma imagem anterior
docker tag arirang-plataforma:commit-anterior arirang-plataforma:latest
docker push arirang-plataforma:latest
```

**Boas Práticas:**
- ✅ Sempre monitore a aplicação após deploy (primeiros 5-10 minutos)
- ✅ Mantenha logs abertos durante o deploy
- ✅ Tenha métricas/alertas configuradas (erros, latência, etc.)
- ✅ Documente problemas conhecidos e suas soluções

---

## 6. Deploy Manual com Docker

### Pré-requisitos

- Docker instalado
- Acesso ao Docker Hub ou registry privado
- Acesso ao servidor de produção (se for VPS)

### Build da Imagem Docker

```bash
# No diretório do projeto
cd plataforma

# Build da imagem
docker build -t arirang-plataforma:latest .

# Ou com tag específica
docker build -t arirang-plataforma:v1.0.0 .
```

### Testar Localmente

```bash
# Criar rede Docker (para comunicação entre containers)
docker network create arirang-network

# Subir MySQL
docker run -d \
  --name mysql-arirang \
  --network arirang-network \
  -e MYSQL_ROOT_PASSWORD=senha_root \
  -e MYSQL_DATABASE=arirang_db_prod \
  mysql:8.0

# Subir Redis
docker run -d \
  --name redis-arirang \
  --network arirang-network \
  redis:7-alpine

# Subir aplicação
docker run -d \
  --name arirang-plataforma \
  --network arirang-network \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:mysql://mysql-arirang:3306/arirang_db_prod \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=senha_root \
  -e JWT_SECRET=seu_jwt_secret_min_32_chars \
  -e REDIS_HOST=redis-arirang \
  -e REDIS_PORT=6379 \
  arirang-plataforma:latest
```

### Usando Docker Compose (Recomendado)

Crie `docker-compose.yml`:

```yaml
version: '3.8'

services:
  app:
    build: .
    container_name: arirang-plataforma
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:mysql://mysql:3306/arirang_db_prod
      - DB_USERNAME=root
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
    depends_on:
      - mysql
      - redis
    networks:
      - arirang-network
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    container_name: mysql-arirang
    environment:
      - MYSQL_ROOT_PASSWORD=${DB_PASSWORD}
      - MYSQL_DATABASE=arirang_db_prod
    volumes:
      - mysql-data:/var/lib/mysql
    networks:
      - arirang-network
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: redis-arirang
    networks:
      - arirang-network
    restart: unless-stopped

networks:
  arirang-network:
    driver: bridge

volumes:
  mysql-data:
```

Execute:
```bash
docker-compose up -d
```

### Deploy em VPS

1. **Transferir imagem para servidor:**
   ```bash
   # No seu computador
   docker save arirang-plataforma:latest | gzip > arirang-plataforma.tar.gz
   scp arirang-plataforma.tar.gz user@servidor:/path/
   
   # No servidor
   docker load < arirang-plataforma.tar.gz
   ```

2. **Ou usar Registry (Docker Hub):**
   ```bash
   # Build e push
   docker build -t seu-usuario/arirang-plataforma:latest .
   docker push seu-usuario/arirang-plataforma:latest
   
   # No servidor, fazer pull
   docker pull seu-usuario/arirang-plataforma:latest
   ```

---

## 7. Troubleshooting

### Problemas Comuns

#### ❌ Aplicação não inicia: "Access denied for user 'root'@'localhost'"

**Causa:** Variável `DB_PASSWORD` não configurada ou incorreta.

**Solução:**
1. Verifique se `DB_PASSWORD` está definida no painel da PaaS
2. Verifique se o valor está correto (sem espaços extras)
3. Verifique se o usuário do banco tem permissões adequadas

#### ❌ Erro: "Unable to connect to Redis"

**Causa:** Redis não está rodando ou configurações incorretas.

**Solução:**
1. Verifique se o serviço Redis está rodando na PaaS
2. Verifique `REDIS_HOST` e `REDIS_PORT`
3. Se não tiver Redis, defina `CACHE_ENABLED=false` ou use cache simples

#### ❌ Erro: "JWT secret is too short"

**Causa:** `JWT_SECRET` tem menos de 32 caracteres.

**Solução:**
```bash
# Gere um secret seguro
openssl rand -base64 32
# Configure no painel da PaaS
```

#### ❌ Aplicação funciona, mas não carrega assets (CSS/JS)

**Causa:** Configuração de caminhos estáticos incorreta ou cache.

**Solução:**
1. Verifique se os arquivos estão em `src/main/resources/static/`
2. Limpe o cache do navegador
3. Verifique logs para erros 404

#### ❌ Migrações de banco não são aplicadas

**Causa:** `spring.jpa.hibernate.ddl-auto=none` em produção.

**Solução:**
1. Execute scripts SQL manualmente no banco de produção
2. Ou configure Flyway/Liquibase para gerenciar migrações

### Logs e Monitoramento

#### Ver Logs

**Railway:**
- Dashboard → Service → Logs

**Render:**
- Dashboard → Service → Logs

**Docker:**
```bash
docker logs arirang-plataforma
docker logs -f arirang-plataforma  # Follow mode
```

#### Métricas Importantes

- **Tempo de resposta** (latência)
- **Taxa de erro** (5xx, 4xx)
- **Uso de memória/CPU**
- **Conexões de banco de dados**
- **Cache hit rate** (Redis)

---

## 📚 Recursos Adicionais

- [Documentação Spring Boot](https://spring.io/projects/spring-boot)
- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Railway Documentation](https://docs.railway.app)
- [Render Documentation](https://render.com/docs)

---

## ✅ Checklist Final de Deploy

Antes de cada deploy em produção, verifique:

- [ ] Código testado em homologação
- [ ] Todas as variáveis de ambiente configuradas
- [ ] Migrações de banco aplicadas (se houver)
- [ ] Backup do banco de dados feito
- [ ] Logs de homologação revisados
- [ ] Documentação atualizada
- [ ] Equipe notificada sobre o deploy
- [ ] Plano de rollback definido
- [ ] Monitoramento ativo durante deploy

---

**Última atualização:** 2025-01-07  
**Versão:** 1.0.0  
**Mantenedor:** AriranG Team

