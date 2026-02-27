# 📋 Resumo do Projeto AriranG Plataforma

## 🎯 Objetivo do Projeto

O **AriranG Plataforma** é um sistema completo de gestão escolar desenvolvido especificamente para escolas de idiomas. A aplicação oferece uma solução integrada para gerenciar todos os aspectos operacionais de uma escola de idiomas, desde o cadastro de alunos e professores até o controle financeiro e geração de documentos acadêmicos.

### Principais Funcionalidades

1. **Gestão de Alunos**
   - CRUD completo com validações rigorosas
   - Sistema de responsáveis para menores de 18 anos
   - Vinculação a múltiplas turmas
   - Busca inteligente (nome, email, telefone)
   - Geração de crachás personalizados
   - Relatórios individuais e por turma

2. **Gestão de Professores**
   - Cadastro completo de funcionários
   - Registro de formação acadêmica
   - Vinculação a turmas como responsável
   - Filtros por formação e turma
   - Sistema de busca avançado

3. **Gestão de Turmas**
   - Criação e gerenciamento de turmas
   - Controle de status (ativas, fechadas, reabertas)
   - Vinculação de professor responsável
   - Múltiplos alunos por turma
   - Fechamento inteligente com validação de boletins
   - Busca por nome, professor ou nível de proficiência

4. **Sistema de Boletins**
   - Lançamento de notas por categorias
   - Tipos de avaliação: exercícios, trabalhos, avaliações
   - Categorias: produção oral/escrita, compreensão, prova final
   - Cálculo automático de média e situação
   - Geração de boletins em PDF
   - Controle de finalização

5. **Gestão Financeira**
   - Controle de contratos de matrícula
   - Gerenciamento de parcelas e mensalidades
   - Registro de pagamentos e receitas
   - Geração de carnês de pagamento
   - Relatórios financeiros
   - Suporte a pagamentos parciais e integrais

6. **Sistema de Contratos**
   - Criação e gerenciamento de contratos de matrícula
   - Controle de vigência e status
   - Geração automática de parcelas
   - Vinculação com alunos e turmas
   - Geração de PDFs de contratos

7. **Documentos e Relatórios**
   - Geração de boletins em PDF
   - Declarações de matrícula
   - Carnês de pagamento
   - Contratos em PDF
   - Relatórios diversos

8. **Sistema de Busca**
   - Busca global em todas as listas
   - Combinação de filtros específicos
   - Persistência de termos de busca
   - Performance otimizada

---

## 🛠️ Principais Tecnologias

### Backend

- **Java 21** - Linguagem de programação principal
- **Spring Boot 3.2.5** - Framework base da aplicação
- **Spring Security** - Autenticação e autorização
- **Spring Data JPA** - Persistência de dados
- **Hibernate** - ORM (Object-Relational Mapping)
- **JWT (JSON Web Tokens)** - Autenticação via tokens (io.jsonwebtoken 0.11.5)
- **Redis** - Cache distribuído (opcional)
- **Maven 3.9+** - Gerenciamento de dependências e build
- **MapStruct 1.5.5** - Mapeamento automático entre Entity e DTO
- **Bean Validation (Jakarta)** - Validação de dados
- **OpenPDF 1.3.30** - Geração de documentos PDF

### Frontend

- **Thymeleaf** - Template engine server-side
- **HTML5** - Marcação semântica
- **CSS3** - Estilização responsiva
- **JavaScript** - Interatividade e validações client-side
- **Spring Security Thymeleaf Extras** - Integração de segurança no frontend

### Banco de Dados

- **MySQL 8.0** - Banco de dados relacional principal
- **H2 Database** - Banco de dados em memória para testes

### Documentação e APIs

- **SpringDoc OpenAPI 2.0.2** - Documentação de APIs REST (Swagger/OpenAPI)

### Infraestrutura e Deploy

- **Docker** - Containerização (Dockerfile disponível)
- **Render** - Plataforma de deploy (render.yaml configurado)
- **Railway** - Alternativa de deploy (configurações disponíveis)

---

## 🏗️ Arquitetura

### Padrões Arquiteturais

- **MVC (Model-View-Controller)** - Separação de responsabilidades
- **Repository Pattern** - Abstração de acesso a dados
- **DTO Pattern** - Transferência de dados entre camadas
- **Service Layer** - Lógica de negócio isolada
- **RESTful APIs** - Endpoints REST para integração

### Estrutura de Camadas

```
┌─────────────────────────────────────┐
│      Controllers (MVC + REST)       │
├─────────────────────────────────────┤
│         Services (Business)         │
├─────────────────────────────────────┤
│      Repositories (Data Access)     │
├─────────────────────────────────────┤
│      Entities (JPA/Hibernate)      │
├─────────────────────────────────────┤
│         MySQL Database              │
└─────────────────────────────────────┘
```

### Componentes Principais

- **Controllers**: 20+ controladores (MVC e REST)
- **Services**: 16 serviços de negócio
- **Repositories**: 14 repositórios Spring Data JPA
- **Entities**: 20+ entidades JPA
- **DTOs**: 20+ Data Transfer Objects
- **Mappers**: 6 mappers MapStruct
- **Templates**: 67 templates Thymeleaf
- **CSS**: 6 folhas de estilo

---

## 🔒 Segurança

### Mecanismos Implementados

- **Spring Security** - Framework de segurança completo
- **JWT Authentication** - Autenticação stateless via tokens
- **Bean Validation** - Validação de dados em múltiplas camadas
- **Proteção SQL Injection** - Via JPA/Hibernate
- **HTTPS** - Em produção
- **CORS Configurado** - Para integrações frontend
- **Role-Based Access Control (RBAC)** - Controle de acesso por roles

### Validações de Negócio

- Turmas fechadas não permitem novos alunos
- Validação de boletins antes de fechar turmas
- Responsáveis obrigatórios para menores de 18 anos
- Validação de campos obrigatórios (frontend e backend)
- Validação de datas e valores financeiros

---

## 📊 Modelo de Dados

### Entidades Principais

- **Aluno** - Dados dos estudantes
- **Professor** - Funcionários professores
- **Turma** - Turmas de ensino
- **Contrato** - Contratos de matrícula
- **Parcela** - Parcelas de pagamento
- **Pagamento** - Registros de pagamento
- **Receita** - Receitas financeiras
- **Boletim** - Boletins escolares
- **Nota** - Notas de avaliações
- **Avaliacao** - Avaliações aplicadas
- **Usuario** - Usuários do sistema
- **Responsavel** - Responsáveis pelos alunos

### Relacionamentos Principais

- Turma ↔ Professor: `Many-to-One`
- Turma ↔ Aluno: `Many-to-Many`
- Aluno ↔ Responsável: `One-to-One`
- Aluno ↔ Contrato: `One-to-Many`
- Contrato ↔ Parcela: `One-to-Many`
- Parcela ↔ Pagamento: `One-to-Many`
- Boletim ↔ Aluno: `One-to-One`
- Boletim ↔ Nota: `One-to-Many`

---

## 🚀 Performance e Otimizações

### Backend

- **Lazy Loading** - Carregamento sob demanda de relacionamentos
- **Cache Redis** - Cache distribuído para dados frequentes
- **Queries Otimizadas** - JOINs eficientes e índices
- **Paginação** - Para listas grandes
- **Eager Fetching** - Quando necessário para evitar N+1 queries

### Frontend

- **CSS Otimizado** - Organizado e modular
- **Validações Client-Side** - Reduz requisições desnecessárias
- **Imagens Otimizadas** - Comprimidas e em formatos adequados
- **Reutilização de Componentes** - Templates Thymeleaf reutilizáveis

---

## 📦 Dependências Principais (Maven)

```xml
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-thymeleaf
- spring-boot-starter-data-redis
- mysql-connector-j
- mapstruct
- jjwt (JWT)
- openpdf
- springdoc-openapi-starter-webmvc-ui
```

---

## 🎨 Interface do Usuário

### Características

- Design responsivo para todos os dispositivos
- Tema consistente e profissional (dark theme)
- Navegação intuitiva com menu lateral
- Feedback visual (pop-ups de sucesso/erro)
- Confirmações para ações críticas
- Formulários com validação em tempo real

### Tecnologias Frontend

- Thymeleaf para renderização server-side
- CSS3 com gradientes e animações
- JavaScript vanilla para interatividade
- Spring Security integration no frontend

---

## 📈 Métricas do Projeto

- **Linhas de Código**: ~15.000+ linhas Java
- **Controllers**: 20+ arquivos
- **Services**: 16 serviços
- **Repositories**: 14 repositórios
- **Entities**: 20+ entidades
- **Templates**: 67 templates HTML
- **Arquivos CSS**: 6 folhas de estilo
- **Módulos CRUD**: 7 principais (Alunos, Professores, Turmas, Contratos, Financeiro, Boletins, Avaliações)
- **APIs REST**: Endpoints completos para integração
- **Regras de Negócio**: 30+ validações

---

## 🔧 Configuração e Deploy

### Ambientes

- **Development** (`dev`) - Ambiente de desenvolvimento local
- **Homologation** (`homolog`) - Ambiente de homologação
- **Production** (`prod`) - Ambiente de produção
- **Test** (`test`) - Ambiente de testes

### Variáveis de Ambiente Principais

- `SPRING_PROFILES_ACTIVE` - Perfil ativo
- `SPRING_DATASOURCE_URL` - URL do banco de dados
- `SPRING_DATASOURCE_USERNAME` - Usuário do banco
- `SPRING_DATASOURCE_PASSWORD` - Senha do banco
- `JWT_SECRET` - Chave secreta para JWT
- `CACHE_ENABLED` - Habilitar/desabilitar cache Redis
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD` - Configurações Redis

---

## ✅ Pontos Fortes do Projeto

- 🏗️ Arquitetura sólida com padrões bem implementados
- 🔒 Segurança robusta com múltiplas camadas
- 🎨 UX excelente e interface intuitiva
- 📊 Funcionalidades completas end-to-end
- 🛡️ Validações rigorosas em frontend e backend
- 🔍 Sistema de busca avançado
- 📱 Totalmente responsivo
- 🚀 Otimizado para produção
- 📄 Geração de documentos (PDF)
- 💰 Sistema financeiro completo
- 📝 Gestão acadêmica integrada

---

## 📚 Documentação Disponível

- `README.md` - Documentação principal
- `ESTRUTURA_PROJETO.md` - Estrutura e lógica do projeto
- `DEPLOY-GUIDE.md` - Guia de deploy
- `RAILWAY_GUIDE.md` - Guia para Railway
- `SETUP_ENV.md` - Configuração de ambiente
- `SYNC_CHECKLIST.md` - Checklist de sincronização

---

## 🎯 Casos de Uso Principais

1. **Gestão Acadêmica**
   - Cadastro e gerenciamento de alunos
   - Criação e organização de turmas
   - Lançamento de notas e geração de boletins
   - Controle de avaliações

2. **Gestão Financeira**
   - Criação de contratos de matrícula
   - Gerenciamento de parcelas e mensalidades
   - Registro de pagamentos
   - Geração de carnês e relatórios financeiros

3. **Gestão de Pessoal**
   - Cadastro de professores
   - Vinculação de professores a turmas
   - Controle de funcionários

4. **Documentação**
   - Geração de boletins em PDF
   - Emissão de declarações
   - Geração de contratos
   - Criação de crachás

---

## 🔮 Roadmap Futuro

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

---

**Versão do Projeto**: 0.0.1-SNAPSHOT  
**Licença**: MIT  
**Desenvolvedor Principal**: Murilo Melo  
**Organização**: AriranG

