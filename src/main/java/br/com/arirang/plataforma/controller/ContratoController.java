package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.AlunoDTO;
import br.com.arirang.plataforma.dto.AlunoTurmaDTO;
import br.com.arirang.plataforma.dto.TurmaDTO;
import br.com.arirang.plataforma.dto.ContratoDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Contrato;
import br.com.arirang.plataforma.mapper.AlunoMapper;
import br.com.arirang.plataforma.service.AlunoService;
import br.com.arirang.plataforma.service.ContratoService;
import br.com.arirang.plataforma.service.TurmaService;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import java.util.Arrays;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/contratos")
public class ContratoController {

    private static final Logger logger = LoggerFactory.getLogger(ContratoController.class);

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private TurmaService turmaService;
    
    @Autowired
    private AlunoMapper alunoMapper;
    
    private AlunoDTO convertToDTO(Aluno aluno) {
        return alunoMapper.toDto(aluno);
    }
    
    private List<AlunoDTO> convertAlunosToDTO(List<Aluno> alunos) {
        return alunos.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Monta estrutura AlunoTurmaDTO (aluno + suas turmas) para uso no front-end
     * evitando inconsistências aluno/turma ao criar contrato.
     */
    private List<AlunoTurmaDTO> buildAlunosComTurmas() {
        List<Aluno> alunos = alunoService.listarTodosAlunos();
        return alunos.stream()
                .map(aluno -> {
                    List<TurmaDTO> turmasDTO = aluno.getTurmas() != null
                            ? aluno.getTurmas().stream()
                                    .map(turma -> TurmaDTO.simple(turma.getId(), turma.getNomeTurma(), turma.getNivelProficiencia()))
                                    .toList()
                            : java.util.List.of();
                    return new AlunoTurmaDTO(aluno.getId(), aluno.getNomeCompleto(), turmasDTO);
                })
                .toList();
    }
    
    /**
     * Mapa alunoId -> lista de turmaIds, usado no JavaScript para restringir
     * as turmas disponíveis de acordo com o aluno selecionado.
     */
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

    /**
     * Calcula valores com desconto (matrícula e mensalidade) a partir do contrato
     * e adiciona ao model para uso nos templates PDF.
     * O desconto é rateado proporcionalmente com base em valorTotalContrato.
     */
    private void adicionarValoresComDescontoAoModel(Model model, ContratoDTO c) {
        BigDecimal vMat = c.valorMatricula() != null ? c.valorMatricula() : BigDecimal.ZERO;
        BigDecimal vMen = c.valorMensalidade() != null ? c.valorMensalidade() : BigDecimal.ZERO;
        int n = (c.numeroParcelas() != null && c.numeroParcelas() > 0) ? c.numeroParcelas() : 1;
        BigDecimal totalSem = vMat.add(vMen.multiply(BigDecimal.valueOf(n)));
        BigDecimal vTotal = c.valorTotalContrato() != null ? c.valorTotalContrato() : totalSem;
        BigDecimal fator = (totalSem.compareTo(BigDecimal.ZERO) > 0)
                ? vTotal.divide(totalSem, 4, RoundingMode.HALF_UP)
                : BigDecimal.ONE;
        BigDecimal vMatDesc = vMat.multiply(fator).setScale(2, RoundingMode.HALF_UP);
        BigDecimal vMenDesc = vMen.multiply(fator).setScale(2, RoundingMode.HALF_UP);
        model.addAttribute("valorMatriculaComDesconto", vMatDesc);
        model.addAttribute("valorMensalidadeComDesconto", vMenDesc);
    }

    /**
     * Lista todos os contratos
     */
    @GetMapping
    public String listarContratos(@RequestParam(value = "alunoId", required = false) Long alunoId,
                                 @RequestParam(value = "turmaId", required = false) Long turmaId,
                                 @RequestParam(value = "situacao", required = false) String situacao,
                                 @RequestParam(value = "numeroContrato", required = false) String numeroContrato,
                                 @RequestParam(value = "search", required = false) String search,
                                 Model model) {
        // Os templates válidos de contrato são apenas os de `templates/contratos/pdf/*`.
        // Para listagem/gestão de contratos, usar o módulo v2.
        return "redirect:/contratos-v2";
    }

    /**
     * Formulário para novo contrato
     */
    @GetMapping("/novo")
    public String novoContratoForm(@RequestParam(value = "alunoId", required = false) Long alunoId,
                                  @RequestParam(value = "turmaId", required = false) Long turmaId,
                                  Model model) {
        return "redirect:/contratos-v2/novo";
    }

    /**
     * Cria novo contrato
     */
    @PostMapping
    public String criarContrato(@Valid @ModelAttribute("contrato") ContratoDTO contrato,
                               BindingResult bindingResult, Model model,
                               RedirectAttributes redirectAttributes) {
        logger.info("Tentando criar novo contrato - Aluno ID: {}, Turma ID: {}", 
                   contrato.alunoId(), contrato.turmaId());
        
        // Log de validações do Bean Validation
        if (bindingResult.hasErrors()) {
            logger.warn("Erros de validação ao criar contrato: {}", bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("error", "Por favor, corrija os erros no formulário.");
            return "redirect:/contratos-v2/novo";
        }
        
        try {
            logger.debug("Dados do contrato recebidos - Aluno: {}, Turma: {}, Data Contrato: {}, Data Início: {}, Data Fim: {}",
                        contrato.alunoId(), contrato.turmaId(), contrato.dataContrato(), 
                        contrato.dataInicioVigencia(), contrato.dataFimVigencia());
            
            ContratoDTO contratoCriado = contratoService.criarContrato(contrato);
            logger.info("Contrato criado com sucesso - ID: {}, Número: {}", 
                       contratoCriado.id(), contratoCriado.numeroContrato());
            
            redirectAttributes.addFlashAttribute("success", "Contrato criado com sucesso!");
            return "redirect:/contratos-v2";
            
        } catch (BusinessException e) {
            logger.error("Erro de negócio ao criar contrato: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/contratos-v2/novo";
            
        } catch (ResourceNotFoundException e) {
            logger.error("Recurso não encontrado ao criar contrato: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/contratos-v2/novo";
            
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar contrato: ", e);
            logger.error("Stack trace completo:", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao criar contrato: " + e.getMessage());
            return "redirect:/contratos-v2/novo";
        }
    }

    /**
     * Formulário para editar contrato
     */
    @GetMapping("/editar/{id}")
    public String editarContratoForm(@PathVariable Long id, Model model) {
        return "redirect:/contratos-v2";
    }

    /**
     * Atualiza contrato
     */
    @PostMapping("/atualizar/{id}")
    public String atualizarContrato(@PathVariable Long id,
                                   @Valid @ModelAttribute("contrato") ContratoDTO contrato,
                                   BindingResult bindingResult, Model model) {
        try {
            contratoService.atualizarContrato(id, contrato);
            return "redirect:/contratos-v2?success=Contrato atualizado com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao atualizar contrato ID {}: ", id, e);
            return "redirect:/contratos-v2?error=" + e.getMessage();
        }
    }

    /**
     * Confirmação para deletar contrato
     */
    @GetMapping("/deletar/{id}")
    public String deletarContratoConfirm(@PathVariable Long id, Model model) {
        return "redirect:/contratos-v2";
    }

    /**
     * Deleta contrato
     */
    @PostMapping("/deletar/{id}")
    public String deletarContrato(@PathVariable Long id) {
        try {
            contratoService.deletarContrato(id);
            return "redirect:/contratos-v2?success=Contrato deletado com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao deletar contrato ID {}: ", id, e);
            return "redirect:/contratos-v2?error=" + e.getMessage();
        }
    }

    /**
     * Gera contrato rápido para aluno e turma
     */
    @PostMapping("/gerar-rapido")
    public String gerarContratoRapido(@RequestParam("alunoId") Long alunoId,
                                     @RequestParam("turmaId") Long turmaId) {
        try {
            contratoService.gerarContratoRapido(alunoId, turmaId);
            return "redirect:/contratos-v2?success=Contrato gerado com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao gerar contrato rápido: ", e);
            return "redirect:/contratos-v2?error=" + e.getMessage();
        }
    }

    /**
     * Visualiza contrato específico
     */
    @GetMapping("/visualizar/{id}")
    public String visualizarContrato(@PathVariable Long id, Model model) {
        // Visualização do contrato deve ser feita pelo template PDF em `templates/contratos/pdf/*`
        return "redirect:/contratos/pdf/" + id;
    }

    /**
     * Gera PDF do contrato (usa template salvo ou padrão)
     */
    @GetMapping("/pdf/{id}")
    public String gerarPdfContrato(@PathVariable Long id, Model model) {
        try {
            // Buscar entidade completa com relacionamentos para ter acesso aos dados do aluno
            Contrato contrato = contratoService.buscarContratoEntityPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
            
            // Converter para DTO para o template
            ContratoDTO contratoDTO = contratoService.buscarContratoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
            
            // Buscar turma atualizada: priorizar turma atual do aluno, senão usar turma do contrato
            br.com.arirang.plataforma.entity.Turma turmaAtualizada = null;
            
            // Primeiro, tentar usar a turma atual do aluno (se o aluno estiver em alguma turma)
            if (contrato.getAluno() != null && contrato.getAluno().getTurmas() != null && !contrato.getAluno().getTurmas().isEmpty()) {
                // Usar a primeira turma ativa do aluno (ou a primeira se não houver ativa)
                turmaAtualizada = contrato.getAluno().getTurmas().stream()
                        .filter(t -> t != null && t.getId() != null)
                        .filter(t -> "ATIVA".equalsIgnoreCase(t.getSituacaoTurma()))
                        .findFirst()
                        .orElse(contrato.getAluno().getTurmas().get(0));
                
                // Buscar dados atualizados do banco sem cache
                if (turmaAtualizada != null && turmaAtualizada.getId() != null) {
                    turmaAtualizada = turmaService.buscarTurmaPorIdSemCache(turmaAtualizada.getId())
                            .orElse(turmaAtualizada);
                }
            }
            
            // Se não encontrou turma do aluno, usar a turma do contrato (atualizada)
            if (turmaAtualizada == null && contrato.getTurma() != null && contrato.getTurma().getId() != null) {
                turmaAtualizada = turmaService.buscarTurmaPorIdSemCache(contrato.getTurma().getId())
                        .orElse(contrato.getTurma());
            } else if (turmaAtualizada == null) {
                turmaAtualizada = contrato.getTurma();
            }
            
            // Adicionar atributos ao modelo
            model.addAttribute("contrato", contratoDTO);
            model.addAttribute("aluno", contrato.getAluno());
            model.addAttribute("turma", turmaAtualizada);
            if (contrato.getAluno() != null && contrato.getAluno().getResponsavel() != null) {
                model.addAttribute("responsavel", contrato.getAluno().getResponsavel());
            }
            adicionarValoresComDescontoAoModel(model, contratoDTO);
            
            // Verificar se há template PDF salvo no contrato
            String templatePdf = contratoDTO.templatePdf();
            if (templatePdf != null && !templatePdf.trim().isEmpty()) {
                // Lista de templates válidos
                List<String> templatesValidos = Arrays.asList(
                    "contrato-servicos-menor",
                    "contrato-curso",
                    "uso-imagem-menor",
                    "uso-imagem-adulto"
                );
                
                if (templatesValidos.contains(templatePdf)) {
                    // Usar o template salvo
                    return "contratos/pdf/" + templatePdf;
                } else {
                    logger.warn("Template inválido no contrato ID {}: {}. Usando template padrão.", id, templatePdf);
                }
            }
            
            // Usar template padrão se não houver template salvo ou se for inválido
            return "contratos/pdf/contrato-curso";
            
        } catch (Exception e) {
            logger.error("Erro ao gerar PDF do contrato ID {}: ", id, e);
            model.addAttribute("error", "Erro ao gerar PDF: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Gera PDF do contrato com template customizado
     */
    @GetMapping("/pdf/{id}/{template}")
    public String gerarPdfContratoCustom(@PathVariable Long id, 
                                        @PathVariable String template, 
                                        Model model) {
        try {
            // Lista de templates válidos
            List<String> templatesValidos = Arrays.asList(
                "contrato-servicos-menor",
                "contrato-curso",
                "uso-imagem-menor",
                "uso-imagem-adulto"
            );
            
            if (!templatesValidos.contains(template)) {
                logger.warn("Template inválido solicitado: {}", template);
                model.addAttribute("error", "Template inválido: " + template);
                return "error";
            }

            // Buscar entidade completa com relacionamentos
            Contrato contrato = contratoService.buscarContratoEntityPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
            
            // Converter para DTO para o template
            ContratoDTO contratoDTO = contratoService.buscarContratoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
            
            // Buscar turma atualizada: priorizar turma atual do aluno, senão usar turma do contrato
            br.com.arirang.plataforma.entity.Turma turmaAtualizada = null;
            
            // Primeiro, tentar usar a turma atual do aluno (se o aluno estiver em alguma turma)
            if (contrato.getAluno() != null && contrato.getAluno().getTurmas() != null && !contrato.getAluno().getTurmas().isEmpty()) {
                // Usar a primeira turma ativa do aluno (ou a primeira se não houver ativa)
                turmaAtualizada = contrato.getAluno().getTurmas().stream()
                        .filter(t -> t != null && t.getId() != null)
                        .filter(t -> "ATIVA".equalsIgnoreCase(t.getSituacaoTurma()))
                        .findFirst()
                        .orElse(contrato.getAluno().getTurmas().get(0));
                
                // Buscar dados atualizados do banco sem cache
                if (turmaAtualizada != null && turmaAtualizada.getId() != null) {
                    turmaAtualizada = turmaService.buscarTurmaPorIdSemCache(turmaAtualizada.getId())
                            .orElse(turmaAtualizada);
                }
            }
            
            // Se não encontrou turma do aluno, usar a turma do contrato (atualizada)
            if (turmaAtualizada == null && contrato.getTurma() != null && contrato.getTurma().getId() != null) {
                turmaAtualizada = turmaService.buscarTurmaPorIdSemCache(contrato.getTurma().getId())
                        .orElse(contrato.getTurma());
            } else if (turmaAtualizada == null) {
                turmaAtualizada = contrato.getTurma();
            }
            
            // Adicionar atributos ao modelo
            model.addAttribute("contrato", contratoDTO);
            model.addAttribute("aluno", contrato.getAluno());
            model.addAttribute("turma", turmaAtualizada);
            if (contrato.getAluno() != null && contrato.getAluno().getResponsavel() != null) {
                model.addAttribute("responsavel", contrato.getAluno().getResponsavel());
            }
            adicionarValoresComDescontoAoModel(model, contratoDTO);
            
            // Retornar o template específico
            return "contratos/pdf/" + template;
            
        } catch (Exception e) {
            logger.error("Erro ao gerar PDF customizado do contrato ID {} com template {}: ", id, template, e);
            model.addAttribute("error", "Erro ao gerar PDF: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Download do PDF do contrato com nome personalizado
     * Formato: "ID - NumeroContrato - Nome do Aluno.pdf"
     */
    @GetMapping("/download/{id}")
    public String downloadPdfContrato(@PathVariable Long id, Model model) {
        try {
            // Buscar entidade completa com relacionamentos
            Contrato contrato = contratoService.buscarContratoEntityPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
            
            // Converter para DTO para o template
            ContratoDTO contratoDTO = contratoService.buscarContratoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
            
            // Buscar turma atualizada: priorizar turma atual do aluno, senão usar turma do contrato
            br.com.arirang.plataforma.entity.Turma turmaAtualizada = null;
            
            // Primeiro, tentar usar a turma atual do aluno (se o aluno estiver em alguma turma)
            if (contrato.getAluno() != null && contrato.getAluno().getTurmas() != null && !contrato.getAluno().getTurmas().isEmpty()) {
                // Usar a primeira turma ativa do aluno (ou a primeira se não houver ativa)
                turmaAtualizada = contrato.getAluno().getTurmas().stream()
                        .filter(t -> t != null && t.getId() != null)
                        .filter(t -> "ATIVA".equalsIgnoreCase(t.getSituacaoTurma()))
                        .findFirst()
                        .orElse(contrato.getAluno().getTurmas().get(0));
                
                // Buscar dados atualizados do banco sem cache
                if (turmaAtualizada != null && turmaAtualizada.getId() != null) {
                    turmaAtualizada = turmaService.buscarTurmaPorIdSemCache(turmaAtualizada.getId())
                            .orElse(turmaAtualizada);
                }
            }
            
            // Se não encontrou turma do aluno, usar a turma do contrato (atualizada)
            if (turmaAtualizada == null && contrato.getTurma() != null && contrato.getTurma().getId() != null) {
                turmaAtualizada = turmaService.buscarTurmaPorIdSemCache(contrato.getTurma().getId())
                        .orElse(contrato.getTurma());
            } else if (turmaAtualizada == null) {
                turmaAtualizada = contrato.getTurma();
            }
            
            // Adicionar atributos ao modelo
            model.addAttribute("contrato", contratoDTO);
            model.addAttribute("aluno", contrato.getAluno());
            model.addAttribute("turma", turmaAtualizada);
            if (contrato.getAluno() != null && contrato.getAluno().getResponsavel() != null) {
                model.addAttribute("responsavel", contrato.getAluno().getResponsavel());
            }
            adicionarValoresComDescontoAoModel(model, contratoDTO);
            
            // Gerar nome do arquivo: "ID - NumeroContrato - Nome do Aluno"
            String numeroContrato = contratoDTO.numeroContrato() != null ? contratoDTO.numeroContrato() : String.valueOf(id);
            String nomeAluno = contrato.getAluno() != null && contrato.getAluno().getNomeCompleto() != null 
                ? contrato.getAluno().getNomeCompleto().replaceAll("[^a-zA-Z0-9\\s]", "_").replaceAll("\\s+", "_")
                : "Aluno_" + id;
            String nomeArquivo = id + " - " + numeroContrato + " - " + nomeAluno + ".pdf";
            model.addAttribute("nomeArquivo", nomeArquivo);
            
            // Determinar qual template usar
            String templatePdf = contratoDTO.templatePdf();
            String templateParaUsar = "contratos/pdf/contrato-curso"; // padrão (somente templates válidos)
            
            if (templatePdf != null && !templatePdf.trim().isEmpty()) {
                List<String> templatesValidos = Arrays.asList(
                    "contrato-servicos-menor",
                    "contrato-curso",
                    "uso-imagem-menor",
                    "uso-imagem-adulto"
                );
                
                if (templatesValidos.contains(templatePdf)) {
                    templateParaUsar = "contratos/pdf/" + templatePdf;
                }
            }
            
            // Adicionar flag para indicar que é download
            model.addAttribute("downloadMode", true);
            model.addAttribute("templateParaUsar", templateParaUsar);
            
            // Retornar o template do contrato diretamente, que terá o script de download
            return templateParaUsar;
            
        } catch (Exception e) {
            logger.error("Erro ao preparar download do PDF do contrato ID {}: ", id, e);
            model.addAttribute("error", "Erro ao preparar download: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Cria contrato sem validações (bypass) - OPcional
     * ⚠️ Use apenas quando necessário criar contratos que normalmente seriam bloqueados
     */
    /**
     * Endpoint administrativo para migrar parcelas dos contratos existentes
     * Atualiza as datas de vencimento das parcelas PENDENTES para usar dataInicioVigencia
     * ao invés de dataContrato
     * 
     * ⚠️ ATENÇÃO: Este endpoint deve ser executado apenas uma vez após a atualização do sistema
     */
    @PostMapping("/admin/migrar-parcelas")
    public String migrarParcelasParaDataInicioVigencia(RedirectAttributes redirectAttributes) {
        try {
            logger.info("Iniciando migração de parcelas para dataInicioVigencia");
            int contratosAtualizados = contratoService.migrarParcelasParaDataInicioVigencia();
            
            redirectAttributes.addFlashAttribute("success", 
                String.format("Migração concluída com sucesso! %d contratos tiveram suas parcelas atualizadas.", 
                    contratosAtualizados));
            
            logger.info("Migração concluída: {} contratos atualizados", contratosAtualizados);
            return "redirect:/contratos";
            
        } catch (Exception e) {
            logger.error("Erro ao executar migração de parcelas: ", e);
            redirectAttributes.addFlashAttribute("error", 
                "Erro ao executar migração: " + e.getMessage());
            return "redirect:/contratos";
        }
    }

    /**
     * Endpoint administrativo para migrar valores das parcelas dos contratos existentes
     * Atualiza os valores das parcelas PENDENTES para incluir matrícula na primeira parcela
     * 
     * ⚠️ ATENÇÃO: Este endpoint deve ser executado apenas uma vez após a atualização do sistema
     */
    @PostMapping("/admin/migrar-valores-parcelas")
    public String migrarValoresParcelasParaIncluirMatricula(RedirectAttributes redirectAttributes) {
        try {
            logger.info("Iniciando migração de valores de parcelas para incluir matrícula");
            int contratosAtualizados = contratoService.migrarValoresParcelasParaIncluirMatricula();
            
            redirectAttributes.addFlashAttribute("success", 
                String.format("Migração de valores concluída com sucesso! %d contratos tiveram os valores de suas parcelas atualizados.", 
                    contratosAtualizados));
            
            logger.info("Migração de valores concluída: {} contratos atualizados", contratosAtualizados);
            return "redirect:/contratos";
            
        } catch (Exception e) {
            logger.error("Erro ao executar migração de valores de parcelas: ", e);
            redirectAttributes.addFlashAttribute("error", 
                "Erro ao executar migração de valores: " + e.getMessage());
            return "redirect:/contratos";
        }
    }

    @PostMapping("/criar-sem-validacao")
    public String criarSemValidacao(@Valid @ModelAttribute("contrato") ContratoDTO contrato,
                                   BindingResult bindingResult, 
                                   Model model,
                                   RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("error", "Por favor, corrija os erros no formulário.");
            return "redirect:/contratos-v2/novo";
        }
        
        try {
            contratoService.criarContratoSemValidacao(contrato);
            ra.addFlashAttribute("success", "Contrato criado sem validações!");
            return "redirect:/contratos-v2";
        } catch (Exception e) {
            logger.error("Erro ao criar contrato sem validação: ", e);
            ra.addFlashAttribute("error", "Erro ao criar contrato: " + e.getMessage());
            return "redirect:/contratos-v2/novo";
        }
    }
}
