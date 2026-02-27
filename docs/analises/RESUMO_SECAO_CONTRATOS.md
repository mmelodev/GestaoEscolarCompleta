# 📋 Resumo da Seção de Contratos - AriranG Plataforma

## 🎯 Visão Geral

A seção de contratos é um módulo completo e integrado do sistema AriranG que gerencia contratos de matrícula de alunos em turmas. O sistema permite criar, editar, visualizar, deletar e gerar documentos PDF de contratos, além de integrar-se automaticamente com o módulo financeiro para geração de parcelas e controle de pagamentos.

---

## 🏗️ Arquitetura e Componentes

### 1. **Entidade Principal: `Contrato`**

**Localização:** `br.com.arirang.plataforma.entity.Contrato`

**Características:**
- Relacionamento `Many-to-One` com `Aluno` (obrigatório)
- Relacionamento `Many-to-One` com `Turma` (obrigatório)
- Número de contrato único e gerado automaticamente
- Campos financeiros: valor matrícula, valor mensalidade, número de parcelas, descontos
- Cálculo automático do valor total do contrato
- Controle de situação: ATIVO, CANCELADO, SUSPENSO
- Datas de vigência (início e fim)
- Observações e metadados (data criação, atualização)

**Campos Principais:**
```java
- id: Long
- aluno: Aluno (ManyToOne, obrigatório)
- turma: Turma (ManyToOne, obrigatório)
- numeroContrato: String (único, gerado automaticamente)
- dataContrato: LocalDate (obrigatório)
- dataInicioVigencia: LocalDate (transient)
- dataFimVigencia: LocalDate (transient)
- valorMatricula: BigDecimal
- valorMensalidade: BigDecimal
- numeroParcelas: Integer
- descontoValor: BigDecimal
- descontoPercentual: BigDecimal
- valorTotalContrato: BigDecimal (calculado automaticamente)
- observacoes: String
- situacaoContrato: String (ATIVO, CANCELADO, SUSPENSO)
- dataCriacao: LocalDateTime
- dataAtualizacao: LocalDateTime
```

### 2. **DTO: `ContratoDTO`**

**Localização:** `br.com.arirang.plataforma.dto.ContratoDTO`

**Características:**
- Record Java (imutável)
- Inclui nomes do aluno e turma para exibição
- Validações Bean Validation
- Métodos estáticos para criação: `createNew()`, `of()`

**Validações:**
- Aluno e Turma obrigatórios
- Datas obrigatórias
- Valores monetários devem ser positivos

### 3. **Mapper: `ContratoMapper`**

**Localização:** `br.com.arirang.plataforma.mapper.ContratoMapper`

**Funcionalidades:**
- Mapeamento automático Entity ↔ DTO usando MapStruct
- Extração de IDs e nomes de relacionamentos
- Método `toDtoLazy()` para performance

### 4. **Repository: `ContratoRepository`**

**Localização:** `br.com.arirang.plataforma.repository.ContratoRepository`

**Métodos Principais:**
- `findByAlunoIdOrderByDataCriacaoDesc(Long alunoId)` - Contratos por aluno
- `findByTurmaIdOrderByDataCriacaoDesc(Long turmaId)` - Contratos por turma
- `findBySituacaoContratoOrderByDataCriacaoDesc(String situacao)` - Por situação
- `findByNumeroContrato(String numeroContrato)` - Busca por número
- `existsByAlunoIdAndTurmaIdAndSituacaoContrato(...)` - Verifica existência
- `findContratosWithFilters(...)` - Busca avançada com múltiplos filtros
- `countContratosAtivosByTurma(Long turmaId)` - Contagem de ativos

**Query Customizada:**
```java
@Query("SELECT c FROM Contrato c WHERE " +
       "c.aluno.id IS NOT NULL AND " +
       "c.turma.id IS NOT NULL AND " +
       "(:alunoId IS NULL OR c.aluno.id = :alunoId) AND " +
       "(:turmaId IS NULL OR c.turma.id = :turmaId) AND " +
       "(:situacao IS NULL OR c.situacaoContrato = :situacao) AND " +
       "(:numeroContrato IS NULL OR c.numeroContrato LIKE CONCAT('%', :numeroContrato, '%')) " +
       "ORDER BY c.dataCriacao DESC")
```

### 5. **Service: `ContratoService`**

**Localização:** `br.com.arirang.plataforma.service.ContratoService`

**Métodos Principais:**

#### Consultas
- `listarTodosContratos()` - Lista todos
- `buscarContratoPorId(Long id)` - Busca por ID
- `listarContratosPorAluno(Long alunoId)` - Por aluno
- `listarContratosPorTurma(Long turmaId)` - Por turma
- `buscarContratosAtivos()` - Apenas ativos
- `buscarContratosComFiltros(...)` - Busca avançada

#### Operações CRUD
- `criarContrato(ContratoDTO)` - Cria novo contrato
- `atualizarContrato(Long id, ContratoDTO)` - Atualiza existente
- `deletarContrato(Long id)` - Remove contrato

#### Funcionalidades Especiais
- `gerarContratoRapido(Long alunoId, Long turmaId)` - Criação rápida
- `gerarNumeroContrato()` - Gera número único (formato: CTRYYYYMM####)
- `calcularValorTotalContrato(Contrato)` - Calcula valor total
- `gerarParcelasAutomaticamente(Contrato)` - Cria parcelas automaticamente

#### Validações
- `validarCriacaoContrato(ContratoDTO)` - Validações de negócio

### 6. **Controller: `ContratoController`**

**Localização:** `br.com.arirang.plataforma.controller.ContratoController`

**Endpoints MVC:**

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/contratos` | Lista todos os contratos com filtros |
| GET | `/contratos/novo` | Formulário de novo contrato |
| POST | `/contratos` | Cria novo contrato |
| GET | `/contratos/editar/{id}` | Formulário de edição |
| POST | `/contratos/atualizar/{id}` | Atualiza contrato |
| GET | `/contratos/deletar/{id}` | Confirmação de deleção |
| POST | `/contratos/deletar/{id}` | Deleta contrato |
| GET | `/contratos/visualizar/{id}` | Visualiza detalhes |
| GET | `/contratos/pdf/{id}` | Gera PDF do contrato |
| POST | `/contratos/gerar-rapido` | Gera contrato rápido |

**Parâmetros de Filtro (GET `/contratos`):**
- `alunoId` - Filtrar por aluno
- `turmaId` - Filtrar por turma
- `situacao` - Filtrar por situação (ATIVO, CANCELADO, SUSPENSO)
- `numeroContrato` - Buscar por número
- `search` - Busca geral

---

## 🔄 Fluxos e Funcionalidades

### 1. **Criação de Contrato**

**Fluxo:**
1. Usuário acessa `/contratos/novo` (opcionalmente com `alunoId` e `turmaId`)
2. Sistema carrega lista de alunos e turmas para seleção
3. Usuário preenche formulário:
   - Seleciona aluno e turma
   - Define valores (matrícula, mensalidade)
   - Define número de parcelas
   - Define datas de vigência
   - Adiciona observações
4. Sistema valida:
   - Aluno e turma existem
   - Não existe contrato ativo para o mesmo aluno/turma
   - Turma não está fechada
   - Datas são válidas
5. Sistema cria contrato:
   - Gera número único (CTRYYYYMM####)
   - Calcula valor total automaticamente
   - Salva no banco
6. Sistema gera parcelas automaticamente (se houver mensalidade e número de parcelas):
   - Cria parcelas mensais
   - Primeiro vencimento: 1 mês após data do contrato
   - Status inicial: PENDENTE

### 2. **Geração Automática de Parcelas**

**Quando ocorre:**
- Automaticamente após criação de contrato
- Apenas se `valorMensalidade > 0` e `numeroParcelas > 0`
- Não cria se já existem parcelas para o contrato

**Lógica:**
```java
- Data primeiro vencimento: dataContrato + 1 mês
- Cada parcela: valorMensalidade
- Intervalo: mensal
- Status: PENDENTE
- Numeração: sequencial (1, 2, 3, ...)
```

### 3. **Cálculo de Valor Total**

**Fórmula:**
```
Valor Total = Valor Matrícula + (Valor Mensalidade × Número de Parcelas) - Desconto Valor
```

**Observações:**
- Desconto percentual não é aplicado automaticamente (deve ser calculado manualmente)
- Valor mínimo: R$ 0,00

### 4. **Geração de Número de Contrato**

**Formato:** `CTRYYYYMM####`

**Exemplo:** `CTR2024120001`

**Lógica:**
- `CTR` - Prefixo fixo
- `YYYY` - Ano atual (4 dígitos)
- `MM` - Mês atual (2 dígitos)
- `####` - Sequencial (4 dígitos, baseado no total de contratos)

### 5. **Validações de Negócio**

**Ao criar contrato:**
- ✅ Não pode existir contrato ativo para o mesmo aluno/turma
- ✅ Turma não pode estar fechada
- ✅ Data de fim deve ser posterior à data de início
- ✅ Aluno e turma devem existir

**Ao deletar contrato:**
- ❌ Contratos cancelados não podem ser deletados
- ⚠️ Deve verificar se há parcelas/pagamentos vinculados

### 6. **Busca e Filtros**

**Filtros disponíveis:**
- Por aluno (dropdown)
- Por turma (dropdown)
- Por situação (ATIVO, CANCELADO, SUSPENSO)
- Por número de contrato (busca parcial)
- Busca geral (campo de texto)

**Query otimizada:**
- Exclui contratos com alunos ou turmas deletados
- Ordenação por data de criação (mais recentes primeiro)
- Suporta múltiplos filtros simultâneos

---

## 🔗 Integrações

### 1. **Integração com Módulo Financeiro**

**Relacionamentos:**
- `Contrato` → `Parcela` (One-to-Many)
- `Parcela` → `Pagamento` (One-to-Many)
- `Contrato` → `Financeiro` (One-to-Many)
- `Contrato` → `ComprovantePagamento` (One-to-Many)

**Sincronização Automática:**
- Ao criar contrato com parcelas, sistema cria entidades `Parcela`
- Parcelas podem gerar `Receita` no módulo financeiro
- Pagamentos registrados atualizam status das parcelas

### 2. **Integração com Alunos**

**Funcionalidades:**
- Lista de contratos por aluno (`/alunos/{id}/contratos`)
- Validação de aluno existente ao criar contrato
- Exibição de nome do aluno nos contratos

### 3. **Integração com Turmas**

**Funcionalidades:**
- Lista de contratos por turma
- Validação de turma ativa ao criar contrato
- Uso de datas de início/fim da turma como padrão

---

## 📄 Templates HTML

### 1. **`contratos.html`** - Lista Principal

**Funcionalidades:**
- Tabela com todos os contratos
- Filtros laterais (aluno, turma, situação)
- Busca por número de contrato
- Ações: Visualizar, Editar, PDF, Deletar
- Badges de situação (ATIVO, CANCELADO, SUSPENSO)
- Exibição de vigência (início - fim)

**Colunas:**
- Número
- Aluno
- Turma
- Data Contrato
- Vigência
- Valor Total
- Situação
- Ações

### 2. **`contrato-form.html`** - Formulário

**Funcionalidades:**
- Formulário unificado (criação e edição)
- Seleção de aluno (dropdown)
- Seleção de turma (dropdown)
- Campos financeiros (matrícula, mensalidade, parcelas)
- Campos de desconto (valor e percentual)
- Datas de vigência
- Campo de observações
- Validação client-side e server-side

### 3. **`contrato-view.html`** - Visualização

**Seções:**
- Informações do Contrato (número, situação, data)
- Informações do Aluno e Turma
- Período de Vigência
- Informações Financeiras (matrícula, mensalidade, parcelas, desconto, total)
- Observações (se houver)
- Informações do Sistema (datas de criação/atualização)

**Ações disponíveis:**
- Editar
- Gerar PDF
- Deletar
- Voltar para lista

### 4. **`contrato-pdf.html`** - Geração de PDF

**Características:**
- Template otimizado para impressão/PDF
- Layout profissional
- Todas as informações do contrato
- Formatação adequada para documento legal

### 5. **`contrato-delete.html`** - Confirmação de Deleção

**Funcionalidades:**
- Exibe dados do contrato
- Checkbox de confirmação
- Botão de deletar
- Link para cancelar

### 6. **`aluno-contratos.html`** - Contratos do Aluno

**Funcionalidades:**
- Lista contratos de um aluno específico
- Integrado na página de detalhes do aluno
- Link para criar novo contrato
- Ações rápidas (editar, visualizar, PDF, deletar)

---

## 🔒 Validações e Regras de Negócio

### Validações de Entrada

**Campos Obrigatórios:**
- Aluno
- Turma
- Data do Contrato
- Data de Início de Vigência
- Data de Fim de Vigência

**Validações de Formato:**
- Valores monetários: `DecimalMin(0.0)`
- Datas: formato válido
- Número de parcelas: inteiro positivo

### Regras de Negócio

1. **Unicidade:**
   - Não pode existir mais de um contrato ATIVO para o mesmo aluno/turma

2. **Integridade:**
   - Aluno e turma devem existir e estar ativos
   - Turma não pode estar fechada ao criar contrato

3. **Datas:**
   - Data de fim deve ser posterior à data de início
   - Data do contrato não pode ser futura (validação opcional)

4. **Deleção:**
   - Contratos cancelados não podem ser deletados
   - Deve verificar dependências (parcelas, pagamentos)

5. **Cálculos:**
   - Valor total calculado automaticamente
   - Desconto aplicado corretamente

---

## 🎨 Interface do Usuário

### Design

- **Tema:** Dark theme consistente com o resto da aplicação
- **Cores:**
  - Status ATIVO: Verde (`#d4edda`)
  - Status CANCELADO: Vermelho (`#f8d7da`)
  - Status SUSPENSO: Amarelo (`#fff3cd`)
- **Navegação:** Integrada com menu principal
- **Responsividade:** Totalmente responsivo

### Componentes Visuais

- **Badges de Situação:** Cores diferentes para cada status
- **Botões de Ação:** Ícones intuitivos (👁️ Visualizar, ✏️ Editar, 📄 PDF, 🗑️ Deletar)
- **Tabelas:** Ordenáveis e filtradas
- **Formulários:** Validação em tempo real
- **Mensagens:** Feedback de sucesso/erro

---

## 📊 Métricas e Estatísticas

### Métodos de Consulta

- **Total de contratos:** `count()`
- **Contratos ativos por turma:** `countContratosAtivosByTurma()`
- **Contratos por aluno:** `listarContratosPorAluno()`
- **Contratos por turma:** `listarContratosPorTurma()`

### Performance

- **Lazy Loading:** Relacionamentos carregados sob demanda
- **Eager Fetching:** Quando necessário (ex: visualização)
- **Cache:** Possível integração com Redis
- **Queries Otimizadas:** JOINs eficientes, índices apropriados

---

## 🔧 Funcionalidades Especiais

### 1. **Geração Rápida de Contrato**

**Endpoint:** `POST /contratos/gerar-rapido`

**Parâmetros:**
- `alunoId` (obrigatório)
- `turmaId` (obrigatório)

**Comportamento:**
- Cria contrato com valores padrão
- Usa datas da turma como vigência
- Gera parcelas automaticamente
- Útil para criação em massa

### 2. **Busca Avançada**

**Recursos:**
- Múltiplos filtros simultâneos
- Busca parcial por número de contrato
- Exclusão automática de registros deletados
- Ordenação por data de criação

### 3. **Integração com Financeiro**

**Sincronização:**
- Parcelas geradas automaticamente
- Receitas criadas quando necessário
- Status atualizado com pagamentos

---

## 🐛 Tratamento de Erros

### Exceções Customizadas

- `ResourceNotFoundException` - Recurso não encontrado
- `BusinessException` - Violação de regra de negócio

### Tratamento no Controller

- Try-catch em todos os métodos
- Logging de erros
- Mensagens amigáveis ao usuário
- Redirecionamento para página de erro quando necessário

### Validações

- **Frontend:** JavaScript e HTML5
- **Backend:** Bean Validation
- **Negócio:** Validações customizadas no Service

---

## 📝 Observações Técnicas

### Padrões Utilizados

- **MVC:** Separação clara de responsabilidades
- **DTO Pattern:** Transferência de dados entre camadas
- **Repository Pattern:** Abstração de acesso a dados
- **Service Layer:** Lógica de negócio isolada
- **MapStruct:** Mapeamento automático Entity ↔ DTO

### Boas Práticas

- ✅ Transações gerenciadas (`@Transactional`)
- ✅ Logging adequado (SLF4J)
- ✅ Validações em múltiplas camadas
- ✅ Tratamento de exceções robusto
- ✅ Código limpo e documentado
- ✅ Queries otimizadas
- ✅ Lazy loading quando apropriado

### Melhorias Futuras Sugeridas

- [ ] Paginação na lista de contratos
- [ ] Exportação para Excel/CSV
- [ ] Histórico de alterações (auditoria)
- [ ] Notificações por email
- [ ] Assinatura digital de contratos
- [ ] Templates de contrato customizáveis
- [ ] Relatórios avançados
- [ ] API REST completa
- [ ] Testes unitários e de integração

---

## 🔗 Relacionamentos com Outras Entidades

```
Contrato
├── Aluno (ManyToOne) - Obrigatório
├── Turma (ManyToOne) - Obrigatório
├── Parcela (OneToMany) - Parcelas do contrato
│   └── Pagamento (OneToMany) - Pagamentos da parcela
├── Financeiro (OneToMany) - Movimentos financeiros
└── ComprovantePagamento (OneToMany) - Comprovantes
```

---

## 📚 Arquivos Relacionados

### Java
- `Contrato.java` - Entidade
- `ContratoDTO.java` - DTO
- `ContratoMapper.java` - Mapper MapStruct
- `ContratoRepository.java` - Repository
- `ContratoService.java` - Service
- `ContratoController.java` - Controller

### Templates HTML
- `contratos.html` - Lista principal
- `contrato-form.html` - Formulário
- `contrato-view.html` - Visualização
- `contrato-pdf.html` - PDF
- `contrato-delete.html` - Deleção
- `aluno-contratos.html` - Contratos do aluno

### CSS
- `alunos.css` - Estilos principais (reutilizado)

---

**Última atualização:** Dezembro 2024  
**Versão do módulo:** 1.0  
**Status:** ✅ Funcional e em produção

