# 🎓 AriranG Plataforma

> Sistema completo de gestão para escolas de idiomas

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📋 Sobre o Projeto

Uma aplicação web robusta desenvolvida para gestão completa de escolas de idiomas. O sistema oferece funcionalidades abrangentes para gerenciamento de alunos, professores, turmas, boletins e muito mais, com foco em usabilidade e segurança.

## ⚡ Quick Start

Para começar rapidamente:

```bash
# 1. Clone o repositório
git clone https://github.com/seu-usuario/arirang-plataforma.git
cd arirang-plataforma

# 2. Configure o banco de dados MySQL
mysql -u root -p
CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;

# 3. Configure as variáveis de ambiente
cp env.example .env
# Edite .env e configure DB_PASSWORD e JWT_SECRET

# 4. Execute a aplicação
# Windows:
.\mvnw.cmd spring-boot:run
# Linux/Mac:
./mvnw spring-boot:run

# 5. Acesse http://localhost:8080
```

**📖 Para instruções detalhadas, veja a seção [🚀 Como Executar o Projeto](#-como-executar-o-projeto) abaixo.**

## ✨ Funcionalidades Principais

### 🎓 Gestão de Alunos
- CRUD completo com validações rigorosas
- Sistema de responsáveis para menores de 18 anos
- Vinculação a múltiplas turmas
- Busca inteligente (nome, email, telefone)
- Geração de crachás personalizados
- Relatórios individuais e por turma

### 👨‍🏫 Gestão de Professores
- Cadastro completo de funcionários
- Registro de formação acadêmica
- Vinculação a turmas como responsável
- Filtros por formação e turma
- Sistema de busca avançado

### 🏫 Gestão de Turmas
- Criação e gerenciamento de turmas
- Controle de status (ativas, fechadas, reabertas)
- Vinculação de professor responsável
- Múltiplos alunos por turma
- Fechamento inteligente com validação de boletins
- Busca por nome, professor ou nível de proficiência

### 📊 Sistema de Boletins
- Lançamento de notas por categorias
- Tipos de avaliação: exercícios, trabalhos, avaliações
- Categorias: produção oral/escrita, compreensão, prova final
- Cálculo automático de média e situação
- Geração de boletins em PDF
- Controle de finalização

### 🔍 Sistema de Busca
- Busca global em todas as listas
- Combinação de filtros específicos
- Persistência de termos de busca
- Performance otimizada

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 21** - Linguagem principal
- **Spring Boot 3.2.5** - Framework base
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM
- **JWT** - Tokens de autenticação
- **Redis** - Cache distribuído
- **Maven 3.9+** - Gerenciamento de dependências

### Frontend
- **Thymeleaf** - Template engine
- **HTML5** - Marcação semântica
- **CSS3** - Estilização responsiva
- **JavaScript** - Interatividade

### Banco de Dados
- **MySQL 8.0** - Banco de dados relacional

### Documentação
- **OpenAPI/Swagger** - Documentação de APIs

## 🏗️ Arquitetura

O projeto segue os seguintes padrões arquiteturais:

- ✅ **MVC (Model-View-Controller)**
- ✅ **Repository Pattern**
- ✅ **DTO Pattern**
- ✅ **Service Layer**
- ✅ **RESTful APIs**

### Estrutura de Pastas

```
arirang-plataforma/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/arirang/
│   │   │       ├── controller/     # Controladores MVC
│   │   │       ├── entity/         # Entidades JPA
│   │   │       ├── repository/     # Repositórios
│   │   │       ├── service/        # Lógica de negócio
│   │   │       ├── dto/            # Data Transfer Objects
│   │   │       ├── config/         # Configurações
│   │   │       └── security/       # Segurança
│   │   └── resources/
│   │       ├── templates/          # Templates Thymeleaf
│   │       ├── static/             # CSS, JS, imagens
│   │       └── application.yml     # Configurações
│   └── test/                       # Testes
├── pom.xml                         # Dependências Maven
└── README.md
```

## 🚀 Como Executar o Projeto

### 📦 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

1. **Java 21 ou superior**
   - Verifique a instalação: `java -version`
   - Download: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) ou [OpenJDK](https://openjdk.org/)

2. **Maven 3.9 ou superior**
   - Verifique a instalação: `mvn -version`
   - O projeto inclui Maven Wrapper (`mvnw` ou `mvnw.cmd`), então você pode usar sem instalar o Maven globalmente
   - Download: [Apache Maven](https://maven.apache.org/download.cgi)

3. **MySQL 8.0 ou superior**
   - Verifique a instalação: `mysql --version`
   - Download: [MySQL Community Server](https://dev.mysql.com/downloads/mysql/)

4. **Redis (Opcional)**
   - Necessário apenas se quiser usar cache Redis
   - Download: [Redis](https://redis.io/download)

### 🔧 Instalação das Dependências

Este projeto usa **Maven** para gerenciar dependências. As dependências são baixadas automaticamente na primeira compilação.

#### Opção 1: Usando Maven Wrapper (Recomendado)

O projeto inclui Maven Wrapper, então você não precisa instalar o Maven:

**Windows:**
```powershell
.\mvnw.cmd clean install
```

**Linux/Mac:**
```bash
./mvnw clean install
```

#### Opção 2: Usando Maven Global

Se você tem Maven instalado globalmente:

```bash
mvn clean install
```

**Nota:** Na primeira execução, o Maven baixará todas as dependências do `pom.xml`. Isso pode levar alguns minutos.

### 🗄️ Configuração do Banco de Dados

1. **Inicie o MySQL** e acesse o console:
```bash
mysql -u root -p
```

2. **Crie o banco de dados:**
```sql
CREATE DATABASE arirang_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **Saia do MySQL:**
```sql
EXIT;
```

**Nota:** O Hibernate criará automaticamente todas as tabelas na primeira execução (se `spring.jpa.hibernate.ddl-auto=update` estiver configurado).

### 🔐 Configuração de Variáveis de Ambiente

⚠️ **IMPORTANTE:** Este projeto usa variáveis de ambiente para credenciais. **NUNCA** commite arquivos `.env` no Git!

#### Passo 1: Criar arquivo `.env`

1. Copie o arquivo de exemplo:
```bash
# Windows PowerShell
Copy-Item env.example .env

# Linux/Mac
cp env.example .env
```

2. Edite o arquivo `.env` e configure suas credenciais:

```env
# Database Configuration
DB_URL=jdbc:mysql://localhost:3306/arirang_db?createDatabaseIfNotExist=true&serverTimezone=UTC
DB_USERNAME=root
DB_PASSWORD=sua_senha_mysql_aqui
DB_DRIVER=com.mysql.cj.jdbc.Driver

# JWT Configuration (OBRIGATÓRIO)
# Gere uma chave secreta segura (mínimo 32 caracteres):
# Windows: [Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
# Linux/Mac: openssl rand -base64 32
JWT_SECRET=sua_chave_jwt_secreta_minimo_32_caracteres_aqui
JWT_EXPIRATION=86400000

# Application Configuration
SPRING_PROFILES_ACTIVE=dev
```

#### Passo 2: Carregar variáveis de ambiente

**Windows (PowerShell):**
```powershell
# Carregar variáveis do arquivo .env
Get-Content .env | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $name = $matches[1].Trim()
        $value = $matches[2].Trim()
        [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
    }
}
```

**Windows (CMD):**
```cmd
# Configure manualmente cada variável
set DB_PASSWORD=sua_senha_mysql_aqui
set JWT_SECRET=sua_chave_jwt_secreta_aqui
```

**Linux/Mac:**
```bash
# Carregar variáveis do arquivo .env
export $(cat .env | grep -v '^#' | xargs)
```

**Alternativa:** Use o script `run.ps1` (Windows) que carrega automaticamente o `.env`:

```powershell
.\run.ps1
```

### 📋 Variáveis de Ambiente

#### ⚠️ Obrigatórias

| Variável | Descrição | Exemplo |
|----------|-----------|---------|
| `DB_PASSWORD` | Senha do banco de dados MySQL | `minhasenha123` |
| `JWT_SECRET` | Chave secreta para tokens JWT (mínimo 32 caracteres) | `chave_super_secreta_32_caracteres_minimo` |

#### 📌 Opcionais (com valores padrão)

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DB_URL` | URL de conexão do MySQL | `jdbc:mysql://localhost:3306/arirang_db?createDatabaseIfNotExist=true&serverTimezone=UTC` |
| `DB_USERNAME` | Usuário do MySQL | `root` |
| `DB_DRIVER` | Driver JDBC | `com.mysql.cj.jdbc.Driver` |
| `JWT_EXPIRATION` | Tempo de expiração do token JWT (ms) | `86400000` (24 horas) |
| `SPRING_PROFILES_ACTIVE` | Profile Spring Boot ativo | `dev` |
| `APP_DEFAULT_ADMIN_PASSWORD` | Senha do usuário admin padrão (apenas dev) | `admin123` |
| `REDIS_HOST` | Host do Redis (se usado) | `localhost` |
| `REDIS_PORT` | Porta do Redis | `6379` |
| `REDIS_PASSWORD` | Senha do Redis (se usado) | (vazio) |
| `CACHE_ENABLED` | Habilitar cache | `true` |
| `LOG_PATH` | Diretório de logs | `logs` |
| `LOG_SQL_LEVEL` | Nível de log SQL | `WARN` |

**📖 Consulte `env.example` para ver todas as variáveis disponíveis.**

### ▶️ Executando o Projeto

#### Opção 1: Usando Maven Wrapper (Recomendado)

**Windows:**
```powershell
# Certifique-se de que as variáveis de ambiente estão configuradas
.\mvnw.cmd spring-boot:run
```

**Linux/Mac:**
```bash
./mvnw spring-boot:run
```

#### Opção 2: Usando script helper (Windows)

```powershell
# O script carrega automaticamente o arquivo .env
.\run.ps1
```

#### Opção 3: Usando Maven Global

```bash
mvn spring-boot:run
```

#### Opção 4: Executar JAR compilado

```bash
# Compilar
mvn clean package

# Executar
java -jar target/plataforma-0.0.1-SNAPSHOT.jar
```

### 🌐 Acessando a Aplicação

Após iniciar a aplicação, acesse:

- **URL:** http://localhost:8080
- **Login padrão (desenvolvimento):**
  - Usuário: `admin`
  - Senha: Valor de `APP_DEFAULT_ADMIN_PASSWORD` (padrão: `admin123`)

### ✅ Verificação

Se tudo estiver configurado corretamente, você verá:

```
Started PlataformaApplication in X.XXX seconds
```

E poderá acessar a aplicação no navegador.

### 🐛 Solução de Problemas

#### Erro: "Access denied for user 'root'@'localhost'"
- **Causa:** Senha do MySQL não configurada
- **Solução:** Configure `DB_PASSWORD` no arquivo `.env`

#### Erro: "JWT_SECRET is too short"
- **Causa:** JWT_SECRET tem menos de 32 caracteres
- **Solução:** Gere uma chave com pelo menos 32 caracteres

#### Erro: "Could not connect to database"
- **Causa:** MySQL não está rodando ou credenciais incorretas
- **Solução:** Verifique se o MySQL está rodando e se as credenciais estão corretas

#### Erro: "Port 8080 already in use"
- **Causa:** Outra aplicação está usando a porta 8080
- **Solução:** Pare a outra aplicação ou configure `server.port` em `application.properties`

### 📚 Documentação Adicional

- **Configuração detalhada:** `docs/deploy/SETUP_ENV.md`
- **Deploy em produção:** `docs/deploy/DEPLOY_GUIDE.md`
- **Railway:** `docs/deploy/RAILWAY_GUIDE.md`

## 🧾 Logging

- Configuração centralizada em `src/main/resources/logback-spring.xml`
- Arquivos gravados (por padrão) em `logs/` – ajuste com a variável `LOG_PATH`
- Níveis do pacote `br.com.arirang.plataforma`:
  - `dev`: DEBUG no console + arquivo
  - `prod`: INFO no console/arquivo, Spring e Hibernate em `WARN`
- Para inspecionar SQL, defina `LOG_SQL_LEVEL=DEBUG` nas variáveis de ambiente (somente em desenvolvimento)

## ⚡ Cache Redis

- Redis habilitado por padrão quando disponível (`app.cache.enabled=true`)
- Configuração: `src/main/java/br/com/arirang/plataforma/config/RedisCacheConfig.java`
- Principais caches:
  - `usuarios`: autenticação (`UsuarioService.loadUserByUsername`)
  - `turmasLista` e `turmasPorId`: consultas de turmas
- Ajustes via variáveis de ambiente:
  - `CACHE_ENABLED` (`true`/`false`)
  - `CACHE_DEFAULT_TTL` (duração ISO-8601, ex.: `PT10M`)
  - `CACHE_ALLOW_NULL` (`false` recomendado)
  - `CACHE_KEY_PREFIX` (padrão `plataforma::`)
  - `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- Em ambientes sem Redis, defina `CACHE_ENABLED=false` ou utilize o profile `test`

## 🗄️ Modelo de Dados

### Entidades Principais

- **Aluno**: Dados pessoais, responsáveis, turmas vinculadas
- **Professor**: Herança de Funcionário, formação acadêmica
- **Turma**: Nome, professor responsável, alunos, status
- **Boletim**: Notas, média final, situação do aluno
- **Nota**: Tipos de avaliação e valores
- **Responsável**: Dados do responsável legal

### Relacionamentos

- Turma ↔ Professor: `Many-to-One`
- Turma ↔ Aluno: `Many-to-Many`
- Aluno ↔ Responsável: `One-to-One`
- Boletim ↔ Aluno: `One-to-One`
- Boletim ↔ Nota: `One-to-Many`

## 🔒 Segurança

### ⚠️ CRÍTICO: Proteção de Credenciais

**NUNCA commite credenciais no repositório Git!**

#### Arquivos que NUNCA devem ser commitados:
- ❌ Arquivos `.env` (use `env.example` como template)
- ❌ Arquivos com senhas, chaves de API ou tokens
- ❌ Arquivos `SECRETS_BACKUP.txt`, `secrets.txt`, `credentials.txt`
- ❌ Arquivos de configuração local (`application-local.properties`)

#### Boas Práticas:
- ✅ Use variáveis de ambiente para todas as credenciais
- ✅ Configure credenciais através de serviços de secrets (Railway, Render, etc.)
- ✅ Use `env.example` como template para documentar variáveis necessárias
- ✅ Revise commits antes de fazer push (`git diff` antes de `git commit`)
- ✅ Use `.gitignore` para proteger arquivos sensíveis

#### Variáveis de Ambiente Obrigatórias:
- `DB_PASSWORD` - Senha do banco de dados MySQL
- `JWT_SECRET` - Chave secreta para tokens JWT (mínimo 32 caracteres)

#### Variáveis de Ambiente Opcionais:
- `APP_DEFAULT_ADMIN_PASSWORD` - Senha do usuário admin padrão (apenas desenvolvimento)
- `REDIS_PASSWORD` - Senha do Redis (se usado)
- `CORS_ALLOWED_ORIGINS` - Origens permitidas para CORS (produção)

**📖 Consulte `docs/deploy/SETUP_ENV.md` para instruções detalhadas de configuração.**

### Validações de Negócio

- ✅ Turmas fechadas não permitem novos alunos
- ✅ Validação de boletins antes de fechar turmas
- ✅ Responsáveis obrigatórios para menores de 18 anos
- ✅ Validação de campos obrigatórios (frontend e backend)

### Mecanismos de Segurança

- ✅ Spring Security para autenticação e autorização
- ✅ JWT para tokens de API
- ✅ Bean Validation para validação de dados
- ✅ Proteção contra SQL Injection via JPA
- ✅ HTTPS em produção
- ✅ Senhas armazenadas com hash (BCrypt)
- ✅ Tokens JWT com expiração configurável

## 🎨 Interface do Usuário

### Características

- ✅ Design responsivo para todos os dispositivos
- ✅ Tema consistente e profissional
- ✅ Navegação intuitiva
- ✅ Feedback visual (pop-ups de sucesso/erro)
- ✅ Confirmações para ações críticas

### Páginas Disponíveis

- Home (página inicial)
- Listagem e formulários de alunos
- Listagem e formulários de professores
- Listagem e formulários de turmas
- Lançamento de boletins
- Cadastro unificado
- Geração de crachás

## 📊 Métricas do Projeto

### Código

- **Controllers**: 14 arquivos
- **Entities**: 13 entidades
- **Services**: 6 serviços
- **Repositories**: 8 repositórios
- **Templates**: 20+ templates HTML
- **Arquivos CSS**: 5 folhas de estilo

### Funcionalidades

- **Módulos CRUD**: 4 principais
- **APIs REST**: Endpoints completos
- **Regras de Negócio**: 20+ validações
- **Tipos de Relatórios**: 3
- **Integrações**: PDF, JWT, Redis

## 🚀 Performance e Otimizações

### Backend

- ✅ Lazy Loading para carregamento sob demanda
- ✅ Cache Redis para dados frequentes
- ✅ Queries otimizadas com JOINs eficientes
- ✅ Paginação para listas grandes

### Frontend

- ✅ CSS otimizado e organizado
- ✅ Validações client-side com JavaScript
- ✅ Imagens otimizadas e comprimidas
- ✅ Reutilização de componentes

## ✅ Pontos Fortes

- 🏗️ Arquitetura sólida com padrões bem implementados
- 🔒 Segurança robusta com múltiplas camadas
- 🎨 UX excelente e interface intuitiva
- 📊 Funcionalidades completas end-to-end
- 🛡️ Validações rigorosas em frontend e backend
- 🔍 Sistema de busca avançado
- 📱 Totalmente responsivo
- 🚀 Otimizado para produção

## 🔧 Roadmap

### Melhorias Futuras

- [ ] Implementar testes unitários e de integração
- [ ] Dashboard com analytics e métricas
- [ ] Sistema de notificações por email
- [ ] Aplicativo mobile nativo
- [ ] Suporte a múltiplos idiomas (i18n)
- [ ] Relatórios avançados com gráficos
- [ ] Sistema de auditoria e logs
- [ ] Integração com sistemas de pagamento
- [ ] API pública documentada
- [ ] Sistema de backup automatizado
