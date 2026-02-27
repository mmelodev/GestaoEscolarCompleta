# 🔍 Análise Profunda: Fluxo de Criação de Contratos - Endpoint `/contratos/novo`

## 📋 Visão Geral

Este documento analisa detalhadamente o fluxo completo de criação de contratos no sistema AriranG, começando pelo endpoint `GET /contratos/novo` até a persistência final no banco de dados.

---

## 🎯 Endpoint Principal: `GET /contratos/novo`

### Localização
**Arquivo:** `ContratoController.java`  
**Método:** `novoContratoForm()`  
**Linhas:** 163-188

### Código

```java
@GetMapping("/novo")
public String novoContratoForm(@RequestParam(value = "alunoId", required = false) Long alunoId,
                              @RequestParam(value = "turmaId", required = false) Long turmaId,
                              Model model) {
    try {
        ContratoDTO contrato = ContratoDTO.createNew(
            alunoId, turmaId, 
            java.time.LocalDate.now(),
            java.time.LocalDate.now(),
            java.time.LocalDate.now().plusMonths(6)
        );
        
        model.addAttribute("contrato", contrato);
        model.addAttribute("isNew", true);
        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
        model.addAttribute("alunosComTurmas", buildAlunosComTurmas());
        model.addAttribute("alunoTurmasMap", buildAlunoTurmasMap());
        model.addAttribute("turmas", turmaService.listarTodasTurmas());
        
    } catch (Exception e) {
        logger.error("Erro ao carregar formulário de contrato: ", e);
        model.addAttribute("error", "Erro ao carregar formulário: " + e.getMessage());
    }
    
    return "contrato-form";
}
```

### Análise do Endpoint

#### 1. **Parâmetros Opcionais na URL**
```java
@RequestParam(value = "alunoId", required = false) Long alunoId
@RequestParam(value = "turmaId", required = false) Long turmaId
```

**Funcionalidade:**
- Permite pré-selecionar aluno e/ou turma via query string
- Exemplo de uso: `/contratos/novo?alunoId=5&turmaId=10`
- Facilita criação de contratos a partir de páginas de alunos ou turmas

#### 2. **Criação do DTO Inicial**
```java
ContratoDTO contrato = ContratoDTO.createNew(
    alunoId, turmaId, 
    LocalDate.now(),           // dataContrato = hoje
    LocalDate.now(),           // dataInicioVigencia = hoje
    LocalDate.now().plusMonths(6) // dataFimVigencia = hoje + 6 meses
);
```

**Valores Padrão:**
- `id`: `null` (novo contrato)
- `alunoId`: valor do parâmetro ou `null`
- `turmaId`: valor do parâmetro ou `null`
- `dataContrato`: data atual
- `dataInicioVigencia`: data atual
- `dataFimVigencia`: data atual + 6 meses
- `valorMatricula`: `BigDecimal.ZERO`
- `valorMensalidade`: `BigDecimal.ZERO`
- `numeroParcelas`: `0`
- `descontoValor`: `BigDecimal.ZERO`
- `descontoPercentual`: `BigDecimal.ZERO`
- `valorTotalContrato`: `BigDecimal.ZERO`
- `observacoes`: string vazia
- `situacaoContrato`: `"ATIVO"`
- `templatePdf`: `null`

#### 3. **Preparação de Dados para o Frontend**

##### a) Lista de Alunos (`alunos`)
```java
model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
```
- **Propósito:** Preencher dropdown de seleção de alunos
- **Formato:** Lista de `AlunoDTO` (convertidos via `AlunoMapper`)
- **Uso no template:** `<option th:each="aluno : ${alunos}">`

##### b) Lista de Turmas (`turmas`)
```java
model.addAttribute("turmas", turmaService.listarTodasTurmas());
```
- **Propósito:** Preencher dropdown de seleção de turmas
- **Formato:** Lista de `TurmaDTO`
- **Uso no template:** `<option th:each="turma : ${turmas}">`

##### c) Alunos com Turmas (`alunosComTurmas`)
```java
model.addAttribute("alunosComTurmas", buildAlunosComTurmas());
```
- **Propósito:** Estrutura para sincronização JavaScript (aluno ↔ turma)
- **Formato:** Lista de `AlunoTurmaDTO` contendo:
  - `alunoId`
  - `alunoNome`
  - `List<TurmaDTO> turmas` (turmas vinculadas ao aluno)

##### d) Mapa Aluno-Turmas (`alunoTurmasMap`)
```java
model.addAttribute("alunoTurmasMap", buildAlunoTurmasMap());
```
- **Propósito:** Mapa JavaScript para filtro dinâmico de turmas
- **Formato:** `Map<Long, List<Long>>` (alunoId → lista de turmaIds)
- **Uso:** JavaScript filtra turmas disponíveis baseado no aluno selecionado

#### 4. **Métodos Auxiliares do Controller**

##### `buildAlunosComTurmas()` (linhas 61-73)
```java
private List<AlunoTurmaDTO> buildAlunosComTurmas() {
    List<Aluno> alunos = alunoService.listarTodosAlunos();
    return alunos.stream()
            .map(aluno -> {
                List<TurmaDTO> turmasDTO = aluno.getTurmas() != null
                        ? aluno.getTurmas().stream()
                                .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                                .toList()
                        : List.of();
                return new AlunoTurmaDTO(aluno.getId(), aluno.getNomeCompleto(), turmasDTO);
            })
            .toList();
}
```

**Funcionalidade:**
- Constroi estrutura que associa cada aluno às suas turmas
- Usa relacionamento `@ManyToMany` entre `Aluno` e `Turma`
- Retorna lista de DTOs simplificados

##### `buildAlunoTurmasMap()` (linhas 79-93)
```java
private Map<Long, List<Long>> buildAlunoTurmasMap() {
    Map<Long, List<Long>> map = new HashMap<>();
    List<Aluno> alunos = alunoService.listarTodosAlunos();
    
    for (Aluno aluno : alunos) {
        if (aluno.getTurmas() != null && !aluno.getTurmas().isEmpty()) {
            List<Long> turmaIds = aluno.getTurmas().stream()
                    .map(t -> t.getId())
                    .toList();
            map.put(aluno.getId(), turmaIds);
        }
    }
    
    return map;
}
```

**Funcionalidade:**
- Cria mapa para JavaScript filtrar turmas
- Chave: `alunoId`
- Valor: Lista de `turmaIds` vinculadas ao aluno
- Usado no template para validação/filtro client-side

---

## 📄 Template HTML: `contrato-form.html`

### Estrutura do Formulário

#### 1. **Cabeçalho e Navegação**
- Menu de navegação com link ativo "Contratos"
- Botão "← Voltar para Contratos"
- Título dinâmico: "Novo Contrato" ou "Editar Contrato"

#### 2. **Formulário Principal**
```html
<form th:action="${isNew} ? @{/contratos} : @{'/contratos/atualizar/' + ${contrato.id}}" 
      th:object="${contrato}" 
      method="post" 
      id="contratoForm">
```

**Características:**
- **Action dinâmica:** `/contratos` (POST) para novo, `/contratos/atualizar/{id}` para edição
- **Binding:** `th:object="${contrato}"` - vincula campos ao DTO
- **CSRF Token:** Incluído automaticamente (linha 63)

#### 3. **Seções do Formulário**

##### a) Template de PDF (linhas 66-83)
```html
<select id="templatePdf" name="templatePdf" th:field="*{templatePdf}">
    <option value="">Nenhum (usar padrão)</option>
    <option value="contrato-servicos-menor">📋 Contrato de Serviços (Menor)</option>
    <option value="contrato-curso">📋 Contrato de Curso (Maior)</option>
    <option value="uso-imagem-menor">📷 Autorização de Uso de Imagem (Menor)</option>
    <option value="uso-imagem-adulto">📷 Autorização de Uso de Imagem e Voz (Adulto)</option>
</select>
```

**Funcionalidade:**
- Seleção do template PDF que será usado ao gerar o documento
- Opcional - se não selecionado, usa template padrão
- Armazenado em `contrato.templatePdf`

##### b) Dados Básicos (linhas 85-139)

**Campos:**
- **Aluno** (`alunoId`): Select obrigatório com todos os alunos
- **Turma** (`turmaId`): Select obrigatório (filtrado dinamicamente via JavaScript)
- **Data do Contrato** (`dataContrato`): Input date obrigatório
- **Situação** (`situacaoContrato`): Select com opções ATIVO, SUSPENSO, CANCELADO

**Validação:**
- Campos marcados com `required`
- Validação Bean Validation no backend
- Feedback visual de erros via Thymeleaf (`th:errors`)

##### c) Período de Vigência (linhas 141-160)

**Campos:**
- **Data de Início** (`dataInicioVigencia`): Input date obrigatório
- **Data de Fim** (`dataFimVigencia`): Input date obrigatório

**Validação JavaScript:**
- Valida que data de fim é posterior à data de início (linhas 499-520)
- Feedback visual em tempo real

##### d) Parte Financeira (linhas 162-216)

**Campos:**
- **Valor da Matrícula** (`valorMatricula`): Number, step 0.01, min 0
- **Valor da Mensalidade** (`valorMensalidade`): Number, step 0.01, min 0
- **Número de Parcelas** (`numeroParcelas`): Number, min 0
- **Desconto em Valor** (`descontoValor`): Number, step 0.01, min 0
- **Valor Total do Contrato** (`valorTotalContrato`): Number, readonly, calculado automaticamente

**Cálculo Automático:**
```javascript
function calcularTotal() {
    const matricula = parseFloat(valorMatricula.value) || 0;
    const mensalidade = parseFloat(valorMensalidade.value) || 0;
    const parcelas = parseInt(numeroParcelas.value) || 0;
    const desconto = parseFloat(descontoValor.value) || 0;
    
    const total = (matricula + (mensalidade * parcelas)) - desconto;
    valorTotal.value = Math.max(0, total).toFixed(2);
}
```

**Fórmula:**
```
Valor Total = (Valor Matrícula + (Valor Mensalidade × Número de Parcelas)) - Desconto em Valor
```

**Observações:**
- Desconto percentual não é aplicado automaticamente
- Valor mínimo: R$ 0,00
- Cálculo em tempo real via JavaScript

##### e) Observações (linhas 218-229)

**Campo:**
- **Observações** (`observacoes`): Textarea opcional, 4 linhas

---

## 🔄 Sincronização Aluno ↔ Turma (JavaScript)

### Funcionalidade
Restringe as turmas disponíveis no dropdown baseado no aluno selecionado, mostrando apenas turmas às quais o aluno está vinculado.

### Código Principal (linhas 399-462)

```javascript
// Mapa alunoId -> [turmaIds]
const alunoTurmasMap = /*[[${alunoTurmasMap}]]*/ || {};

function atualizarTurmasParaAluno(alunoId) {
    const allowedIds = alunoTurmasMap[alunoId] || null;
    
    // Limpar opções atuais
    turmaSelect.innerHTML = '';
    
    // Recolocar apenas turmas permitidas
    turmaOptionsOriginal.slice(1).forEach(opt => {
        const valueNum = parseInt(opt.value, 10);
        if (!allowedIds || allowedIds.includes(valueNum)) {
            turmaSelect.appendChild(opt.cloneNode(true));
        }
    });
    
    // Se aluno tiver exatamente 1 turma, selecionar automaticamente
    if (allowedIds && allowedIds.length === 1) {
        turmaSelect.value = String(allowedIds[0]);
    }
}
```

### Regras de Negócio

1. **Aluno sem turmas vinculadas:**
   - Mostra todas as turmas disponíveis (sem restrição)

2. **Aluno com turmas vinculadas:**
   - Mostra apenas turmas às quais o aluno está vinculado
   - Se aluno tem exatamente 1 turma, seleciona automaticamente
   - Se aluno tem 0 ou mais de 1 turma, mantém placeholder

3. **Nenhum aluno selecionado:**
   - Restaura todas as turmas

---

## 📤 Submissão do Formulário: `POST /contratos`

### Endpoint
**Arquivo:** `ContratoController.java`  
**Método:** `criarContrato()`  
**Linhas:** 193-218

### Código

```java
@PostMapping
public String criarContrato(@Valid @ModelAttribute("contrato") ContratoDTO contrato,
                           BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
        model.addAttribute("isNew", true);
        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
        model.addAttribute("alunosComTurmas", buildAlunosComTurmas());
        model.addAttribute("alunoTurmasMap", buildAlunoTurmasMap());
        model.addAttribute("turmas", turmaService.listarTodasTurmas());
        return "contrato-form";
    }
    
    try {
        contratoService.criarContrato(contrato);
        return "redirect:/contratos?success=Contrato criado com sucesso";
    } catch (Exception e) {
        logger.error("Erro ao criar contrato: ", e);
        model.addAttribute("error", "Erro ao criar contrato: " + e.getMessage());
        model.addAttribute("isNew", true);
        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
        model.addAttribute("alunosComTurmas", buildAlunosComTurmas());
        model.addAttribute("alunoTurmasMap", buildAlunoTurmasMap());
        model.addAttribute("turmas", turmaService.listarTodasTurmas());
        return "contrato-form";
    }
}
```

### Processamento

#### 1. **Validação Bean Validation**
```java
@Valid @ModelAttribute("contrato") ContratoDTO contrato
BindingResult bindingResult
```

**Validações Aplicadas (do ContratoDTO):**
- `@NotNull` em `alunoId`
- `@NotNull` em `turmaId`
- `@NotNull` em `dataContrato`
- `@NotNull` em `dataInicioVigencia`
- `@NotNull` em `dataFimVigencia`
- `@DecimalMin(value = "0.0")` em valores monetários

**Se houver erros:**
- Retorna ao formulário com mensagens de erro
- Mantém dados preenchidos (via `th:field`)
- Exibe erros via `th:errors`

#### 2. **Chamada ao Service**
```java
contratoService.criarContrato(contrato);
```

#### 3. **Redirecionamento**
- **Sucesso:** `redirect:/contratos?success=Contrato criado com sucesso`
- **Erro:** Retorna ao formulário com mensagem de erro

---

## ⚙️ Service: `ContratoService.criarContrato()`

### Localização
**Arquivo:** `ContratoService.java`  
**Método:** `criarContrato()`  
**Linhas:** 205-249

### Fluxo Completo

#### 1. **Logging e Validação de Negócio**
```java
logger.debug("Criando novo contrato para aluno ID: {} e turma ID: {}", 
            contratoDTO.alunoId(), contratoDTO.turmaId());

// Validações de negócio
validarCriacaoContrato(contratoDTO);
```

#### 2. **Validações de Negócio (`validarCriacaoContrato`)**

**Localização:** linhas 440-455

```java
private void validarCriacaoContrato(ContratoDTO contratoDTO) {
    // ✅ NOVA REGRA: Permitir múltiplos contratos ativos para mesmo aluno/turma
    // Removida validação: existsByAlunoIdAndTurmaIdAndSituacaoContrato
    
    // Validar datas
    if (contratoDTO.dataFimVigencia().isBefore(contratoDTO.dataInicioVigencia())) {
        throw new BusinessException("Data de fim deve ser posterior à data de início");
    }

    // Validar se a turma existe (mas não precisa estar ativa)
    turmaService.buscarTurmaPorId(contratoDTO.turmaId())
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
    
    // ✅ NOVA REGRA: Permitir criar contrato mesmo para turma fechada
    // Removida validação: if ("FECHADA".equals(turma.getSituacaoTurma()))
}
```

**Validações Atuais:**
- ✅ Data de fim deve ser posterior à data de início
- ✅ Turma deve existir
- ❌ **REMOVIDO:** Validação de contrato duplicado (permitido múltiplos ativos)
- ❌ **REMOVIDO:** Validação de turma fechada (permitido criar para turma fechada)

#### 3. **Busca de Aluno e Turma**
```java
Aluno aluno = alunoService.buscarAlunoPorId(contratoDTO.alunoId())
        .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + contratoDTO.alunoId()));

Turma turma = turmaService.buscarTurmaPorId(contratoDTO.turmaId())
        .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + contratoDTO.turmaId()));
```

**Funcionalidade:**
- Busca entidades completas (não apenas IDs)
- Lança exceção se não encontrado
- Necessário para estabelecer relacionamentos JPA

#### 4. **Conversão DTO → Entity**
```java
Contrato contrato = contratoMapper.toEntity(contratoDTO);
contrato.setAluno(aluno);
contrato.setTurma(turma);
```

**Mapper (`ContratoMapper`):**
```java
@Mapping(target = "aluno", ignore = true)
@Mapping(target = "turma", ignore = true)
@Mapping(target = "dataCriacao", ignore = true)
@Mapping(target = "dataAtualizacao", ignore = true)
Contrato toEntity(ContratoDTO contratoDTO);
```

**Observações:**
- Mapper ignora `aluno` e `turma` (setados manualmente)
- Mapper ignora timestamps (gerenciados automaticamente)

#### 5. **Geração de Número de Contrato**
```java
contrato.setNumeroContrato(gerarNumeroContrato());
```

**Método `gerarNumeroContrato()`** (linhas 460-468):
```java
private String gerarNumeroContrato() {
    String ano = String.valueOf(LocalDate.now().getYear());
    String mes = String.format("%02d", LocalDate.now().getMonthValue());
    
    // Contar contratos do mês atual
    long count = contratoRepository.count() + 1;
    
    return String.format("CTR%s%s%04d", ano, mes, count);
}
```

**Formato:** `CTRYYYYMM####`
- **Exemplo:** `CTR2024120001`
- **Prefix:** `CTR` (fixo)
- **Ano:** 4 dígitos (ex: 2024)
- **Mês:** 2 dígitos (ex: 12)
- **Sequencial:** 4 dígitos (baseado em `count()` + 1)

**⚠️ Problema Potencial:**
- Usa `count()` de todos os contratos, não apenas do mês atual
- Pode gerar números duplicados se houver contratos de meses anteriores
- **Recomendação:** Filtrar por mês/ano na query

#### 6. **Cálculo de Valor Total**
```java
if (contrato.getValorTotalContrato() == null || contrato.getValorTotalContrato().compareTo(BigDecimal.ZERO) == 0) {
    contrato.setValorTotalContrato(calcularValorTotalContrato(contrato));
}
```

**Método `calcularValorTotalContrato()`** (linhas 473-491):
```java
private BigDecimal calcularValorTotalContrato(Contrato contrato) {
    BigDecimal valorBase = BigDecimal.ZERO;
    
    if (contrato.getValorMatricula() != null) {
        valorBase = valorBase.add(contrato.getValorMatricula());
    }
    
    if (contrato.getValorMensalidade() != null 
            && contrato.getNumeroParcelas() != null 
            && contrato.getNumeroParcelas() > 0) {
        BigDecimal valorParcelas = contrato.getValorMensalidade()
                .multiply(BigDecimal.valueOf(contrato.getNumeroParcelas()));
        valorBase = valorBase.add(valorParcelas);
    }
    
    // Aplicar desconto
    if (contrato.getDescontoValor() != null 
            && contrato.getDescontoValor().compareTo(BigDecimal.ZERO) > 0) {
        valorBase = valorBase.subtract(contrato.getDescontoValor());
    }
    
    return valorBase.max(BigDecimal.ZERO);
}
```

**Fórmula:**
```
Valor Total = (Valor Matrícula + (Valor Mensalidade × Número de Parcelas)) - Desconto Valor
Valor Total = max(Valor Total, 0)
```

**Observações:**
- Desconto percentual não é aplicado automaticamente
- Valor mínimo garantido: R$ 0,00

#### 7. **Persistência no Banco**
```java
Contrato contratoSalvo = contratoRepository.save(contrato);
logger.info("Contrato criado com sucesso. ID: {}, Número: {}", 
           contratoSalvo.getId(), contratoSalvo.getNumeroContrato());
```

**Operações:**
- `@Transactional` garante atomicidade
- Entity é persistida e recebe ID gerado
- Logging de sucesso

#### 8. **Geração Automática de Parcelas**
```java
if (contratoSalvo.getValorMensalidade() != null 
        && contratoSalvo.getValorMensalidade().compareTo(BigDecimal.ZERO) > 0
        && contratoSalvo.getNumeroParcelas() != null 
        && contratoSalvo.getNumeroParcelas() > 0) {
    gerarParcelasAutomaticamente(contratoSalvo);
}
```

**Condições:**
- ✅ `valorMensalidade > 0`
- ✅ `numeroParcelas > 0`

**Método `gerarParcelasAutomaticamente()`** (linhas 497-535):

```java
private void gerarParcelasAutomaticamente(Contrato contrato) {
    // Verificar se já existem parcelas
    List<Parcela> parcelasExistentes = parcelaRepository.findByContratoId(contrato.getId());
    if (!parcelasExistentes.isEmpty()) {
        logger.info("Contrato ID {} já possui {} parcelas. Não serão geradas novas parcelas.", 
                   contrato.getId(), parcelasExistentes.size());
        return;
    }

    // Calcular data de início (primeiro vencimento um mês após a data do contrato)
    LocalDate dataVencimento = contrato.getDataContrato().plusMonths(1);
    
    List<Parcela> parcelas = new ArrayList<>();
    
    for (int i = 1; i <= contrato.getNumeroParcelas(); i++) {
        Parcela parcela = new Parcela();
        parcela.setContrato(contrato);
        parcela.setNumeroParcela(i);
        parcela.setValorParcela(contrato.getValorMensalidade());
        parcela.setDataVencimento(dataVencimento);
        parcela.setStatusParcela(StatusParcela.PENDENTE);
        
        parcelas.add(parcela);
        
        // Próximo vencimento: um mês após o anterior
        dataVencimento = dataVencimento.plusMonths(1);
    }
    
    parcelaRepository.saveAll(parcelas);
    logger.info("Geradas {} parcelas automaticamente para contrato ID {}", 
               parcelas.size(), contrato.getId());
}
```

**Características:**
- Primeiro vencimento: `dataContrato + 1 mês`
- Intervalo: mensal (cada parcela = 1 mês após anterior)
- Valor: `valorMensalidade` (todas as parcelas têm mesmo valor)
- Status inicial: `PENDENTE`
- Numeração: sequencial (1, 2, 3, ...)
- Não gera se já existirem parcelas

**Exemplo:**
```
Contrato criado em: 2024-01-15
Número de parcelas: 3
Valor mensalidade: R$ 500,00

Parcela 1: Vencimento 2024-02-15, Valor R$ 500,00, Status PENDENTE
Parcela 2: Vencimento 2024-03-15, Valor R$ 500,00, Status PENDENTE
Parcela 3: Vencimento 2024-04-15, Valor R$ 500,00, Status PENDENTE
```

#### 9. **Criação de Receita no Dashboard Financeiro**
```java
criarReceitaTotalNoDashboard(contratoSalvo);
```

**Método `criarReceitaTotalNoDashboard()`** (linhas 541-586):

```java
private void criarReceitaTotalNoDashboard(Contrato contrato) {
    // Verificar se já existe receita total para este contrato
    List<Financeiro> receitasExistentes = financeiroRepository.findByContratoId(contrato.getId())
            .stream()
            .filter(f -> f.getTipoMovimento() == TipoMovimentoFinanceiro.RECEITA 
                    && f.getParcela() == null) // Receita total não tem parcela vinculada
            .toList();
    
    if (!receitasExistentes.isEmpty()) {
        logger.info("Contrato ID {} já possui receita total no dashboard. Valor: R$ {}", 
                   contrato.getId(), receitasExistentes.get(0).getValor());
        return;
    }

    // Criar movimento financeiro com o valor total do contrato
    if (contrato.getValorTotalContrato() != null 
            && contrato.getValorTotalContrato().compareTo(BigDecimal.ZERO) > 0) {
        
        Financeiro receitaTotal = new Financeiro();
        receitaTotal.setTipoMovimento(TipoMovimentoFinanceiro.RECEITA);
        receitaTotal.setValor(contrato.getValorTotalContrato());
        receitaTotal.setDataMovimento(contrato.getDataContrato() != null ? 
                                    contrato.getDataContrato() : LocalDate.now());
        receitaTotal.setDescricao("Receita Total - Contrato " + contrato.getNumeroContrato());
        receitaTotal.setCategoria(CategoriaFinanceira.MENSALIDADE);
        receitaTotal.setContrato(contrato);
        receitaTotal.setAluno(contrato.getAluno());
        receitaTotal.setReferencia("CONTRATO-" + contrato.getNumeroContrato());
        receitaTotal.setNumeroDocumento("CTR-" + contrato.getId());
        receitaTotal.setObservacoes("Receita total do contrato gerada automaticamente");
        receitaTotal.setConfirmado(false);
        receitaTotal.setDataCriacao(LocalDateTime.now());

        financeiroRepository.save(receitaTotal);
        
        logger.info("Receita total criada no dashboard para contrato ID {} - Valor: R$ {}", 
                   contrato.getId(), contrato.getValorTotalContrato());
    }
}
```

**Funcionalidade:**
- Cria registro de receita total no dashboard financeiro
- Sincroniza valor total do contrato com módulo financeiro
- Não cria se já existir receita total para o contrato
- Não cria se `valorTotalContrato <= 0`

#### 10. **Retorno**
```java
return contratoMapper.toDto(contratoSalvo);
```

- Converte Entity → DTO
- Retorna DTO para o Controller
- Controller redireciona para lista de contratos

---

## 📊 Fluxo Completo Visualizado

```
┌─────────────────────────────────────────────────────────────────┐
│ 1. GET /contratos/novo                                          │
│    └─> Controller: novoContratoForm()                           │
│        ├─> Cria ContratoDTO.createNew()                         │
│        ├─> Carrega alunos (convertAlunosToDTO)                  │
│        ├─> Carrega turmas (listarTodasTurmas)                   │
│        ├─> Build alunosComTurmas                                │
│        └─> Build alunoTurmasMap                                 │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 2. Renderiza Template: contrato-form.html                       │
│    ├─> Formulário com todos os campos                           │
│    ├─> JavaScript: Cálculo automático de valor total            │
│    └─> JavaScript: Sincronização Aluno ↔ Turma                  │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 3. POST /contratos (Submit do formulário)                       │
│    └─> Controller: criarContrato()                              │
│        ├─> Validação Bean Validation (@Valid)                   │
│        │   └─> Se erros: retorna ao formulário                  │
│        └─> Chama Service: criarContrato()                       │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 4. Service: ContratoService.criarContrato()                     │
│    ├─> validarCriacaoContrato()                                 │
│    │   ├─> Valida datas (fim > início)                          │
│    │   └─> Valida existência de turma                           │
│    ├─> Busca Aluno e Turma (entidades completas)                │
│    ├─> Converte DTO → Entity (ContratoMapper)                   │
│    ├─> Vincula Aluno e Turma                                    │
│    ├─> Gera número de contrato (CTRYYYYMM####)                  │
│    ├─> Calcula valor total                                      │
│    ├─> Salva contrato (contratoRepository.save)                 │
│    ├─> gerarParcelasAutomaticamente()                           │
│    │   └─> Cria N parcelas (status PENDENTE)                    │
│    ├─> criarReceitaTotalNoDashboard()                           │
│    │   └─> Cria registro Financeiro (RECEITA)                   │
│    └─> Retorna ContratoDTO                                      │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│ 5. Controller: redirect:/contratos?success=...                  │
│    └─> Redireciona para lista de contratos                      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🔍 Pontos de Atenção e Melhorias

### 1. **Geração de Número de Contrato**

**Problema Atual:**
```java
long count = contratoRepository.count() + 1;
```

**Problemas:**
- Conta TODOS os contratos, não apenas do mês atual
- Pode gerar números duplicados se houver contratos de meses anteriores
- Não é thread-safe (race condition em criação simultânea)

**Solução Recomendada:**
```java
private String gerarNumeroContrato() {
    LocalDate hoje = LocalDate.now();
    String ano = String.valueOf(hoje.getYear());
    String mes = String.format("%02d", hoje.getMonthValue());
    
    // Contar apenas contratos do mês atual
    LocalDate inicioMes = hoje.withDayOfMonth(1);
    LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());
    
    long count = contratoRepository.countByDataCriacaoBetween(
        inicioMes.atStartOfDay(), 
        fimMes.atTime(23, 59, 59)
    ) + 1;
    
    return String.format("CTR%s%s%04d", ano, mes, count);
}
```

**Requer adicionar no Repository:**
```java
long countByDataCriacaoBetween(LocalDateTime inicio, LocalDateTime fim);
```

### 2. **Validação de Duplicatas**

**Status Atual:**
- ✅ Validação de duplicatas foi REMOVIDA
- ✅ Permite múltiplos contratos ativos para mesmo aluno/turma

**Considerações:**
- Se for regra de negócio, está correto
- Caso contrário, pode causar inconsistências
- Recomendação: documentar decisão de negócio

### 3. **Validação de Turma Fechada**

**Status Atual:**
- ✅ Validação de turma fechada foi REMOVIDA
- ✅ Permite criar contrato para turma fechada

**Considerações:**
- Verificar se é comportamento desejado
- Pode ser necessário para contratos retroativos
- Recomendação: documentar decisão de negócio

### 4. **Cálculo de Valor Total**

**Problema:**
- Desconto percentual não é aplicado automaticamente
- Precisa ser calculado manualmente antes de enviar

**Solução Recomendada:**
```java
private BigDecimal calcularValorTotalContrato(Contrato contrato) {
    BigDecimal valorBase = BigDecimal.ZERO;
    
    if (contrato.getValorMatricula() != null) {
        valorBase = valorBase.add(contrato.getValorMatricula());
    }
    
    if (contrato.getValorMensalidade() != null 
            && contrato.getNumeroParcelas() != null 
            && contrato.getNumeroParcelas() > 0) {
        BigDecimal valorParcelas = contrato.getValorMensalidade()
                .multiply(BigDecimal.valueOf(contrato.getNumeroParcelas()));
        valorBase = valorBase.add(valorParcelas);
    }
    
    // Aplicar desconto percentual primeiro
    if (contrato.getDescontoPercentual() != null 
            && contrato.getDescontoPercentual().compareTo(BigDecimal.ZERO) > 0) {
        BigDecimal descontoPercentual = valorBase
                .multiply(contrato.getDescontoPercentual())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        valorBase = valorBase.subtract(descontoPercentual);
    }
    
    // Aplicar desconto em valor depois
    if (contrato.getDescontoValor() != null 
            && contrato.getDescontoValor().compareTo(BigDecimal.ZERO) > 0) {
        valorBase = valorBase.subtract(contrato.getDescontoValor());
    }
    
    return valorBase.max(BigDecimal.ZERO);
}
```

### 5. **Performance**

**Otimizações Possíveis:**
- Carregar alunos e turmas de forma lazy/paginada (se houver muitos)
- Cache de lista de alunos/turmas
- Busca otimizada no `buildAlunoTurmasMap()` (evitar carregar todos os relacionamentos)

### 6. **Validação JavaScript vs Backend**

**Atual:**
- Validação de datas apenas no JavaScript
- Fórmula de cálculo no JavaScript pode divergir do backend

**Recomendação:**
- Validar datas também no backend
- Garantir que fórmula JavaScript seja idêntica ao backend
- Considerar endpoint REST para cálculo em tempo real

---

## ✅ Checklist de Qualidade

### Validações
- [x] Bean Validation implementado
- [x] Validações de negócio no Service
- [x] Validação client-side (JavaScript)
- [x] Feedback visual de erros
- [ ] Validação de datas no backend (apenas JavaScript)
- [ ] Fórmula de cálculo sincronizada (JavaScript e backend divergem)

### Segurança
- [x] CSRF Token incluído
- [x] Validação de autenticação (via Spring Security)
- [x] Validação de autorização (roles)
- [x] Sanitização de dados (via Bean Validation)

### Funcionalidades
- [x] Geração automática de número de contrato
- [x] Cálculo automático de valor total
- [x] Geração automática de parcelas
- [x] Criação automática de receita financeira
- [x] Sincronização Aluno ↔ Turma
- [ ] Desconto percentual não aplicado automaticamente

### Performance
- [x] Transações gerenciadas (@Transactional)
- [ ] Cache de listas (não implementado)
- [ ] Paginação (não implementada)
- [x] Lazy loading em relacionamentos

### Código
- [x] Logging adequado
- [x] Tratamento de exceções
- [x] Código limpo e organizado
- [x] Comentários onde necessário
- [ ] Thread-safety na geração de número

---

## 📝 Conclusão

O fluxo de criação de contratos está bem estruturado e funcional, com:

**Pontos Fortes:**
- ✅ Separação clara de responsabilidades (Controller → Service → Repository)
- ✅ Validações em múltiplas camadas
- ✅ Funcionalidades automáticas (parcelas, receita)
- ✅ Interface amigável com validação em tempo real
- ✅ Sincronização inteligente Aluno ↔ Turma

**Áreas de Melhoria:**
- ⚠️ Geração de número de contrato pode ter problemas de concorrência
- ⚠️ Desconto percentual não é aplicado automaticamente
- ⚠️ Validação de datas apenas no JavaScript
- ⚠️ Fórmula de cálculo pode divergir entre frontend e backend
- ⚠️ Regras de negócio removidas (duplicatas, turma fechada) podem precisar revisão

O código está pronto para produção após as correções recomendadas.

---

**Última Atualização:** 2025-01-XX  
**Versão do Documento:** 1.0  
**Status:** ✅ Completo
