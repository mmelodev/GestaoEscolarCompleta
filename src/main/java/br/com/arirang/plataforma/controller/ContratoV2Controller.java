package br.com.arirang.plataforma.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.entity.Contrato;
import br.com.arirang.plataforma.entity.Parcela;
import br.com.arirang.plataforma.entity.StatusParcela;
import br.com.arirang.plataforma.dto.ContratoDTO;
import br.com.arirang.plataforma.service.AlunoService;
import br.com.arirang.plataforma.service.TurmaService;
import br.com.arirang.plataforma.service.ContratoService;
import br.com.arirang.plataforma.service.FinanceiroContratoSyncService;
import br.com.arirang.plataforma.repository.ParcelaRepository;
import br.com.arirang.plataforma.repository.ContratoRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/contratos-v2")
public class ContratoV2Controller {

    private static final Logger logger = LoggerFactory.getLogger(ContratoV2Controller.class);

    @Autowired
    private AlunoService alunoService;
    
    @Autowired
    private TurmaService turmaService;
    
    @Autowired
    private ContratoService contratoService;
    
    @Autowired
    private FinanceiroContratoSyncService financeiroSyncService;
    
    @Autowired
    private ParcelaRepository parcelaRepository;
    
    @Autowired
    private ContratoRepository contratoRepository;

    @GetMapping
    public String listarContratos(Model model) {
        try {
            logger.info("Listando contratos reais do banco de dados");
            
            // Buscar contratos reais do banco de dados
            List<ContratoDTO> contratosReais = contratoService.buscarContratosAtivos();
            logger.info("Contratos encontrados: {}", contratosReais.size());
            
            model.addAttribute("contratos", contratosReais);
            model.addAttribute("totalContratos", contratosReais.size());
            return "contratos-v2/lista";
        } catch (Exception e) {
            logger.error("Erro ao listar contratos: ", e);
            model.addAttribute("error", "Erro ao listar contratos: " + e.getMessage());
            model.addAttribute("contratos", new ArrayList<>());
            model.addAttribute("totalContratos", 0);
            return "contratos-v2/lista";
        }
    }

    @GetMapping("/{id}")
    public String verContrato(@PathVariable Long id) {
        return "redirect:/contratos-v2/detalhes/" + id;
    }

    @GetMapping("/detalhes/{id}")
    public String detalhesContrato(@PathVariable Long id, Model model) {
        try {
            Contrato contrato = contratoService.buscarContratoEntityPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
            ContratoDTO contratoDTO = contratoService.buscarContratoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
            
            // Buscar turma atualizada: priorizar turma atual do aluno, senão usar turma do contrato
            Turma turmaAtualizada = null;
            
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
            
            model.addAttribute("contrato", contratoDTO);
            model.addAttribute("aluno", contrato.getAluno());
            model.addAttribute("turma", turmaAtualizada);
            return "contratos-v2/detalhes";
        } catch (Exception e) {
            logger.error("Erro ao carregar detalhes do contrato ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar detalhes: " + e.getMessage());
            return "redirect:/contratos-v2";
        }
    }

    @GetMapping("/novo")
    public String novoContrato(Model model) {
        try {
            logger.info("Carregando formulário de novo contrato");
            
            // Carregar alunos (com turmas) e turmas do banco de dados
            List<Aluno> alunos = alunoService.listarTodosAlunos();
            List<Turma> turmas = turmaService.listarTodasTurmas();
            
            logger.info("Carregados {} alunos e {} turmas", alunos.size(), turmas.size());
            
            model.addAttribute("alunos", alunos);
            model.addAttribute("turmas", turmas);
            
            return "contratos-v2/form";
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário: ", e);
            model.addAttribute("error", "Erro ao carregar formulário: " + e.getMessage());
            return "contratos-v2/form";
        }
    }

    /**
     * API JSON: turmas do aluno para auto-seleção no formulário de novo contrato.
     */
    @GetMapping("/api/aluno/{id}/turmas")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> turmasDoAluno(@PathVariable Long id) {
        List<Map<String, Object>> empty = Collections.<Map<String, Object>>emptyList();
        try {
            var opt = alunoService.buscarAlunoPorId(id);
            if (opt.isEmpty()) {
                return ResponseEntity.ok(empty);
            }
            var a = opt.get();
            if (a.getTurmas() == null || a.getTurmas().isEmpty()) {
                return ResponseEntity.ok(empty);
            }
            List<Map<String, Object>> list = new ArrayList<>();
            for (Turma t : a.getTurmas()) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", t.getId());
                m.put("nomeTurma", t.getNomeTurma());
                list.add(m);
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            logger.warn("Erro ao buscar turmas do aluno {}: ", id, e);
            return ResponseEntity.ok(empty);
        }
    }

    @PostMapping
    public String criarContrato(@RequestParam(required = false) String alunoId,
                               @RequestParam(required = false) String turmaId,
                               @RequestParam(required = false) String valorMatricula,
                               @RequestParam(required = false) String valorMensalidade,
                               @RequestParam(required = false) String numeroParcelas,
                               @RequestParam(required = false) String descontoPercentual,
                               @RequestParam(required = false) String templatePdf,
                               @RequestParam(required = false) String observacoes,
                               RedirectAttributes redirectAttributes) {
        try {
            logger.info("Criando contrato real para aluno ID: '{}' e turma ID: '{}'", alunoId, turmaId);
            logger.info("Parâmetros recebidos - valorMatricula: '{}', valorMensalidade: '{}', numeroParcelas: '{}'", 
                       valorMatricula, valorMensalidade, numeroParcelas);
            
            // Converter strings para tipos apropriados
            Long alunoIdLong = null;
            Long turmaIdLong = null;
            BigDecimal valorMatriculaBD = null;
            BigDecimal valorMensalidadeBD = null;
            Integer numeroParcelasInt = null;
            
            try {
                if (alunoId != null && !alunoId.trim().isEmpty()) {
                    alunoIdLong = Long.parseLong(alunoId);
                }
            } catch (NumberFormatException e) {
                logger.warn("Erro ao converter alunoId: '{}'", alunoId);
            }
            
            try {
                if (turmaId != null && !turmaId.trim().isEmpty()) {
                    turmaIdLong = Long.parseLong(turmaId);
                }
            } catch (NumberFormatException e) {
                logger.warn("Erro ao converter turmaId: '{}'", turmaId);
            }
            
            try {
                if (valorMatricula != null && !valorMatricula.trim().isEmpty() && !valorMatricula.equals("") && !valorMatricula.equals("null")) {
                    valorMatriculaBD = new BigDecimal(valorMatricula.trim());
                }
            } catch (NumberFormatException e) {
                logger.warn("Erro ao converter valorMatricula: '{}'", valorMatricula);
            }
            
            try {
                if (valorMensalidade != null && !valorMensalidade.trim().isEmpty() && !valorMensalidade.equals("") && !valorMensalidade.equals("null")) {
                    valorMensalidadeBD = new BigDecimal(valorMensalidade.trim());
                }
            } catch (NumberFormatException e) {
                logger.warn("Erro ao converter valorMensalidade: '{}'", valorMensalidade);
            }
            
            try {
                if (numeroParcelas != null && !numeroParcelas.trim().isEmpty() && !numeroParcelas.equals("") && !numeroParcelas.equals("null")) {
                    numeroParcelasInt = Integer.parseInt(numeroParcelas.trim());
                    logger.info("numeroParcelas convertido com sucesso: {}", numeroParcelasInt);
                } else {
                    logger.warn("numeroParcelas é null, vazio ou 'null': '{}'", numeroParcelas);
                }
            } catch (NumberFormatException e) {
                logger.warn("Erro ao converter numeroParcelas: '{}'", numeroParcelas);
            }

            BigDecimal descontoPercentualBD = BigDecimal.ZERO;
            try {
                if (descontoPercentual != null && !descontoPercentual.trim().isEmpty() && !descontoPercentual.equals("null")) {
                    descontoPercentualBD = new BigDecimal(descontoPercentual.trim());
                    if (descontoPercentualBD.compareTo(BigDecimal.ZERO) < 0) {
                        descontoPercentualBD = BigDecimal.ZERO;
                    } else if (descontoPercentualBD.compareTo(BigDecimal.valueOf(100)) > 0) {
                        descontoPercentualBD = BigDecimal.valueOf(100);
                    }
                }
            } catch (NumberFormatException e) {
                logger.warn("Erro ao converter descontoPercentual: '{}'", descontoPercentual);
            }
            
            // Validar dados obrigatórios antes de criar o DTO
            if (alunoIdLong == null) {
                redirectAttributes.addFlashAttribute("error", "Aluno é obrigatório");
                redirectAttributes.addFlashAttribute("alunoId", alunoId);
                redirectAttributes.addFlashAttribute("turmaId", turmaId);
                redirectAttributes.addFlashAttribute("valorMatricula", valorMatricula);
                redirectAttributes.addFlashAttribute("valorMensalidade", valorMensalidade);
                redirectAttributes.addFlashAttribute("numeroParcelas", numeroParcelas);
                redirectAttributes.addFlashAttribute("descontoPercentual", descontoPercentual);
                redirectAttributes.addFlashAttribute("templatePdf", templatePdf);
                redirectAttributes.addFlashAttribute("observacoes", observacoes);
                return "redirect:/contratos-v2/novo";
            }
            
            if (turmaIdLong == null) {
                redirectAttributes.addFlashAttribute("error", "Turma é obrigatória");
                redirectAttributes.addFlashAttribute("alunoId", alunoId);
                redirectAttributes.addFlashAttribute("turmaId", turmaId);
                redirectAttributes.addFlashAttribute("valorMatricula", valorMatricula);
                redirectAttributes.addFlashAttribute("valorMensalidade", valorMensalidade);
                redirectAttributes.addFlashAttribute("numeroParcelas", numeroParcelas);
                redirectAttributes.addFlashAttribute("descontoPercentual", descontoPercentual);
                redirectAttributes.addFlashAttribute("templatePdf", templatePdf);
                redirectAttributes.addFlashAttribute("observacoes", observacoes);
                return "redirect:/contratos-v2/novo";
            }
            
            // Template PDF obrigatório (tipo de contrato)
            if (templatePdf == null || templatePdf.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tipo de contrato é obrigatório.");
                redirectAttributes.addFlashAttribute("alunoId", alunoId);
                redirectAttributes.addFlashAttribute("turmaId", turmaId);
                redirectAttributes.addFlashAttribute("valorMatricula", valorMatricula);
                redirectAttributes.addFlashAttribute("valorMensalidade", valorMensalidade);
                redirectAttributes.addFlashAttribute("numeroParcelas", numeroParcelas);
                redirectAttributes.addFlashAttribute("descontoPercentual", descontoPercentual);
                redirectAttributes.addFlashAttribute("templatePdf", templatePdf);
                redirectAttributes.addFlashAttribute("observacoes", observacoes);
                return "redirect:/contratos-v2/novo";
            }

            if (numeroParcelasInt == null || numeroParcelasInt <= 0) {
                redirectAttributes.addFlashAttribute("error", "Número de parcelas é obrigatório e deve ser maior que zero");
                redirectAttributes.addFlashAttribute("alunoId", alunoId);
                redirectAttributes.addFlashAttribute("turmaId", turmaId);
                redirectAttributes.addFlashAttribute("valorMatricula", valorMatricula);
                redirectAttributes.addFlashAttribute("valorMensalidade", valorMensalidade);
                redirectAttributes.addFlashAttribute("numeroParcelas", numeroParcelas);
                redirectAttributes.addFlashAttribute("descontoPercentual", descontoPercentual);
                redirectAttributes.addFlashAttribute("templatePdf", templatePdf);
                redirectAttributes.addFlashAttribute("observacoes", observacoes);
                return "redirect:/contratos-v2/novo";
            }
            
            if (valorMatriculaBD == null || valorMatriculaBD.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error", "Valor da matrícula é obrigatório e deve ser maior que zero");
                redirectAttributes.addFlashAttribute("alunoId", alunoId);
                redirectAttributes.addFlashAttribute("turmaId", turmaId);
                redirectAttributes.addFlashAttribute("valorMatricula", valorMatricula);
                redirectAttributes.addFlashAttribute("valorMensalidade", valorMensalidade);
                redirectAttributes.addFlashAttribute("numeroParcelas", numeroParcelas);
                redirectAttributes.addFlashAttribute("descontoPercentual", descontoPercentual);
                redirectAttributes.addFlashAttribute("templatePdf", templatePdf);
                redirectAttributes.addFlashAttribute("observacoes", observacoes);
                return "redirect:/contratos-v2/novo";
            }
            
            if (valorMensalidadeBD == null || valorMensalidadeBD.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error", "Valor da mensalidade é obrigatório e deve ser maior que zero");
                redirectAttributes.addFlashAttribute("alunoId", alunoId);
                redirectAttributes.addFlashAttribute("turmaId", turmaId);
                redirectAttributes.addFlashAttribute("valorMatricula", valorMatricula);
                redirectAttributes.addFlashAttribute("valorMensalidade", valorMensalidade);
                redirectAttributes.addFlashAttribute("numeroParcelas", numeroParcelas);
                redirectAttributes.addFlashAttribute("descontoPercentual", descontoPercentual);
                redirectAttributes.addFlashAttribute("templatePdf", templatePdf);
                redirectAttributes.addFlashAttribute("observacoes", observacoes);
                return "redirect:/contratos-v2/novo";
            }
            
            // Buscar turma para obter datas de início e fim
            Turma turma = turmaService.buscarTurmaPorId(turmaIdLong)
                    .orElseThrow(() -> new RuntimeException("Turma não encontrada"));
            
            LocalDate dataInicioVigencia = turma.getInicioTurma() != null ? turma.getInicioTurma() : LocalDate.now();
            LocalDate dataFimVigencia = turma.getTerminoTurma() != null ? turma.getTerminoTurma() : LocalDate.now().plusMonths(6);
            
            // Criar contrato real no banco usando DTO
            ContratoDTO contratoDTO = new ContratoDTO(
                null, // id
                alunoIdLong, // alunoId
                null, // alunoNome (será preenchido automaticamente)
                turmaIdLong, // turmaId
                null, // turmaNome (será preenchido automaticamente)
                null, // numeroContrato (será gerado automaticamente)
                LocalDate.now(), // dataContrato
                dataInicioVigencia, // dataInicioVigencia
                dataFimVigencia, // dataFimVigencia
                valorMatriculaBD != null ? valorMatriculaBD : BigDecimal.ZERO, // valorMatricula
                valorMensalidadeBD != null ? valorMensalidadeBD : BigDecimal.ZERO, // valorMensalidade
                numeroParcelasInt != null ? numeroParcelasInt : 0, // numeroParcelas
                BigDecimal.ZERO, // descontoValor
                descontoPercentualBD != null ? descontoPercentualBD : BigDecimal.ZERO, // descontoPercentual
                null, // valorTotalContrato (será calculado automaticamente)
                observacoes != null ? observacoes : "", // observacoes
                "ATIVO", // situacaoContrato (String, não StatusContrato)
                templatePdf != null ? templatePdf.trim() : null, // templatePdf (obrigatório)
                null, // dataCriacao
                null // dataAtualizacao
            );
            
            // Criar contrato no banco (retorna ContratoDTO)
            ContratoDTO contratoCriado = contratoService.criarContrato(contratoDTO);
            
            // Sincronizar automaticamente com o sistema financeiro
            try {
                financeiroSyncService.sincronizarContratoComFinanceiro(contratoCriado.id());
                logger.info("Contrato ID {} sincronizado automaticamente com sistema financeiro", contratoCriado.id());
            } catch (Exception syncError) {
                logger.warn("Erro na sincronização automática do contrato ID {}: {}", contratoCriado.id(), syncError.getMessage());
                // Não falha a criação do contrato se a sincronização falhar
            }
            
            logger.info("Contrato criado com sucesso! ID: {}", contratoCriado.id());
            
            redirectAttributes.addFlashAttribute("success", 
                String.format("Contrato criado com sucesso! ID: %d. Sincronização automática realizada.", contratoCriado.id()));
            return "redirect:/contratos-v2";
            
        } catch (Exception e) {
            logger.error("Erro ao criar contrato: ", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao criar contrato: " + e.getMessage());
            redirectAttributes.addFlashAttribute("alunoId", alunoId);
            redirectAttributes.addFlashAttribute("turmaId", turmaId);
            redirectAttributes.addFlashAttribute("valorMatricula", valorMatricula);
            redirectAttributes.addFlashAttribute("valorMensalidade", valorMensalidade);
            redirectAttributes.addFlashAttribute("numeroParcelas", numeroParcelas);
            redirectAttributes.addFlashAttribute("descontoPercentual", descontoPercentual);
            redirectAttributes.addFlashAttribute("templatePdf", templatePdf);
            redirectAttributes.addFlashAttribute("observacoes", observacoes);
            return "redirect:/contratos-v2/novo";
        }
    }

    @GetMapping("/sincronizar-financeiro")
    public String sincronizarComFinanceiro(RedirectAttributes redirectAttributes) {
        try {
            logger.info("Iniciando sincronização de contratos com sistema financeiro");
            
            // Buscar contratos ativos para sincronizar
            List<ContratoDTO> contratosAtivos = contratoService.buscarContratosAtivos();
            logger.info("Encontrados {} contratos ativos para sincronização", contratosAtivos.size());
            
            if (contratosAtivos.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning", "Nenhum contrato ativo encontrado para sincronização.");
                return "redirect:/contratos-v2";
            }
            
            // Sincronizar TODAS as parcelas de todos os contratos ativos
            financeiroSyncService.sincronizarTodosContratosAtivos();
            
            redirectAttributes.addFlashAttribute("success", 
                String.format("✅ Sincronização concluída! %d contratos ativos foram sincronizados com o sistema financeiro (TODAS as parcelas).", contratosAtivos.size()));
            return "redirect:/contratos-v2";
            
        } catch (Exception e) {
            logger.error("Erro ao sincronizar contratos com financeiro: ", e);
            redirectAttributes.addFlashAttribute("error", "❌ Erro na sincronização: " + e.getMessage());
            return "redirect:/contratos-v2";
        }
    }

    @GetMapping("/sincronizar-financeiro-real")
    public String sincronizarContratosReaisComFinanceiro(RedirectAttributes redirectAttributes) {
        try {
            logger.info("Iniciando sincronização de contratos REAIS com sistema financeiro");
            
            // Buscar contratos ativos para sincronizar
            List<ContratoDTO> contratosAtivos = contratoService.buscarContratosAtivos();
            logger.info("Encontrados {} contratos ativos para sincronização", contratosAtivos.size());
            
            if (contratosAtivos.isEmpty()) {
                redirectAttributes.addFlashAttribute("warning", "Nenhum contrato ativo encontrado para sincronização.");
                return "redirect:/financeiro";
            }
            
            // Sincronizar TODAS as parcelas de todos os contratos ativos
            financeiroSyncService.sincronizarTodosContratosAtivos();
            
            redirectAttributes.addFlashAttribute("success", 
                String.format("✅ Sincronização REAL concluída! %d contratos ativos foram sincronizados com o sistema financeiro (TODAS as parcelas).", contratosAtivos.size()));
            return "redirect:/financeiro";
            
        } catch (Exception e) {
            logger.error("Erro ao sincronizar contratos reais com financeiro: ", e);
            redirectAttributes.addFlashAttribute("error", "❌ Erro na sincronização: " + e.getMessage());
            return "redirect:/contratos-v2";
        }
    }

    @GetMapping("/diagnostico/{contratoId}")
    @ResponseBody
    public String diagnosticarContrato(@PathVariable Long contratoId) {
        try {
            return financeiroSyncService.diagnosticarContrato(contratoId);
        } catch (Exception e) {
            logger.error("Erro ao diagnosticar contrato ID {}: ", contratoId, e);
            return "❌ Erro ao diagnosticar: " + e.getMessage();
        }
    }

    @GetMapping("/debug-parcelas")
    public String debugParcelas(Model model) {
        try {
            logger.info("Debug parcelas: Verificando parcelas dos contratos");
            
            // Verificar contratos e suas parcelas
            List<ContratoDTO> contratosDTO = contratoService.buscarContratosAtivos();
            List<ContratoComParcelas> contratosComParcelas = new ArrayList<>();
            
            for (ContratoDTO contratoDTO : contratosDTO) {
                // Buscar entidade Contrato do repository usando o ID do DTO
                Contrato contrato = contratoRepository.findById(contratoDTO.id())
                        .orElse(null);
                if (contrato != null) {
                    List<Parcela> parcelas = parcelaRepository.findByContratoId(contrato.getId());
                    contratosComParcelas.add(new ContratoComParcelas(contrato, parcelas));
                }
            }
            
            model.addAttribute("contratosComParcelas", contratosComParcelas);
            model.addAttribute("totalContratos", contratosComParcelas.size());
            
            return "contratos-v2/debug-parcelas";
            
        } catch (Exception e) {
            logger.error("Erro no debug parcelas: ", e);
            model.addAttribute("error", "Erro no debug: " + e.getMessage());
            return "error";
        }
    }
    
    // Classe interna para debug
    public static class ContratoComParcelas {
        private Contrato contrato;
        private List<Parcela> parcelas;
        
        public ContratoComParcelas(Contrato contrato, List<Parcela> parcelas) {
            this.contrato = contrato;
            this.parcelas = parcelas;
        }
        
        public Contrato getContrato() { return contrato; }
        public List<Parcela> getParcelas() { return parcelas; }
        public int getTotalParcelas() { return parcelas.size(); }
    }

    @GetMapping("/teste-parcelas")
    public String testeParcelas(RedirectAttributes redirectAttributes) {
        try {
            logger.info("Teste parcelas: tentando criar parcelas para contrato existente");
            
            // Buscar contrato existente
            List<ContratoDTO> contratosDTO = contratoService.buscarContratosAtivos();
            if (contratosDTO.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Nenhum contrato encontrado");
                return "redirect:/contratos-v2/debug-parcelas";
            }
            
            // Buscar entidade Contrato do repository usando o ID do primeiro DTO
            Contrato contrato = contratoRepository.findById(contratosDTO.get(0).id())
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado"));
            logger.info("Contrato encontrado: ID={}, Parcelas={}", contrato.getId(), contrato.getNumeroParcelas());
            
            // Tentar criar parcelas manualmente
            List<Parcela> parcelasExistentes = parcelaRepository.findByContratoId(contrato.getId());
            logger.info("Parcelas existentes: {}", parcelasExistentes.size());
            
            if (parcelasExistentes.isEmpty()) {
                logger.info("Criando parcelas manualmente...");
                
                List<Parcela> parcelas = new ArrayList<>();
                // Usar dataInicioVigencia se disponível, caso contrário usar dataContrato
                LocalDate dataBase = contrato.getDataInicioVigencia() != null 
                        ? contrato.getDataInicioVigencia() 
                        : contrato.getDataContrato();
                LocalDate dataVencimento = dataBase.plusMonths(1);
                
                for (int i = 1; i <= contrato.getNumeroParcelas(); i++) {
                    Parcela parcela = new Parcela();
                    parcela.setContrato(contrato);
                    parcela.setNumeroParcela(i);
                    
                    // Primeira parcela: valor da matrícula + valor da mensalidade
                    // Demais parcelas: apenas valor da mensalidade
                    BigDecimal valorParcela;
                    if (i == 1) {
                        valorParcela = contrato.getValorMensalidade();
                        if (contrato.getValorMatricula() != null) {
                            valorParcela = valorParcela.add(contrato.getValorMatricula());
                        }
                    } else {
                        valorParcela = contrato.getValorMensalidade();
                    }
                    
                    parcela.setValorParcela(valorParcela);
                    parcela.setDataVencimento(dataVencimento);
                    parcela.setStatusParcela(StatusParcela.PENDENTE);
                    
                    parcelas.add(parcela);
                    logger.info("Parcela {} criada: valor={}, vencimento={}", i, parcela.getValorParcela(), parcela.getDataVencimento());
                    dataVencimento = dataVencimento.plusMonths(1);
                }
                
                parcelaRepository.saveAll(parcelas);
                logger.info("Parcelas salvas: {}", parcelas.size());
                
                redirectAttributes.addFlashAttribute("success", 
                    String.format("Parcelas criadas com sucesso! %d parcelas para contrato ID %d", parcelas.size(), contrato.getId()));
            } else {
                redirectAttributes.addFlashAttribute("info", 
                    String.format("Contrato já possui %d parcelas", parcelasExistentes.size()));
            }
            
            return "redirect:/contratos-v2/debug-parcelas";
            
        } catch (Exception e) {
            logger.error("Erro no teste parcelas: ", e);
            redirectAttributes.addFlashAttribute("error", "Erro no teste parcelas: " + e.getMessage());
            return "redirect:/contratos-v2/debug-parcelas";
        }
    }

    @GetMapping("/debug-count")
    public ResponseEntity<String> debugCount() {
        try {
            List<ContratoDTO> ativos = contratoService.buscarContratosAtivos();
            return ResponseEntity.ok(String.format("Contratos ativos no banco: %d", ativos.size()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro ao contar contratos: " + e.getMessage());
        }
    }

    @GetMapping("/deletar/{id}")
    public String deletarContratoConfirm(@PathVariable Long id, Model model) {
        try {
            ContratoDTO contrato = contratoService.buscarContratoPorId(id)
                    .orElseThrow(() -> new RuntimeException("Contrato não encontrado com ID: " + id));
            
            model.addAttribute("contrato", contrato);
            
            return "contratos-v2/delete";
        } catch (Exception e) {
            logger.error("Erro ao carregar confirmação de deleção para contrato ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar a confirmação: " + e.getMessage());
            return "redirect:/contratos-v2";
        }
    }

    @PostMapping("/deletar/{id}")
    public String deletarContrato(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            contratoService.deletarContrato(id);
            redirectAttributes.addFlashAttribute("success", "Contrato deletado com sucesso");
            return "redirect:/contratos-v2";
        } catch (Exception e) {
            logger.error("Erro ao deletar contrato com ID {}: ", id, e);
            redirectAttributes.addFlashAttribute("error", "Erro ao deletar contrato: " + e.getMessage());
            return "redirect:/contratos-v2";
        }
    }

}

