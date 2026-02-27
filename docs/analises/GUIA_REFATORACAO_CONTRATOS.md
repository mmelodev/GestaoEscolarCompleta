# 🚀 Guia Completo - Refatoração Módulo Contratos AriranG Plataforma

## 📋 ÍNDICE

1. [Limpeza de Dados Existentes](#1-limpeza-de-dados-existentes)
2. [Modificar Regras de Negócio](#2-modificar-regras-de-negócio)
3. [Diagnóstico de Endpoints](#3-diagnóstico-de-endpoints)
4. [Implementar Templates PDF Customizáveis](#4-implementar-templates-pdf-customizáveis)
5. [Testes Finais](#5-testes-finais)
6. [Como Enviar Templates do Cliente](#6-como-enviar-templates-do-cliente)

---

## 1. LIMPEZA DE DADOS EXISTENTES

### 1.1 Verificar Contratos Existentes

Execute no MySQL ou H2:

```sql
-- Ver todos os contratos
SELECT * FROM contratos;

-- Contar total
SELECT COUNT(*) as total_contratos FROM contratos;

-- Ver contratos com informações relacionadas
SELECT 
    c.id,
    c.numero_contrato,
    c.data_contrato,
    a.nome_completo as aluno_nome,
    t.nome_turma as turma_nome,
    c.situacao_contrato,
    c.valor_total_contrato
FROM contratos c 
LEFT JOIN alunos a ON c.aluno_id = a.id 
LEFT JOIN turmas t ON c.turma_id = t.id
ORDER BY c.data_criacao DESC;
```

### 1.2 Verificar Dependências

```sql
-- Verificar parcelas vinculadas
SELECT COUNT(*) as total_parcelas FROM parcelas WHERE contrato_id IS NOT NULL;

SELECT 
    p.id,
    p.numero_parcela,
    p.valor_parcela,
    p.status_parcela,
    c.numero_contrato
FROM parcelas p
JOIN contratos c ON p.contrato_id = c.id;

-- Verificar pagamentos vinculados
SELECT COUNT(*) as total_pagamentos FROM pagamentos WHERE parcela_id IS NOT NULL;

SELECT 
    pg.id,
    pg.valor_pago,
    pg.data_pagamento,
    p.numero_parcela,
    c.numero_contrato
FROM pagamentos pg
JOIN parcelas p ON pg.parcela_id = p.id
JOIN contratos c ON p.contrato_id = c.id;
```

### 1.3 Script de Limpeza

**Criar arquivo:** `Arirang-plataforma/src/main/resources/cleanup-contratos.sql`

```sql
-- ⚠️ ATENÇÃO: Este script deleta TODOS os dados relacionados a contratos
-- Faça backup antes de executar!

-- Desabilitar verificação de chaves estrangeiras temporariamente
SET FOREIGN_KEY_CHECKS = 0;

-- Deletar em ordem (respeitando dependências)
DELETE FROM pagamentos WHERE parcela_id IN (SELECT id FROM parcelas);
DELETE FROM comprovantes_pagamento WHERE parcela_id IN (SELECT id FROM parcelas);
DELETE FROM receitas WHERE parcela_id IN (SELECT id FROM parcelas);
DELETE FROM financeiro WHERE parcela_id IN (SELECT id FROM parcelas);

DELETE FROM parcelas WHERE contrato_id IS NOT NULL;
DELETE FROM financeiro WHERE contrato_id IS NOT NULL;
DELETE FROM comprovantes_pagamento WHERE contrato_id IS NOT NULL;

DELETE FROM contratos;

-- Reabilitar verificação de chaves estrangeiras
SET FOREIGN_KEY_CHECKS = 1;

-- Verificar limpeza
SELECT COUNT(*) as contratos_restantes FROM contratos;
SELECT COUNT(*) as parcelas_restantes FROM parcelas;
SELECT COUNT(*) as pagamentos_restantes FROM pagamentos;

COMMIT;
```

**Como executar:**
```bash
# MySQL
mysql -u root -p arirang_db < src/main/resources/cleanup-contratos.sql

# Ou via Spring Boot (criar endpoint temporário)
```

### 1.4 Endpoint Temporário para Limpeza (Opcional)

**Criar em:** `ContratoController.java`

```java
/**
 * ⚠️ ENDPOINT TEMPORÁRIO - REMOVER EM PRODUÇÃO
 * Limpa todos os contratos e dependências
 */
@PostMapping("/admin/limpar-todos")
@Secured("ROLE_ADMIN") // Proteger com role admin
public String limparTodosContratos(RedirectAttributes ra) {
    try {
        // Deletar pagamentos
        pagamentoRepository.deleteAll();
        
        // Deletar parcelas
        parcelaRepository.deleteAll();
        
        // Deletar contratos
        contratoRepository.deleteAll();
        
        ra.addFlashAttribute("success", "Todos os contratos foram deletados!");
        return "redirect:/contratos";
    } catch (Exception e) {
        logger.error("Erro ao limpar contratos: ", e);
        ra.addFlashAttribute("error", "Erro ao limpar: " + e.getMessage());
        return "redirect:/contratos";
    }
}
```

---

## 2. MODIFICAR REGRAS DE NEGÓCIO

### 2.1 Alterar ContratoService.java

**Arquivo:** `Arirang-plataforma/src/main/java/br/com/arirang/plataforma/service/ContratoService.java`

#### 2.1.1 Modificar método `deletarContrato()`

```java
/**
 * Deleta um contrato (SEM validar pagamentos/parcelas)
 */
public void deletarContrato(Long id) {
    logger.debug("Deletando contrato ID: {}", id);

    Contrato contrato = contratoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + id));

    // ✅ NOVA REGRA: Não validar pagamentos/parcelas
    // Removida validação de contratos cancelados
    // Removida validação de parcelas existentes
    
    // Deletar parcelas relacionadas primeiro (cascade)
    List<Parcela> parcelas = parcelaRepository.findByContratoId(id);
    if (!parcelas.isEmpty()) {
        logger.info("Deletando {} parcelas do contrato ID {}", parcelas.size(), id);
        parcelaRepository.deleteAll(parcelas);
    }

    contratoRepository.delete(contrato);
    logger.info("Contrato deletado com sucesso. ID: {}", id);
}
```

#### 2.1.2 Modificar método `validarCriacaoContrato()`

```java
/**
 * Validações para criação de contrato (SEM validar duplicatas)
 */
private void validarCriacaoContrato(ContratoDTO contratoDTO) {
    // ✅ NOVA REGRA: Permitir múltiplos contratos ativos para mesmo aluno/turma
    // Removida validação: existsByAlunoIdAndTurmaIdAndSituacaoContrato
    
    // Validar datas
    if (contratoDTO.dataFimVigencia().isBefore(contratoDTO.dataInicioVigencia())) {
        throw new BusinessException("Data de fim deve ser posterior à data de início");
    }

    // Validar se a turma existe (mas não precisa estar ativa)
    Turma turma = turmaService.buscarTurmaPorId(contratoDTO.turmaId())
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
    
    // ✅ NOVA REGRA: Permitir criar contrato mesmo para turma fechada
    // Removida validação: if ("FECHADA".equals(turma.getSituacaoTurma()))
}
```

#### 2.1.3 Adicionar método para criação sem validação (opcional)

```java
/**
 * Cria contrato sem validações de duplicatas (para casos especiais)
 */
@Transactional
public ContratoDTO criarContratoSemValidacao(ContratoDTO contratoDTO) {
    logger.debug("Criando contrato sem validações para aluno ID: {} e turma ID: {}", 
                contratoDTO.alunoId(), contratoDTO.turmaId());

    // Buscar aluno e turma
    Aluno aluno = alunoService.buscarAlunoPorId(contratoDTO.alunoId())
            .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + contratoDTO.alunoId()));

    Turma turma = turmaService.buscarTurmaPorId(contratoDTO.turmaId())
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + contratoDTO.turmaId()));

    // Criar entidade
    Contrato contrato = contratoMapper.toEntity(contratoDTO);
    contrato.setAluno(aluno);
    contrato.setTurma(turma);
    
    // Gerar número do contrato
    contrato.setNumeroContrato(gerarNumeroContrato());
    
    // Calcular valor total se não informado
    if (contrato.getValorTotalContrato() == null || contrato.getValorTotalContrato().compareTo(BigDecimal.ZERO) == 0) {
        contrato.setValorTotalContrato(calcularValorTotalContrato(contrato));
    }

    // Salvar contrato
    Contrato contratoSalvo = contratoRepository.save(contrato);
    logger.info("Contrato criado sem validações. ID: {}, Número: {}", 
               contratoSalvo.getId(), contratoSalvo.getNumeroContrato());

    // Gerar parcelas automaticamente
    if (contratoSalvo.getValorMensalidade() != null 
            && contratoSalvo.getValorMensalidade().compareTo(BigDecimal.ZERO) > 0
            && contratoSalvo.getNumeroParcelas() != null 
            && contratoSalvo.getNumeroParcelas() > 0) {
        gerarParcelasAutomaticamente(contratoSalvo);
    }

    return contratoMapper.toDto(contratoSalvo);
}
```

### 2.2 Controller - Novo Endpoint (Opcional)

**Adicionar em:** `ContratoController.java`

```java
/**
 * Cria contrato sem validações (bypass)
 */
@PostMapping("/criar-sem-validacao")
public String criarSemValidacao(@Valid @ModelAttribute("contrato") ContratoDTO contrato,
                               BindingResult bindingResult, 
                               Model model,
                               RedirectAttributes ra) {
    if (bindingResult.hasErrors()) {
        model.addAttribute("isNew", true);
        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
        model.addAttribute("turmas", turmaService.listarTodasTurmas());
        return "contrato-form";
    }
    
    try {
        contratoService.criarContratoSemValidacao(contrato);
        ra.addFlashAttribute("success", "Contrato criado sem validações!");
        return "redirect:/contratos";
    } catch (Exception e) {
        logger.error("Erro ao criar contrato sem validação: ", e);
        model.addAttribute("error", "Erro ao criar contrato: " + e.getMessage());
        model.addAttribute("isNew", true);
        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
        model.addAttribute("turmas", turmaService.listarTodasTurmas());
        return "contrato-form";
    }
}
```

---

## 3. DIAGNÓSTICO DE ENDPOINTS

### 3.1 Checklist de Verificação

Execute este checklist para cada endpoint:

```markdown
□ [ ] Controller tem @Autowired ContratoService
□ [ ] Service tem @Autowired ContratoRepository  
□ [ ] Repository extends JpaRepository<Contrato, Long>
□ [ ] Templates existem:
    □ [ ] contratos.html
    □ [ ] contrato-view.html
    □ [ ] contrato-form.html
    □ [ ] contrato-pdf.html
    □ [ ] contrato-delete.html
□ [ ] CSS carregado em templates (alunos.css, header.css)
□ [ ] ExceptionHandler configurado (GlobalExceptionHandler)
□ [ ] Mapper configurado (ContratoMapper)
□ [ ] DTOs com validações corretas
```

### 3.2 Testar Endpoints Manualmente

#### 3.2.1 Via Navegador

```
GET  http://localhost:8080/contratos
GET  http://localhost:8080/contratos/novo
GET  http://localhost:8080/contratos/visualizar/1
GET  http://localhost:8080/contratos/editar/1  
GET  http://localhost:8080/contratos/pdf/1
GET  http://localhost:8080/contratos/deletar/1
```

#### 3.2.2 Via cURL (Terminal)

```bash
# Listar contratos
curl -X GET http://localhost:8080/contratos

# Visualizar contrato
curl -X GET http://localhost:8080/contratos/visualizar/1

# Criar contrato (POST)
curl -X POST http://localhost:8080/contratos \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "alunoId=1&turmaId=1&dataContrato=2024-12-01"
```

### 3.3 Verificar Logs

```bash
# Terminal do Spring Boot
tail -f logs/plataforma.log | grep -i contrato

# Ou no Windows PowerShell
Get-Content logs\plataforma.log -Wait | Select-String -Pattern "contrato"
```

### 3.4 Erros Comuns e Soluções

| Erro | Causa | Solução |
|------|-------|---------|
| `404 Not Found` | Template não existe | Criar template em `templates/` |
| `500 Internal Server Error` | NullPointerException | Verificar se entidades estão carregadas |
| `TemplateProcessingException` | Erro no Thymeleaf | Verificar sintaxe das expressões |
| `LazyInitializationException` | Relacionamento não carregado | Usar `@Transactional` ou fetch join |
| `ValidationException` | DTO inválido | Verificar validações Bean Validation |

### 3.5 Script de Teste Automatizado

**Criar arquivo:** `Arirang-plataforma/src/test/java/br/com/arirang/plataforma/controller/ContratoControllerTest.java`

```java
@SpringBootTest
@AutoConfigureMockMvc
class ContratoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testListarContratos() throws Exception {
        mockMvc.perform(get("/contratos"))
                .andExpect(status().isOk())
                .andExpect(view().name("contratos"));
    }

    @Test
    void testVisualizarContrato() throws Exception {
        mockMvc.perform(get("/contratos/visualizar/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("contrato-view"));
    }
}
```

---

## 4. IMPLEMENTAR TEMPLATES PDF CUSTOMIZÁVEIS

### 4.1 Estrutura de Diretórios

**Criar estrutura:**

```
Arirang-plataforma/src/main/resources/templates/contratos/pdf/
├── contrato-servicos-menor.html
├── contrato-curso.html  
├── uso-imagem-menor.html
└── uso-imagem-adulto.html
```

### 4.2 Service para Geração de PDF

**Criar arquivo:** `Arirang-plataforma/src/main/java/br/com/arirang/plataforma/service/ContratoPdfService.java`

```java
package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.ContratoDTO;
import br.com.arirang.plataforma.entity.Contrato;
import br.com.arirang.plataforma.mapper.ContratoMapper;
import br.com.arirang.plataforma.repository.ContratoRepository;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Service
public class ContratoPdfService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoPdfService.class);

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ContratoMapper contratoMapper;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * Gera PDF do contrato usando template customizado
     */
    public byte[] gerarPdf(Long contratoId, String templateName) {
        try {
            Contrato contrato = contratoRepository.findById(contratoId)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));

            ContratoDTO contratoDTO = contratoMapper.toDto(contrato);

            // Preparar contexto Thymeleaf
            Context context = new Context();
            context.setVariable("contrato", contratoDTO);
            context.setVariable("aluno", contrato.getAluno());
            context.setVariable("turma", contrato.getTurma());
            if (contrato.getAluno().getResponsavel() != null) {
                context.setVariable("responsavel", contrato.getAluno().getResponsavel());
            }

            // Renderizar template HTML
            String html = templateEngine.process("contratos/pdf/" + templateName, context);

            // Converter HTML para PDF
            return htmlToPdf(html);

        } catch (Exception e) {
            logger.error("Erro ao gerar PDF do contrato ID {}: ", contratoId, e);
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Converte HTML para PDF usando OpenPDF
     */
    private byte[] htmlToPdf(String html) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        Document document = new Document();
        PdfWriter.getInstance(document, outputStream);
        document.open();
        
        // Aqui você pode usar uma biblioteca como Flying Saucer ou OpenPDF
        // Por enquanto, retornando HTML como exemplo
        // TODO: Implementar conversão HTML para PDF
        
        document.close();
        return outputStream.toByteArray();
    }
}
```

### 4.3 Controller Atualizado

**Modificar:** `ContratoController.java`

```java
@Autowired
private ContratoPdfService contratoPdfService;

/**
 * Gera PDF do contrato com template customizado
 */
@GetMapping("/pdf/{id}/{template}")
public void gerarPdfCustom(
        @PathVariable Long id,
        @PathVariable String template,
        HttpServletResponse response) {
    try {
        // Validar template
        List<String> templatesValidos = Arrays.asList(
            "contrato-servicos-menor",
            "contrato-curso",
            "uso-imagem-menor",
            "uso-imagem-adulto"
        );
        
        if (!templatesValidos.contains(template)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Template inválido");
            return;
        }

        // Gerar PDF
        byte[] pdfBytes = contratoPdfService.gerarPdf(id, template + ".html");

        // Configurar resposta
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", 
            "inline; filename=contrato-" + id + "-" + template + ".pdf");
        response.setContentLength(pdfBytes.length);

        // Escrever PDF na resposta
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();

    } catch (Exception e) {
        logger.error("Erro ao gerar PDF customizado para contrato ID {}: ", id, e);
        try {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Erro ao gerar PDF: " + e.getMessage());
        } catch (IOException ex) {
            logger.error("Erro ao enviar resposta de erro: ", ex);
        }
    }
}

/**
 * Gera PDF padrão (mantém compatibilidade)
 */
@GetMapping("/pdf/{id}")
public String gerarPdfContrato(@PathVariable Long id, Model model) {
    try {
        ContratoDTO contrato = contratoService.buscarContratoPorId(id)
                .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
        
        model.addAttribute("contrato", contrato);
        return "contrato-pdf";
        
    } catch (Exception e) {
        logger.error("Erro ao gerar PDF do contrato ID {}: ", id, e);
        model.addAttribute("error", "Erro ao gerar PDF: " + e.getMessage());
        return "error";
    }
}
```

### 4.4 Template Base - contrato-servicos-menor.html

**Criar arquivo:** `templates/contratos/pdf/contrato-servicos-menor.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>CONTRATO DE SERVIÇOS - MENOR DE IDADE</title>
    <style>
        @page {
            margin: 2cm;
            size: A4;
        }
        body {
            font-family: 'Times New Roman', serif;
            font-size: 12pt;
            line-height: 1.6;
            color: #000;
        }
        .header {
            text-align: center;
            font-size: 16pt;
            font-weight: bold;
            margin-bottom: 30px;
            border-bottom: 2px solid #000;
            padding-bottom: 10px;
        }
        .section {
            margin: 20px 0;
        }
        .section-title {
            font-weight: bold;
            font-size: 13pt;
            margin-bottom: 10px;
        }
        .field {
            margin: 5px 0;
        }
        .field-label {
            font-weight: bold;
        }
        .assinatura {
            margin-top: 60px;
            text-align: center;
        }
        .linha-assinatura {
            border-top: 1px solid #000;
            width: 300px;
            margin: 0 auto;
            padding-top: 5px;
        }
        .footer {
            margin-top: 40px;
            font-size: 10pt;
            text-align: center;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="header">
        CONTRATO DE PRESTAÇÃO DE SERVIÇOS EDUCACIONAIS<br>
        MENOR DE IDADE<br>
        Nº <span th:text="${contrato.numeroContrato}">CTR2024120001</span>
    </div>

    <div class="section">
        <div class="section-title">CONTRATANTE (RESPONSÁVEL LEGAL):</div>
        <div class="field">
            <span class="field-label">Nome:</span> 
            <span th:text="${responsavel != null ? responsavel.nomeCompleto : 'Não informado'}">Nome do Responsável</span>
        </div>
        <div class="field">
            <span class="field-label">CPF:</span> 
            <span th:text="${responsavel != null ? responsavel.cpf : 'Não informado'}">000.000.000-00</span>
        </div>
        <div class="field">
            <span class="field-label">RG:</span> 
            <span th:text="${responsavel != null ? responsavel.rg : 'Não informado'}">00.000.000-0</span>
        </div>
        <div class="field">
            <span class="field-label">Telefone:</span> 
            <span th:text="${responsavel != null ? responsavel.telefone : 'Não informado'}">(00) 00000-0000</span>
        </div>
        <div class="field">
            <span class="field-label">E-mail:</span> 
            <span th:text="${responsavel != null ? responsavel.email : 'Não informado'}">email@exemplo.com</span>
        </div>
    </div>

    <div class="section">
        <div class="section-title">ALUNO (MENOR DE IDADE):</div>
        <div class="field">
            <span class="field-label">Nome:</span> 
            <span th:text="${aluno.nomeCompleto}">Nome do Aluno</span>
        </div>
        <div class="field">
            <span class="field-label">Data de Nascimento:</span> 
            <span th:text="${#temporals.format(aluno.dataNascimento, 'dd/MM/yyyy')}">01/01/2010</span>
        </div>
        <div class="field">
            <span class="field-label">CPF:</span> 
            <span th:text="${aluno.cpf}">000.000.000-00</span>
        </div>
    </div>

    <div class="section">
        <div class="section-title">SERVIÇO CONTRATADO:</div>
        <div class="field">
            <span class="field-label">Turma:</span> 
            <span th:text="${turma.nomeTurma}">Nome da Turma</span>
        </div>
        <div class="field">
            <span class="field-label">Nível:</span> 
            <span th:text="${turma.nivelProficiencia}">Básico</span>
        </div>
        <div class="field">
            <span class="field-label">Período de Vigência:</span> 
            <span th:text="${#temporals.format(contrato.dataInicioVigencia, 'dd/MM/yyyy')}">01/01/2024</span>
            até 
            <span th:text="${#temporals.format(contrato.dataFimVigencia, 'dd/MM/yyyy')}">31/12/2024</span>
        </div>
    </div>

    <div class="section">
        <div class="section-title">VALORES:</div>
        <div class="field">
            <span class="field-label">Valor da Matrícula:</span> 
            R$ <span th:text="${#numbers.formatDecimal(contrato.valorMatricula, 2, 2)}">0,00</span>
        </div>
        <div class="field">
            <span class="field-label">Valor da Mensalidade:</span> 
            R$ <span th:text="${#numbers.formatDecimal(contrato.valorMensalidade, 2, 2)}">0,00</span>
        </div>
        <div class="field">
            <span class="field-label">Número de Parcelas:</span> 
            <span th:text="${contrato.numeroParcelas}">0</span>
        </div>
        <div class="field">
            <span class="field-label">Valor Total do Contrato:</span> 
            <strong>R$ <span th:text="${#numbers.formatDecimal(contrato.valorTotalContrato, 2, 2)}">0,00</span></strong>
        </div>
    </div>

    <div class="section">
        <div class="section-title">CLÁUSULAS:</div>
        <p>1. O CONTRATANTE se compromete a pagar as mensalidades em dia, conforme parcelas acordadas.</p>
        <p>2. O CONTRATADO se compromete a fornecer os serviços educacionais conforme descrito.</p>
        <p>3. Em caso de cancelamento, será aplicada a política de cancelamento conforme regulamento interno.</p>
    </div>

    <div class="assinatura">
        <div class="linha-assinatura">
            <span th:text="${responsavel != null ? responsavel.nomeCompleto : 'Nome do Responsável'}">Nome do Responsável</span><br>
            Responsável Legal
        </div>
    </div>

    <div class="footer">
        <p>Documento gerado em <span th:text="${#temporals.format(#temporals.createNow(), 'dd/MM/yyyy HH:mm')}">01/12/2024 10:00</span></p>
        <p>AriranG - Escola de Idiomas</p>
    </div>
</body>
</html>
```

### 4.5 Atualizar Template contratos.html

**Adicionar botões na tabela de ações:**

```html
<!-- Na coluna de ações da tabela -->
<td class="actions">
    <a th:href="@{'/contratos/visualizar/' + ${contrato.id}}" 
       class="btn btn-sm btn-info" title="Visualizar">👁️</a>
    <a th:href="@{'/contratos/editar/' + ${contrato.id}}" 
       class="btn btn-sm btn-warning" title="Editar">✏️</a>
    
    <!-- Dropdown para PDFs -->
    <div class="dropdown" style="display: inline-block;">
        <button class="btn btn-sm btn-success dropdown-toggle" 
                type="button" 
                data-bs-toggle="dropdown" 
                title="Gerar PDF">
            📄 PDF
        </button>
        <ul class="dropdown-menu">
            <li>
                <a class="dropdown-item" 
                   th:href="@{'/contratos/pdf/' + ${contrato.id} + '/contrato-servicos-menor'}">
                    Contrato Serviços (Menor)
                </a>
            </li>
            <li>
                <a class="dropdown-item" 
                   th:href="@{'/contratos/pdf/' + ${contrato.id} + '/contrato-curso'}">
                    Contrato Curso
                </a>
            </li>
            <li>
                <a class="dropdown-item" 
                   th:href="@{'/contratos/pdf/' + ${contrato.id} + '/uso-imagem-menor'}">
                    Autorização Uso Imagem (Menor)
                </a>
            </li>
            <li>
                <a class="dropdown-item" 
                   th:href="@{'/contratos/pdf/' + ${contrato.id} + '/uso-imagem-adulto'}">
                    Autorização Uso Imagem (Adulto)
                </a>
            </li>
            <li><hr class="dropdown-divider"></li>
            <li>
                <a class="dropdown-item" 
                   th:href="@{'/contratos/pdf/' + ${contrato.id}}">
                    PDF Padrão
                </a>
            </li>
        </ul>
    </div>
    
    <a th:href="@{'/contratos/deletar/' + ${contrato.id}}" 
       class="btn btn-sm btn-danger" title="Deletar">🗑️</a>
</td>
```

---

## 5. TESTES FINAIS

### 5.1 Checklist de Testes

```markdown
## Funcionalidades Básicas
□ [ ] Listar contratos (GET /contratos)
□ [ ] Criar contrato (POST /contratos)
□ [ ] Visualizar contrato (GET /contratos/visualizar/{id})
□ [ ] Editar contrato (GET /contratos/editar/{id} + POST /contratos/atualizar/{id})
□ [ ] Deletar contrato (GET /contratos/deletar/{id} + POST /contratos/deletar/{id})

## Templates PDF
□ [ ] PDF Padrão (GET /contratos/pdf/{id})
□ [ ] PDF Contrato Serviços Menor (GET /contratos/pdf/{id}/contrato-servicos-menor)
□ [ ] PDF Contrato Curso (GET /contratos/pdf/{id}/contrato-curso)
□ [ ] PDF Uso Imagem Menor (GET /contratos/pdf/{id}/uso-imagem-menor)
□ [ ] PDF Uso Imagem Adulto (GET /contratos/pdf/{id}/uso-imagem-adulto)

## Validações
□ [ ] Criar contrato sem validar duplicatas
□ [ ] Deletar contrato sem validar pagamentos
□ [ ] Criar múltiplos contratos para mesmo aluno/turma
□ [ ] Criar contrato para turma fechada

## Performance
□ [ ] Sem N+1 queries
□ [ ] Carregamento rápido de listas
□ [ ] PDFs geram rapidamente
```

### 5.2 Script de Teste Manual

**Criar arquivo:** `test-contratos.md`

```markdown
# Teste Manual - Módulo Contratos

## 1. Criar Contrato
1. Acessar http://localhost:8080/contratos/novo
2. Selecionar aluno existente
3. Selecionar turma ativa
4. Preencher valores:
   - Valor Matrícula: R$ 200,00
   - Valor Mensalidade: R$ 300,00
   - Número de Parcelas: 6
5. Clicar "Salvar"
6. Verificar:
   - ✅ Redirecionamento para lista
   - ✅ Mensagem de sucesso
   - ✅ Contrato aparece na lista
   - ✅ Parcelas foram criadas

## 2. Visualizar Contrato
1. Clicar em "Visualizar" em um contrato
2. Verificar:
   - ✅ Todas as informações aparecem
   - ✅ Aluno e turma corretos
   - ✅ Valores corretos
   - ✅ Datas formatadas

## 3. Gerar PDFs
1. Clicar no dropdown "PDF"
2. Testar cada opção:
   - ✅ Contrato Serviços Menor
   - ✅ Contrato Curso
   - ✅ Uso Imagem Menor
   - ✅ Uso Imagem Adulto
   - ✅ PDF Padrão
3. Verificar:
   - ✅ PDF abre no navegador
   - ✅ Conteúdo correto
   - ✅ Formatação adequada

## 4. Deletar Contrato
1. Clicar em "Deletar"
2. Confirmar deleção
3. Verificar:
   - ✅ Contrato removido da lista
   - ✅ Parcelas deletadas
   - ✅ Sem erros no log
```

---

## 6. COMO ENVIAR TEMPLATES DO CLIENTE

### 6.1 Opções para Enviar Templates

#### Opção 1: Colar o Conteúdo Diretamente
Você pode colar o conteúdo HTML/texto dos templates aqui na conversa. Eu adaptarei para Thymeleaf.

#### Opção 2: Enviar Arquivos
Se você tiver os arquivos (DOC, PDF, HTML), pode:
1. Abrir os arquivos
2. Copiar o conteúdo
3. Colar aqui na conversa
4. Eu adapto para Thymeleaf

#### Opção 3: Descrever a Estrutura
Se preferir, descreva:
- Campos que devem aparecer
- Layout desejado
- Seções do documento
- Eu crio os templates baseados na descrição

### 6.2 Formato Esperado dos Templates

Para facilitar a adaptação, envie os templates com:

```markdown
## Template: [Nome do Template]

### Campos que devem aparecer:
- Nome do aluno
- CPF do aluno
- Nome do responsável (se menor)
- CPF do responsável
- Turma
- Valores
- Datas
- etc.

### Layout:
[Descrição ou imagem do layout]

### Texto/Cláusulas:
[Texto fixo que deve aparecer]
```

### 6.3 Exemplo de Como Enviar

```
Template 1: Contrato de Serviços - Menor de Idade

Campos:
- Nome do responsável
- CPF do responsável
- Nome do aluno
- Data nascimento do aluno
- Turma
- Valor total
- Período de vigência

Layout:
- Cabeçalho centralizado com título
- Seções separadas
- Assinatura no final

Cláusulas:
[colar aqui o texto das cláusulas]
```

---

## 🚀 PRÓXIMOS PASSOS

1. **Execute a limpeza de dados** (Passo 1)
2. **Modifique as regras de negócio** (Passo 2)
3. **Teste os endpoints** (Passo 3)
4. **Envie os templates do cliente** (Passo 6)
5. **Implemente os templates PDF** (Passo 4)
6. **Execute testes finais** (Passo 5)

---

## 📝 NOTAS IMPORTANTES

1. **Backup:** Sempre faça backup antes de limpar dados:
   ```bash
   mysqldump -u root -p arirang_db > backup_antes_refatoracao.sql
   ```

2. **Produção:** Teste em homologação antes de produção

3. **Commit:** 
   ```bash
   git add .
   git commit -m "feat: refatoração módulo contratos - templates PDF custom + bypass validações"
   ```

4. **Rollback:** Mantenha o backup para rollback se necessário

---

**Última atualização:** Dezembro 2024  
**Status:** ✅ Pronto para implementação

