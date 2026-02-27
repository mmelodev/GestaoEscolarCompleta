package br.com.arirang.plataforma.controller;

import br.com.arirang.plataforma.dto.AlunoDTO;
import br.com.arirang.plataforma.dto.PagamentoDTO;
import br.com.arirang.plataforma.dto.ReceitaDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Pagamento;
import br.com.arirang.plataforma.entity.Receita;
import br.com.arirang.plataforma.entity.Contrato;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.entity.Parcela;
import br.com.arirang.plataforma.mapper.AlunoMapper;
import br.com.arirang.plataforma.repository.ParcelaRepository;
import java.math.BigDecimal;
import br.com.arirang.plataforma.service.AlunoService;
import br.com.arirang.plataforma.service.ContratoService;
import br.com.arirang.plataforma.service.PagamentoService;
import br.com.arirang.plataforma.service.ReceitaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/financeiro")
public class FinanceiroController {

    private static final Logger logger = LoggerFactory.getLogger(FinanceiroController.class);

    @Autowired
    private ReceitaService receitaService;

    @Autowired
    private PagamentoService pagamentoService;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private ContratoService contratoService;
    
    @Autowired
    private br.com.arirang.plataforma.service.MensalidadeService mensalidadeService;
    
    @Autowired
    private ParcelaRepository parcelaRepository;
    
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
     * Dashboard financeiro principal
     */
    @GetMapping
    public String dashboardFinanceiro(Model model) {
        try {
            // Estatísticas de mensalidades (sincronizado com financeiro-mensalidades)
            Map<String, Object> estatisticasMensalidades = mensalidadeService.calcularEstatisticasMensalidades();
            model.addAttribute("estatisticasMensalidades", estatisticasMensalidades);
            
            // Estatísticas de pagamentos (para o card de Total Pago no mês)
            Map<String, Object> estatisticasPagamentos = pagamentoService.calcularEstatisticasPagamentos();
            model.addAttribute("estatisticasPagamentos", estatisticasPagamentos);
            
            // Receitas vencidas (para a tabela de alertas)
            List<ReceitaDTO> receitasVencidas = receitaService.listarReceitasVencidas();
            model.addAttribute("receitasVencidas", receitasVencidas);
            
            return "financeiro-dashboard";
            
        } catch (Exception e) {
            logger.error("Erro ao carregar dashboard financeiro: ", e);
            model.addAttribute("error", "Erro ao carregar dashboard: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Lista todas as receitas
     */
    @GetMapping("/receitas")
    public String listarReceitas(@RequestParam(value = "alunoId", required = false) Long alunoId,
                                @RequestParam(value = "contratoId", required = false) Long contratoId,
                                @RequestParam(value = "situacao", required = false) String situacao,
                                @RequestParam(value = "tipoReceita", required = false) String tipoReceita,
                                @RequestParam(value = "dataInicio", required = false) String dataInicio,
                                @RequestParam(value = "dataFim", required = false) String dataFim,
                                @RequestParam(value = "search", required = false) String search,
                                Model model) {
        try {
            List<ReceitaDTO> receitas;
            
            if (search != null && !search.trim().isEmpty()) {
                // Busca simples por nome do aluno
                receitas = receitaService.listarTodasReceitas()
                        .stream()
                        .filter(r -> r.alunoNome().toLowerCase().contains(search.toLowerCase()))
                        .toList();
            } else {
                LocalDate inicio = dataInicio != null ? LocalDate.parse(dataInicio) : null;
                LocalDate fim = dataFim != null ? LocalDate.parse(dataFim) : null;
                
                receitas = receitaService.buscarReceitasComFiltros(alunoId, contratoId, situacao, tipoReceita, inicio, fim);
            }
            
            model.addAttribute("receitas", receitas);
            model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
            model.addAttribute("contratos", contratoService.listarTodosContratos());
            model.addAttribute("alunoSelecionado", alunoId);
            model.addAttribute("contratoSelecionado", contratoId);
            model.addAttribute("situacaoSelecionada", situacao);
            model.addAttribute("tipoReceitaSelecionado", tipoReceita);
            model.addAttribute("dataInicio", dataInicio);
            model.addAttribute("dataFim", dataFim);
            model.addAttribute("searchTerm", search);
            
        } catch (Exception e) {
            logger.error("Erro ao carregar receitas: ", e);
            model.addAttribute("error", "Erro ao carregar receitas: " + e.getMessage());
            model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
            model.addAttribute("contratos", contratoService.listarTodosContratos());
        }
        
        return "financeiro-receitas";
    }

    /**
     * Lista todos os pagamentos
     */
    @GetMapping("/pagamentos")
    public String listarPagamentos(@RequestParam(value = "alunoId", required = false) Long alunoId,
                                  @RequestParam(value = "formaPagamento", required = false) String formaPagamento,
                                  @RequestParam(value = "dataInicio", required = false) String dataInicio,
                                  @RequestParam(value = "dataFim", required = false) String dataFim,
                                  @RequestParam(value = "search", required = false) String search,
                                  Model model) {
        try {
            List<PagamentoDTO> pagamentos;
            
            if (search != null && !search.trim().isEmpty()) {
                // Busca simples por nome do aluno
                pagamentos = pagamentoService.listarTodosPagamentos()
                        .stream()
                        .filter(p -> p.alunoNome() != null && p.alunoNome().toLowerCase().contains(search.toLowerCase()))
                        .toList();
            } else {
                LocalDate inicio = dataInicio != null ? LocalDate.parse(dataInicio) : null;
                LocalDate fim = dataFim != null ? LocalDate.parse(dataFim) : null;
                
                pagamentos = pagamentoService.buscarPagamentosComFiltros(alunoId, formaPagamento, inicio, fim);
            }
            
            model.addAttribute("pagamentos", pagamentos);
            model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
            model.addAttribute("alunoSelecionado", alunoId);
            model.addAttribute("formaPagamentoSelecionada", formaPagamento);
            model.addAttribute("dataInicio", dataInicio);
            model.addAttribute("dataFim", dataFim);
            model.addAttribute("searchTerm", search);
            
        } catch (Exception e) {
            logger.error("Erro ao carregar pagamentos: ", e);
            model.addAttribute("error", "Erro ao carregar pagamentos: " + e.getMessage());
            model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
        }
        
        return "financeiro-pagamentos";
    }

    /**
     * Formulário para novo pagamento
     */
    @GetMapping("/pagamentos/novo")
    public String novoPagamentoForm(@RequestParam(value = "receitaId", required = false) Long receitaId,
                                   @RequestParam(value = "parcelaId", required = false) Long parcelaId,
                                   Model model) {
        try {
            // Sempre adicionar lista de alunos para seleção (convertido para DTO para evitar referências circulares)
            model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
            
            // Se veio de mensalidades (com parcelaId), buscar a parcela diretamente do repositório
            if (parcelaId != null) {
                var parcelaOpt = parcelaRepository.findByIdWithContrato(parcelaId);
                
                if (parcelaOpt.isPresent()) {
                    var parcela = parcelaOpt.get();
                    
                    // Garantir que o contrato e aluno estão carregados
                    if (parcela.getContrato() == null || parcela.getContrato().getAluno() == null) {
                        model.addAttribute("error", "Dados da mensalidade incompletos");
                        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                        return "financeiro-pagamento-form";
                    }
                    
                    // Obter valores diretamente da entidade
                    java.math.BigDecimal valorParcela = parcela.getValorParcela();
                    if (valorParcela == null) {
                        logger.warn("Valor da parcela é null para parcela ID: {}", parcelaId);
                        valorParcela = java.math.BigDecimal.ZERO;
                    }
                    
                    // Calcular valor com juros e multa usando método da entidade
                    java.math.BigDecimal valorFinal = parcela.getValorComJurosEMulta();
                    if (valorFinal == null || valorFinal.compareTo(java.math.BigDecimal.ZERO) == 0) {
                        valorFinal = valorParcela;
                    }
                    
                    logger.debug("Parcela encontrada - ID: {}, Valor Parcela: {}, Valor Final: {}", 
                            parcelaId, valorParcela, valorFinal);
                    
                    // Criar uma receita DTO temporária baseada na parcela
                    ReceitaDTO receitaTemp = ReceitaDTO.of(
                            null, // id
                            parcela.getContrato().getId(), // contratoId
                            parcela.getContrato().getNumeroContrato(), // contratoNumero
                            parcela.getContrato().getAluno().getId(), // alunoId
                            parcela.getContrato().getAluno().getNomeCompleto(), // alunoNome
                            "MENSALIDADE", // tipoReceita
                            "Mensalidade " + parcela.getNumeroParcela() + 
                            (parcela.getContrato().getTurma() != null ? " - " + parcela.getContrato().getTurma().getNomeTurma() : ""), // descricao
                            valorParcela, // valorOriginal
                            parcela.getDescontoAplicado() != null ? parcela.getDescontoAplicado() : java.math.BigDecimal.ZERO, // valorDesconto
                            parcela.getJurosAplicados() != null ? parcela.getJurosAplicados() : java.math.BigDecimal.ZERO, // valorJuros
                            valorFinal, // valorFinal
                            parcela.getDataVencimento(), // dataVencimento
                            parcela.getDataPagamento(), // dataPagamento
                            parcela.getStatusParcela() != null ? parcela.getStatusParcela().name() : "PENDENTE", // situacao
                            parcela.getNumeroParcela(), // numeroParcela
                            null, // totalParcelas
                            parcela.getObservacoes(), // observacoes
                            null, // dataCriacao
                            null, // dataAtualizacao
                            null, // usuarioCriacao
                            null, // diasAtraso
                            valorFinal // valorRestante
                    );
                    
                    model.addAttribute("receita", receitaTemp);
                    model.addAttribute("parcelaId", parcelaId);
                    alunoService.buscarAlunoPorId(parcela.getContrato().getAluno().getId())
                            .ifPresent(a -> model.addAttribute("aluno", a));
                    
                    PagamentoDTO pagamento = PagamentoDTO.createNew(
                            null, // receitaId será criado depois ou será null
                            valorFinal, // valorPago pré-preenchido
                            LocalDate.now(), 
                            "PIX"
                    );
                    model.addAttribute("pagamento", pagamento);
                } else {
                    model.addAttribute("error", "Mensalidade não encontrada");
                }
            } 
            // Se veio com receitaId (do dashboard ou outra página)
            else if (receitaId != null) {
                var receita = receitaService.buscarReceitaPorId(receitaId);
                if (receita.isPresent()) {
                    var rec = receita.get();
                    model.addAttribute("receita", rec);
                    if (rec.alunoId() != null) {
                        alunoService.buscarAlunoPorId(rec.alunoId()).ifPresent(a -> model.addAttribute("aluno", a));
                    }
                    PagamentoDTO pagamento = PagamentoDTO.createNew(
                            receitaId, 
                            null, 
                            LocalDate.now(), 
                            "PIX"
                    );
                    model.addAttribute("pagamento", pagamento);
                } else {
                    model.addAttribute("error", "Receita não encontrada");
                }
            }
            // Se veio sem receitaId e sem parcelaId (do dashboard diretamente)
            else {
                PagamentoDTO pagamento = PagamentoDTO.createNew(
                        null, 
                        null, 
                        LocalDate.now(), 
                        "PIX"
                );
                model.addAttribute("pagamento", pagamento);
            }
            
            model.addAttribute("isNew", true);
            
        } catch (Exception e) {
            logger.error("Erro ao carregar formulário de pagamento: ", e);
            model.addAttribute("error", "Erro ao carregar formulário: " + e.getMessage());
            model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
        }
        
        return "financeiro-pagamento-form";
    }
    
    /**
     * Endpoint AJAX para buscar receitas pendentes de um aluno
     */
    @GetMapping("/receitas/aluno/{alunoId}")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> buscarReceitasPorAluno(@PathVariable Long alunoId) {
        try {
            List<Map<String, Object>> receitas = receitaService.listarReceitasPorAluno(alunoId)
                    .stream()
                    .filter(r -> r != null && r.situacao() != null && 
                            ("PENDENTE".equals(r.situacao()) || "VENCIDO".equals(r.situacao()) || "PARCIAL".equals(r.situacao())))
                    .map(r -> {
                        // Criar um Map simples para evitar qualquer referência circular
                        Map<String, Object> receitaMap = new java.util.HashMap<>();
                        receitaMap.put("id", r.id());
                        receitaMap.put("contratoId", r.contratoId());
                        receitaMap.put("contratoNumero", r.contratoNumero());
                        receitaMap.put("alunoId", r.alunoId());
                        receitaMap.put("alunoNome", r.alunoNome());
                        receitaMap.put("tipoReceita", r.tipoReceita());
                        receitaMap.put("descricao", r.descricao());
                        receitaMap.put("valorOriginal", r.valorOriginal());
                        receitaMap.put("valorDesconto", r.valorDesconto());
                        receitaMap.put("valorJuros", r.valorJuros());
                        receitaMap.put("valorFinal", r.valorFinal());
                        receitaMap.put("dataVencimento", r.dataVencimento() != null ? r.dataVencimento().toString() : null);
                        receitaMap.put("dataPagamento", r.dataPagamento() != null ? r.dataPagamento().toString() : null);
                        receitaMap.put("situacao", r.situacao());
                        receitaMap.put("numeroParcela", r.numeroParcela());
                        receitaMap.put("totalParcelas", r.totalParcelas());
                        receitaMap.put("observacoes", r.observacoes());
                        receitaMap.put("diasAtraso", r.diasAtraso());
                        receitaMap.put("valorRestante", r.valorRestante());
                        return receitaMap;
                    })
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(receitas);
        } catch (Exception e) {
            logger.error("Erro ao buscar receitas do aluno ID {}: ", alunoId, e);
            // Retornar lista vazia em caso de erro para não quebrar o frontend
            return ResponseEntity.ok(java.util.Collections.emptyList());
        }
    }

    /**
     * Cria novo pagamento.
     * NOT_SUPPORTED evita tx request-level que, ao falhar sync/core, gerava "rollback-only".
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @PostMapping("/pagamentos")
    public String criarPagamento(@ModelAttribute("pagamento") PagamentoDTO pagamento,
                                @RequestParam(value = "parcelaId", required = false) Long parcelaId,
                                @RequestParam(value = "tipoPagamento", required = false) String tipoPagamento,
                                @RequestParam(value = "alunoId", required = false) Long alunoId,
                                @RequestParam(value = "descontoPercentual", required = false) java.math.BigDecimal descontoPercentualParam,
                                @RequestParam(value = "descontoValor", required = false) java.math.BigDecimal descontoValorParam,
                                BindingResult bindingResult, Model model) {
        try {
            logger.debug("Criando pagamento - parcelaId recebido: {}, receitaId do DTO: {}", parcelaId, pagamento.receitaId());
            
            // Se veio de mensalidade (parcelaId), criar ou buscar receita relacionada ANTES da validação
            Long receitaIdFinal = pagamento.receitaId();
            
            if (parcelaId != null && receitaIdFinal == null) {
                try {
                    // Buscar parcela com contrato e aluno carregados
                    var parcelaOpt = parcelaRepository.findByIdWithContrato(parcelaId);
                    
                    if (!parcelaOpt.isPresent()) {
                        model.addAttribute("error", "Mensalidade não encontrada");
                        model.addAttribute("isNew", true);
                        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                        return "financeiro-pagamento-form";
                    }
                    
                    var parcela = parcelaOpt.get();
                    
                    // Forçar carregamento do contrato e aluno
                    if (parcela.getContrato() == null) {
                        model.addAttribute("error", "Contrato da mensalidade não encontrado");
                        model.addAttribute("isNew", true);
                        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                        return "financeiro-pagamento-form";
                    }
                    
                    // Acessar o aluno para forçar carregamento
                    if (parcela.getContrato().getAluno() == null) {
                        model.addAttribute("error", "Aluno do contrato não encontrado");
                        model.addAttribute("isNew", true);
                        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                        return "financeiro-pagamento-form";
                    }
                    
                    Long contratoId = parcela.getContrato().getId();
                    Long alunoIdParcela = parcela.getContrato().getAluno().getId();
                    
                    // Buscar receita existente para este contrato e número de parcela
                    var receitas = receitaService.listarReceitasPorContrato(contratoId);
                    var receitaExistente = receitas.stream()
                            .filter(r -> r.numeroParcela() != null && r.numeroParcela().equals(parcela.getNumeroParcela()))
                            .findFirst();
                    
                    if (receitaExistente.isPresent()) {
                        receitaIdFinal = receitaExistente.get().id();
                        logger.debug("Receita existente encontrada: {}", receitaIdFinal);
                    } else {
                        // Criar nova receita baseada na parcela
                        java.math.BigDecimal valorParcela = parcela.getValorParcela();
                        if (valorParcela == null) {
                            valorParcela = java.math.BigDecimal.ZERO;
                        }
                        
                        logger.debug("Criando nova receita para parcela ID: {}, Contrato: {}, Aluno: {}", 
                                parcelaId, contratoId, alunoIdParcela);
                        
                        ReceitaDTO novaReceita = ReceitaDTO.createNew(
                                contratoId, 
                                alunoIdParcela, 
                                "MENSALIDADE",
                                valorParcela, 
                                parcela.getDataVencimento(), 
                                parcela.getNumeroParcela(), 
                                null
                        );
                        // Permitir data passada para receitas retroativas de parcelas vencidas
                        novaReceita = receitaService.criarReceita(novaReceita, true);
                        receitaIdFinal = novaReceita.id();
                        logger.debug("Receita criada com sucesso: {}", receitaIdFinal);
                    }
                } catch (Exception e) {
                    logger.error("Erro ao processar parcela ID {}: ", parcelaId, e);
                    model.addAttribute("error", "Erro ao processar mensalidade: " + e.getMessage());
                    model.addAttribute("isNew", true);
                    model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                    return "financeiro-pagamento-form";
                }
            }
            
            // Se não há receitaId nem parcelaId, mas tem tipoPagamento e alunoId, criar receita automaticamente
            if (receitaIdFinal == null && tipoPagamento != null && !tipoPagamento.trim().isEmpty() && alunoId != null) {
                try {
                    // Buscar contrato ativo do aluno
                    var contratos = contratoService.listarContratosPorAluno(alunoId);
                    var contratoAtivo = contratos.stream()
                            .filter(c -> "ATIVO".equals(c.situacaoContrato()))
                            .findFirst();
                    
                    if (contratoAtivo.isEmpty()) {
                        bindingResult.rejectValue("receitaId", "NotFound", 
                            "Aluno não possui contrato ativo. Por favor, crie um contrato antes de registrar o pagamento.");
                        logger.warn("Tentativa de criar pagamento sem contrato ativo para aluno ID: {}", alunoId);
                        model.addAttribute("isNew", true);
                        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                        return "financeiro-pagamento-form";
                    }
                    
                    var contrato = contratoAtivo.get();
                    java.math.BigDecimal valorPago = pagamento.valorPago();
                    if (valorPago == null || valorPago.compareTo(java.math.BigDecimal.ZERO) <= 0) {
                        bindingResult.rejectValue("valorPago", "NotNull", 
                            "Valor pago é obrigatório e deve ser maior que zero.");
                        model.addAttribute("isNew", true);
                        model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                        return "financeiro-pagamento-form";
                    }
                    
                    // Criar receita com o tipo selecionado
                    ReceitaDTO novaReceita = ReceitaDTO.createNew(
                            contrato.id(), 
                            alunoId, 
                            tipoPagamento,
                            valorPago, 
                            pagamento.dataPagamento() != null ? pagamento.dataPagamento() : java.time.LocalDate.now(), 
                            null, // numeroParcela
                            null  // totalParcelas
                    );
                    novaReceita = receitaService.criarReceita(novaReceita, true);
                    receitaIdFinal = novaReceita.id();
                    logger.debug("Receita criada automaticamente com tipo {}: {}", tipoPagamento, receitaIdFinal);
                } catch (Exception e) {
                    logger.error("Erro ao criar receita automaticamente: ", e);
                    bindingResult.rejectValue("receitaId", "Error", 
                        "Erro ao criar receita: " + e.getMessage());
                    model.addAttribute("isNew", true);
                    model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                    return "financeiro-pagamento-form";
                }
            }
            
            // Validar se receitaId foi encontrado/criado ANTES de tentar buscar
            if (receitaIdFinal == null) {
                bindingResult.rejectValue("receitaId", "NotNull", 
                    "Receita é obrigatória. Por favor, selecione uma receita ou mensalidade antes de registrar o pagamento.");
                logger.warn("Tentativa de criar pagamento sem receitaId e sem parcelaId");
                model.addAttribute("isNew", true);
                model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                // Tentar recuperar dados do formulário para exibição
                if (pagamento.receitaId() != null) {
                    var receitaParaExibicao = receitaService.buscarReceitaPorId(pagamento.receitaId());
                    if (receitaParaExibicao.isPresent()) {
                        model.addAttribute("receita", receitaParaExibicao.get());
                    }
                } else if (parcelaId != null) {
                    model.addAttribute("parcelaId", parcelaId);
                    var parcelaOpt = parcelaRepository.findByIdWithContrato(parcelaId);
                    if (parcelaOpt.isPresent()) {
                        var parcela = parcelaOpt.get();
                        if (parcela.getContrato() != null && parcela.getContrato().getAluno() != null) {
                            java.math.BigDecimal valorParcela = parcela.getValorParcela();
                            if (valorParcela == null) {
                                valorParcela = java.math.BigDecimal.ZERO;
                            }
                            java.math.BigDecimal valorFinal = parcela.getValorComJurosEMulta();
                            if (valorFinal == null || valorFinal.compareTo(java.math.BigDecimal.ZERO) == 0) {
                                valorFinal = valorParcela;
                            }
                            ReceitaDTO receitaTemp = ReceitaDTO.of(
                                    null, // id
                                    parcela.getContrato().getId(), // contratoId
                                    parcela.getContrato().getNumeroContrato(), // contratoNumero
                                    parcela.getContrato().getAluno().getId(), // alunoId
                                    parcela.getContrato().getAluno().getNomeCompleto(), // alunoNome
                                    "MENSALIDADE", // tipoReceita
                                    "Mensalidade " + parcela.getNumeroParcela(), // descricao
                                    valorParcela, // valorOriginal
                                    parcela.getDescontoAplicado() != null ? parcela.getDescontoAplicado() : java.math.BigDecimal.ZERO, // valorDesconto
                                    parcela.getJurosAplicados() != null ? parcela.getJurosAplicados() : java.math.BigDecimal.ZERO, // valorJuros
                                    valorFinal, // valorFinal
                                    parcela.getDataVencimento(), // dataVencimento
                                    parcela.getDataPagamento(), // dataPagamento
                                    "PENDENTE", // situacao
                                    parcela.getNumeroParcela(), // numeroParcela
                                    null, // totalParcelas
                                    parcela.getObservacoes(), // observacoes
                                    null, // dataCriacao
                                    null, // dataAtualizacao
                                    null, // usuarioCriacao
                                    null, // diasAtraso
                                    valorFinal // valorRestante
                            );
                            model.addAttribute("receita", receitaTemp);
                        }
                    }
                }
                return "financeiro-pagamento-form";
            }
            
            // Buscar receita para calcular isParcial e isIntegral
            var receitaOpt = receitaService.buscarReceitaPorId(receitaIdFinal);
            if (!receitaOpt.isPresent()) {
                bindingResult.rejectValue("receitaId", "NotFound", "Receita não encontrada");
                model.addAttribute("isNew", true);
                model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                return "financeiro-pagamento-form";
            }
            
            var receitaParaCalculo = receitaOpt.get();
            java.math.BigDecimal valorPago = pagamento.valorPago();
            java.math.BigDecimal valorRestante = receitaParaCalculo.valorRestante();
            
            // Calcular se é parcial ou integral
            Boolean isParcial = null;
            Boolean isIntegral = null;
            if (valorPago != null && valorRestante != null) {
                boolean desconto100 = false;
                if (valorPago.compareTo(java.math.BigDecimal.ZERO) == 0) {
                    var pct = descontoPercentualParam;
                    var vDesc = descontoValorParam;
                    if (pct != null && pct.compareTo(java.math.BigDecimal.valueOf(100)) >= 0) {
                        desconto100 = true;
                    } else if (vDesc != null && valorRestante.compareTo(java.math.BigDecimal.ZERO) > 0
                            && vDesc.compareTo(valorRestante) >= 0) {
                        desconto100 = true;
                    }
                }
                if (desconto100 || (valorPago.compareTo(valorRestante) >= 0)) {
                    isParcial = false;
                    isIntegral = true;
                } else if (valorPago.compareTo(valorRestante) < 0) {
                    isParcial = true;
                    isIntegral = false;
                } else {
                    isParcial = false;
                    isIntegral = true;
                }
            } else {
                isParcial = pagamento.isParcial();
                isIntegral = pagamento.isIntegral();
            }
            
            // Criar novo PagamentoDTO com receitaId preenchido para validação
            PagamentoDTO pagamentoValidado = new PagamentoDTO(
                    pagamento.id(),
                    receitaIdFinal,
                    receitaParaCalculo.descricao(),
                    receitaParaCalculo.alunoId(),
                    receitaParaCalculo.alunoNome(),
                    valorPago,
                    pagamento.dataPagamento(),
                    pagamento.formaPagamento(),
                    pagamento.numeroTransacao(),
                    pagamento.observacoes(),
                    pagamento.comprovanteCaminho(),
                    pagamento.dataCriacao(),
                    pagamento.dataAtualizacao(),
                    pagamento.usuarioPagamento(),
                    valorRestante,
                    isParcial,
                    isIntegral,
                    descontoPercentualParam,
                    descontoValorParam
            );
            
            // Validar manualmente (receitaId já foi validado acima)
            if (pagamentoValidado.valorPago() == null) {
                bindingResult.rejectValue("valorPago", "NotNull", "Valor pago é obrigatório");
            }
            if (pagamentoValidado.dataPagamento() == null) {
                bindingResult.rejectValue("dataPagamento", "NotNull", "Data do pagamento é obrigatória");
            }
            if (pagamentoValidado.formaPagamento() == null || pagamentoValidado.formaPagamento().trim().isEmpty()) {
                bindingResult.rejectValue("formaPagamento", "NotNull", "Forma de pagamento é obrigatória");
            }
            
            if (bindingResult.hasErrors()) {
                logger.warn("Erros de validação ao criar pagamento: {}", bindingResult.getAllErrors());
                logger.debug("parcelaId durante validação: {}", parcelaId);
                model.addAttribute("isNew", true);
                model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                if (receitaIdFinal != null) {
                    var receitaParaExibicao = receitaService.buscarReceitaPorId(receitaIdFinal);
                    model.addAttribute("receita", receitaParaExibicao.orElse(null));
                } else if (parcelaId != null) {
                    model.addAttribute("parcelaId", parcelaId);
                    // Recarregar dados da parcela para exibição
                    var parcelaOpt = parcelaRepository.findByIdWithContrato(parcelaId);
                    if (parcelaOpt.isPresent()) {
                        var parcela = parcelaOpt.get();
                        
                        // Verificar se contrato e aluno estão carregados
                        if (parcela.getContrato() == null || parcela.getContrato().getAluno() == null) {
                            logger.warn("Contrato ou aluno não encontrado para parcela ID: {}", parcelaId);
                            return "financeiro-pagamento-form";
                        }
                        
                        java.math.BigDecimal valorParcela = parcela.getValorParcela();
                        if (valorParcela == null) {
                            valorParcela = java.math.BigDecimal.ZERO;
                        }
                        java.math.BigDecimal valorFinal = parcela.getValorComJurosEMulta();
                        if (valorFinal == null || valorFinal.compareTo(java.math.BigDecimal.ZERO) == 0) {
                            valorFinal = valorParcela;
                        }
                        
                        ReceitaDTO receitaTemp = ReceitaDTO.of(
                                null, parcela.getContrato().getId(), parcela.getContrato().getNumeroContrato(),
                                parcela.getContrato().getAluno().getId(), parcela.getContrato().getAluno().getNomeCompleto(),
                                "MENSALIDADE", "Mensalidade " + parcela.getNumeroParcela() + 
                                (parcela.getContrato().getTurma() != null ? " - " + parcela.getContrato().getTurma().getNomeTurma() : ""),
                                valorParcela, parcela.getDescontoAplicado() != null ? parcela.getDescontoAplicado() : java.math.BigDecimal.ZERO,
                                parcela.getJurosAplicados() != null ? parcela.getJurosAplicados() : java.math.BigDecimal.ZERO,
                                valorFinal, parcela.getDataVencimento(), parcela.getDataPagamento(),
                                parcela.getStatusParcela() != null ? parcela.getStatusParcela().name() : "PENDENTE",
                                parcela.getNumeroParcela(), null, parcela.getObservacoes(), null, null, null, null, valorFinal
                        );
                        model.addAttribute("receita", receitaTemp);
                        model.addAttribute("parcelaId", parcelaId);
                    }
                }
                return "financeiro-pagamento-form";
            }
            
            // Registrar pagamento com receitaId preenchido
            logger.debug("Registrando pagamento com receitaId: {}, valorPago: {}, isParcial: {}, isIntegral: {}", 
                    receitaIdFinal, pagamentoValidado.valorPago(), pagamentoValidado.isParcial(), pagamentoValidado.isIntegral());
            
            try {
                pagamentoService.registrarPagamento(pagamentoValidado);
                logger.info("Pagamento registrado com sucesso. ID da receita: {}, parcelaId: {}", receitaIdFinal, parcelaId);
                
                // Redirecionar baseado na origem: se veio de mensalidade (parcelaId), vai para mensalidades; senão, vai para pagamentos
                String redirectUrl;
                if (parcelaId != null && parcelaId > 0) {
                    redirectUrl = "redirect:/financeiro/mensalidades?success=Pagamento registrado com sucesso";
                    logger.debug("Redirecionando para mensalidades (parcelaId: {})", parcelaId);
                } else {
                    redirectUrl = "redirect:/financeiro/pagamentos?success=Pagamento registrado com sucesso";
                    logger.debug("Redirecionando para pagamentos (parcelaId não fornecido)");
                }
                return redirectUrl;
            } catch (Exception ex) {
                logger.error("Erro ao registrar pagamento no serviço: ", ex);
                throw ex; // Re-lançar para ser capturado pelo catch externo
            }
        } catch (Exception e) {
            logger.error("Erro ao criar pagamento - parcelaId: {}, receitaId: {}: ", parcelaId, pagamento.receitaId(), e);
            model.addAttribute("error", "Erro ao criar pagamento: " + e.getMessage());
            model.addAttribute("isNew", true);
            model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
            if (parcelaId != null) {
                model.addAttribute("parcelaId", parcelaId);
            }
            
            // Tentar recarregar dados para exibição
            try {
                if (parcelaId != null) {
                    var parcelaOpt = parcelaRepository.findByIdWithContrato(parcelaId);
                    if (parcelaOpt.isPresent()) {
                        var parcela = parcelaOpt.get();
                        
                        // Verificar se contrato e aluno estão carregados
                        if (parcela.getContrato() == null || parcela.getContrato().getAluno() == null) {
                            logger.warn("Contrato ou aluno não encontrado para parcela ID: {}", parcelaId);
                            return "financeiro-pagamento-form";
                        }
                        
                        java.math.BigDecimal valorParcela = parcela.getValorParcela();
                        if (valorParcela == null) {
                            valorParcela = java.math.BigDecimal.ZERO;
                        }
                        java.math.BigDecimal valorFinal = parcela.getValorComJurosEMulta();
                        if (valorFinal == null || valorFinal.compareTo(java.math.BigDecimal.ZERO) == 0) {
                            valorFinal = valorParcela;
                        }
                        
                        ReceitaDTO receitaTemp = ReceitaDTO.of(
                                null, parcela.getContrato().getId(), parcela.getContrato().getNumeroContrato(),
                                parcela.getContrato().getAluno().getId(), parcela.getContrato().getAluno().getNomeCompleto(),
                                "MENSALIDADE", "Mensalidade " + parcela.getNumeroParcela() + 
                                (parcela.getContrato().getTurma() != null ? " - " + parcela.getContrato().getTurma().getNomeTurma() : ""),
                                valorParcela, parcela.getDescontoAplicado() != null ? parcela.getDescontoAplicado() : java.math.BigDecimal.ZERO,
                                parcela.getJurosAplicados() != null ? parcela.getJurosAplicados() : java.math.BigDecimal.ZERO,
                                valorFinal, parcela.getDataVencimento(), parcela.getDataPagamento(),
                                parcela.getStatusParcela() != null ? parcela.getStatusParcela().name() : "PENDENTE",
                                parcela.getNumeroParcela(), null, parcela.getObservacoes(), null, null, null, null, valorFinal
                        );
                        model.addAttribute("receita", receitaTemp);
                        model.addAttribute("parcelaId", parcelaId);
                    }
                } else if (pagamento.receitaId() != null) {
                    var receita = receitaService.buscarReceitaPorId(pagamento.receitaId());
                    model.addAttribute("receita", receita.orElse(null));
                }
            } catch (Exception ex) {
                logger.error("Erro ao recarregar dados para exibição: ", ex);
            }
            
            return "financeiro-pagamento-form";
        }
    }

    /**
     * Gera receitas automaticamente de um contrato
     */
    @PostMapping("/receitas/gerar/{contratoId}")
    public String gerarReceitasContrato(@PathVariable Long contratoId) {
        try {
            receitaService.gerarReceitasDoContrato(contratoId);
            return "redirect:/financeiro/receitas?success=Receitas geradas com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao gerar receitas para contrato ID {}: ", contratoId, e);
            return "redirect:/financeiro/receitas?error=" + e.getMessage();
        }
    }

    /**
     * Gera carnê de pagamentos para um aluno
     */
    @GetMapping("/carne/{alunoId}")
    public String gerarCarneAluno(@PathVariable Long alunoId, Model model) {
        try {
            var aluno = alunoService.buscarAlunoPorId(alunoId);
            var receitas = receitaService.listarReceitasPorAluno(alunoId);
            
            model.addAttribute("aluno", aluno.orElse(null));
            model.addAttribute("receitas", receitas);
            return "financeiro-carne";
            
        } catch (Exception e) {
            logger.error("Erro ao gerar carnê para aluno ID {}: ", alunoId, e);
            model.addAttribute("error", "Erro ao gerar carnê: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Visualiza receita específica
     */
    @GetMapping("/receitas/{id}")
    public String visualizarReceita(@PathVariable Long id, Model model) {
        try {
            var receita = receitaService.buscarReceitaPorId(id);
            if (receita.isPresent()) {
                var pagamentos = pagamentoService.listarPagamentosPorReceita(id);
                
                model.addAttribute("receita", receita.get());
                model.addAttribute("pagamentos", pagamentos);
                return "financeiro-receita-view";
            } else {
                model.addAttribute("error", "Receita não encontrada");
                return "error";
            }
        } catch (Exception e) {
            logger.error("Erro ao visualizar receita ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar receita: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Visualiza detalhes de um pagamento
     */
    @GetMapping("/pagamentos/{id}")
    public String visualizarPagamento(@PathVariable Long id, Model model) {
        try {
            var pagamentoOpt = pagamentoService.buscarPagamentoPorId(id);
            if (pagamentoOpt.isEmpty()) {
                model.addAttribute("error", "Pagamento não encontrado");
                return "error";
            }
            PagamentoDTO pagamento = pagamentoOpt.get();
            model.addAttribute("pagamento", pagamento);
            if (pagamento.alunoId() != null) {
                alunoService.buscarAlunoPorId(pagamento.alunoId()).ifPresent(a -> model.addAttribute("aluno", a));
            }
            return "financeiro-pagamento-view";
        } catch (Exception e) {
            logger.error("Erro ao visualizar pagamento ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar pagamento: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Formulário para editar pagamento
     */
    @GetMapping("/pagamentos/editar/{id}")
    public String editarPagamentoForm(@PathVariable Long id, Model model) {
        try {
            var pagamentoOpt = pagamentoService.buscarPagamentoPorId(id);
            if (pagamentoOpt.isPresent()) {
                model.addAttribute("pagamento", pagamentoOpt.get());
                model.addAttribute("isNew", false);
                model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
                return "financeiro-pagamento-form";
            } else {
                model.addAttribute("error", "Pagamento não encontrado");
                return "error";
            }
        } catch (Exception e) {
            logger.error("Erro ao carregar pagamento para edição ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar pagamento: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Confirmação para deletar pagamento
     */
    @GetMapping("/pagamentos/deletar/{id}")
    public String deletarPagamentoConfirm(@PathVariable Long id, Model model) {
        try {
            var pagamentoOpt = pagamentoService.buscarPagamentoPorId(id);
            PagamentoDTO pagamento = pagamentoOpt.orElse(null);
            model.addAttribute("pagamento", pagamento);
            if (pagamento != null && pagamento.alunoId() != null) {
                alunoService.buscarAlunoPorId(pagamento.alunoId()).ifPresent(a -> model.addAttribute("aluno", a));
            }
            return "financeiro-pagamento-delete";
        } catch (Exception e) {
            logger.error("Erro ao carregar pagamento para deleção ID {}: ", id, e);
            model.addAttribute("error", "Erro ao carregar pagamento: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Deleta pagamento
     */
    @PostMapping("/pagamentos/deletar/{id}")
    public String deletarPagamento(@PathVariable Long id) {
        try {
            pagamentoService.deletarPagamento(id);
            return "redirect:/financeiro/pagamentos?success=Pagamento deletado com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao deletar pagamento ID {}: ", id, e);
            return "redirect:/financeiro/pagamentos?error=" + e.getMessage();
        }
    }

    /**
     * Gera comprovante de pagamento
     */
    @GetMapping("/pagamentos/{id}/comprovante")
    public String gerarComprovantePagamento(@PathVariable Long id, Model model) {
        try {
            // Buscar pagamento com todas as relações
            var pagamentoOpt = pagamentoService.buscarPagamentoComRelacoes(id);
            if (!pagamentoOpt.isPresent()) {
                model.addAttribute("error", "Pagamento não encontrado");
                return "error";
            }
            
            Pagamento pagamento = pagamentoOpt.get();
            Receita receita = pagamento.getReceita();
            
            if (receita == null) {
                model.addAttribute("error", "Receita do pagamento não encontrada");
                return "error";
            }
            
            Contrato contrato = receita.getContrato();
            Aluno aluno = receita.getAluno();
            Turma turma = contrato != null ? contrato.getTurma() : null;
            
            // Buscar parcela relacionada se houver
            Parcela parcela = null;
            if (contrato != null && receita.getNumeroParcela() != null) {
                var parcelas = parcelaRepository.findByContratoId(contrato.getId());
                parcela = parcelas.stream()
                    .filter(p -> p.getNumeroParcela().equals(receita.getNumeroParcela()))
                    .findFirst()
                    .orElse(null);
            }
            
            // Calcular valores
            // O valor base é o valor da parcela/receita sem multa e juros
            BigDecimal valorBase = receita.getValorOriginal();
            if (parcela != null && parcela.getValorParcela() != null) {
                valorBase = parcela.getValorParcela();
            }
            
            BigDecimal valorPago = pagamento.getValorPago();
            BigDecimal valorOriginal = receita.getValorOriginal();
            BigDecimal valorDesconto = receita.getValorDesconto() != null ? receita.getValorDesconto() : BigDecimal.ZERO;
            
            // Desconto no pagamento (pontual): priorizar sobre o da receita
            if (pagamento.getDescontoPercentual() != null && pagamento.getDescontoPercentual().compareTo(BigDecimal.ZERO) > 0) {
                valorDesconto = valorBase.multiply(pagamento.getDescontoPercentual()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            } else if (pagamento.getDescontoValor() != null && pagamento.getDescontoValor().compareTo(BigDecimal.ZERO) > 0) {
                valorDesconto = pagamento.getDescontoValor().min(valorBase);
            }
            
            // Buscar juros - priorizar parcela, depois receita
            BigDecimal valorJuros = BigDecimal.ZERO;
            if (parcela != null && parcela.getJurosAplicados() != null && parcela.getJurosAplicados().compareTo(BigDecimal.ZERO) > 0) {
                valorJuros = parcela.getJurosAplicados();
            } else if (receita.getValorJuros() != null && receita.getValorJuros().compareTo(BigDecimal.ZERO) > 0) {
                valorJuros = receita.getValorJuros();
            }
            
            // Buscar multa - priorizar parcela
            BigDecimal multa = BigDecimal.ZERO;
            if (parcela != null && parcela.getMultaAplicada() != null && parcela.getMultaAplicada().compareTo(BigDecimal.ZERO) > 0) {
                multa = parcela.getMultaAplicada();
            }
            
            // Calcular desconto percentual para exibição
            BigDecimal descontoPercentual = BigDecimal.ZERO;
            if (valorDesconto.compareTo(BigDecimal.ZERO) > 0 && valorOriginal.compareTo(BigDecimal.ZERO) > 0) {
                descontoPercentual = valorDesconto.divide(valorOriginal, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            }
            
            // Para o comprovante, o VALOR é o valor base (sem multa/juros) menos desconto
            // O TOTAL é valor base - desconto + multa + juros
            BigDecimal valorParaExibicao = valorBase.subtract(valorDesconto).max(BigDecimal.ZERO);
            BigDecimal valorTotal = valorParaExibicao;
            if (multa.compareTo(BigDecimal.ZERO) > 0) {
                valorTotal = valorTotal.add(multa);
            }
            if (valorJuros.compareTo(BigDecimal.ZERO) > 0) {
                valorTotal = valorTotal.add(valorJuros);
            }
            
            // Adicionar atributos ao modelo
            model.addAttribute("pagamento", pagamento);
            model.addAttribute("receita", receita);
            model.addAttribute("contrato", contrato);
            model.addAttribute("aluno", aluno);
            model.addAttribute("turma", turma);
            model.addAttribute("parcela", parcela);
            model.addAttribute("valorPago", valorPago);
            model.addAttribute("valorBase", valorParaExibicao); // Valor base para exibição (sem multa/juros)
            model.addAttribute("valorOriginal", valorOriginal);
            model.addAttribute("valorDesconto", valorDesconto);
            model.addAttribute("valorJuros", valorJuros);
            model.addAttribute("multa", multa);
            model.addAttribute("descontoPercentual", descontoPercentual);
            model.addAttribute("valorTotal", valorTotal);
            
            // Nome do responsável financeiro (se houver)
            String responsavelFinanceiroNome = "";
            if (aluno != null && aluno.getResponsavel() != null) {
                responsavelFinanceiroNome = aluno.getResponsavel().getNomeCompleto();
            } else if (aluno != null && aluno.isResponsavelFinanceiro() && aluno.getNomeCompleto() != null) {
                responsavelFinanceiroNome = aluno.getNomeCompleto();
            }
            model.addAttribute("responsavelFinanceiroNome", responsavelFinanceiroNome);
            
            // CPF do responsável financeiro
            String responsavelFinanceiroCpf = "";
            if (aluno != null && aluno.getResponsavel() != null && aluno.getResponsavel().getCpf() != null) {
                responsavelFinanceiroCpf = aluno.getResponsavel().getCpf();
            } else if (aluno != null && aluno.isResponsavelFinanceiro() && aluno.getCpf() != null) {
                responsavelFinanceiroCpf = aluno.getCpf();
            }
            model.addAttribute("responsavelFinanceiroCpf", responsavelFinanceiroCpf);
            
            return "financeiro-comprovante-pagamento";
            
        } catch (Exception e) {
            logger.error("Erro ao gerar comprovante de pagamento ID {}: ", id, e);
            model.addAttribute("error", "Erro ao gerar comprovante: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Lista todas as mensalidades (parcelas)
     */
    @GetMapping("/mensalidades")
    public String listarMensalidades(@RequestParam(value = "alunoId", required = false) Long alunoId,
                                    @RequestParam(value = "contratoId", required = false) Long contratoId,
                                    @RequestParam(value = "status", required = false) String status,
                                    @RequestParam(value = "dataInicio", required = false) String dataInicio,
                                    @RequestParam(value = "dataFim", required = false) String dataFim,
                                    @RequestParam(value = "search", required = false) String search,
                                    Model model) {
        try {
            List<br.com.arirang.plataforma.dto.MensalidadeDTO> mensalidades;
            
            if (search != null && !search.trim().isEmpty()) {
                // Busca simples por nome do aluno - apenas pendentes
                mensalidades = mensalidadeService.listarMensalidadesPorStatus(br.com.arirang.plataforma.entity.StatusParcela.PENDENTE)
                        .stream()
                        .filter(m -> m.alunoNome() != null && m.alunoNome().toLowerCase().contains(search.toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
                
                // Adicionar também mensalidades em atraso que correspondem à busca
                List<br.com.arirang.plataforma.dto.MensalidadeDTO> emAtraso = mensalidadeService.listarMensalidadesPorStatus(br.com.arirang.plataforma.entity.StatusParcela.EM_ATRASO)
                        .stream()
                        .filter(m -> m.alunoNome() != null && m.alunoNome().toLowerCase().contains(search.toLowerCase()))
                        .collect(java.util.stream.Collectors.toList());
                mensalidades.addAll(emAtraso);
            } else if (alunoId != null) {
                // Filtrar apenas pendentes e em atraso
                mensalidades = mensalidadeService.listarMensalidadesPorAluno(alunoId)
                        .stream()
                        .filter(m -> m.statusParcela() != null && 
                                (m.statusParcela() == br.com.arirang.plataforma.entity.StatusParcela.PENDENTE || 
                                 m.statusParcela() == br.com.arirang.plataforma.entity.StatusParcela.EM_ATRASO))
                        .collect(java.util.stream.Collectors.toList());
            } else if (contratoId != null) {
                // Filtrar apenas pendentes e em atraso
                mensalidades = mensalidadeService.listarMensalidadesPorContrato(contratoId)
                        .stream()
                        .filter(m -> m.statusParcela() != null && 
                                (m.statusParcela() == br.com.arirang.plataforma.entity.StatusParcela.PENDENTE || 
                                 m.statusParcela() == br.com.arirang.plataforma.entity.StatusParcela.EM_ATRASO))
                        .collect(java.util.stream.Collectors.toList());
            } else if (status != null && !status.trim().isEmpty()) {
                try {
                    br.com.arirang.plataforma.entity.StatusParcela statusEnum = 
                            br.com.arirang.plataforma.entity.StatusParcela.valueOf(status);
                    // Permitir exibir todos os status, incluindo PAGAS
                    mensalidades = mensalidadeService.listarMensalidadesPorStatus(statusEnum);
                } catch (IllegalArgumentException e) {
                    // Se status inválido, mostrar apenas pendentes
                    mensalidades = mensalidadeService.listarMensalidadesPorStatus(br.com.arirang.plataforma.entity.StatusParcela.PENDENTE);
                    List<br.com.arirang.plataforma.dto.MensalidadeDTO> emAtraso = mensalidadeService.listarMensalidadesPorStatus(br.com.arirang.plataforma.entity.StatusParcela.EM_ATRASO);
                    mensalidades.addAll(emAtraso);
                }
            } else if (dataInicio != null && dataFim != null) {
                LocalDate inicio = LocalDate.parse(dataInicio);
                LocalDate fim = LocalDate.parse(dataFim);
                // Filtrar apenas pendentes e em atraso
                mensalidades = mensalidadeService.listarMensalidadesPorPeriodo(inicio, fim)
                        .stream()
                        .filter(m -> m.statusParcela() != null && 
                                (m.statusParcela() == br.com.arirang.plataforma.entity.StatusParcela.PENDENTE || 
                                 m.statusParcela() == br.com.arirang.plataforma.entity.StatusParcela.EM_ATRASO))
                        .collect(java.util.stream.Collectors.toList());
            } else {
                // Por padrão, mostrar apenas pendentes e em atraso
                mensalidades = mensalidadeService.listarMensalidadesPorStatus(br.com.arirang.plataforma.entity.StatusParcela.PENDENTE);
                List<br.com.arirang.plataforma.dto.MensalidadeDTO> emAtraso = mensalidadeService.listarMensalidadesPorStatus(br.com.arirang.plataforma.entity.StatusParcela.EM_ATRASO);
                mensalidades.addAll(emAtraso);
            }
            
            // Estatísticas (apenas para referência, não afeta a listagem)
            java.util.Map<String, Object> estatisticas = mensalidadeService.calcularEstatisticasMensalidades();
            
            model.addAttribute("mensalidades", mensalidades);
            model.addAttribute("estatisticas", estatisticas);
            model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
            model.addAttribute("contratos", contratoService.listarTodosContratos());
            model.addAttribute("alunoSelecionado", alunoId);
            model.addAttribute("contratoSelecionado", contratoId);
            model.addAttribute("statusSelecionado", status);
            model.addAttribute("dataInicio", dataInicio);
            model.addAttribute("dataFim", dataFim);
            model.addAttribute("searchTerm", search);
            
        } catch (Exception e) {
            logger.error("Erro ao carregar mensalidades: ", e);
            model.addAttribute("error", "Erro ao carregar mensalidades: " + e.getMessage());
            model.addAttribute("alunos", convertAlunosToDTO(alunoService.listarTodosAlunos()));
            model.addAttribute("contratos", contratoService.listarTodosContratos());
        }
        
        return "financeiro-mensalidades";
    }
    
    /**
     * Endpoint para sincronizar status das parcelas (corrigir parcelas pagas que aparecem como pendentes)
     */
    @PostMapping("/mensalidades/sincronizar")
    public String sincronizarParcelas() {
        try {
            pagamentoService.sincronizarStatusParcelas();
            return "redirect:/financeiro/mensalidades?success=Status das parcelas sincronizado com sucesso";
        } catch (Exception e) {
            logger.error("Erro ao sincronizar parcelas: ", e);
            return "redirect:/financeiro/mensalidades?error=Erro ao sincronizar: " + e.getMessage();
        }
    }
}
