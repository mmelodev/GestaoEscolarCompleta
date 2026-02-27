package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.ContratoDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Contrato;
import br.com.arirang.plataforma.entity.Turma;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.mapper.ContratoMapper;
import br.com.arirang.plataforma.entity.Parcela;
import br.com.arirang.plataforma.entity.StatusParcela;
import br.com.arirang.plataforma.entity.Pagamento;
import br.com.arirang.plataforma.entity.Receita;
import br.com.arirang.plataforma.entity.ComprovantePagamento;
import br.com.arirang.plataforma.entity.Financeiro;
import br.com.arirang.plataforma.entity.TipoMovimentoFinanceiro;
import br.com.arirang.plataforma.entity.CategoriaFinanceira;
import br.com.arirang.plataforma.repository.ContratoRepository;
import br.com.arirang.plataforma.repository.ParcelaRepository;
import br.com.arirang.plataforma.repository.PagamentoRepository;
import br.com.arirang.plataforma.repository.ReceitaRepository;
import br.com.arirang.plataforma.repository.ComprovantePagamentoRepository;
import br.com.arirang.plataforma.repository.FinanceiroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ContratoService {

    private static final Logger logger = LoggerFactory.getLogger(ContratoService.class);

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private TurmaService turmaService;

    @Autowired
    private ContratoMapper contratoMapper;

    @Autowired
    private ParcelaRepository parcelaRepository;
    
    @Autowired
    private PagamentoRepository pagamentoRepository;
    
    @Autowired
    private ReceitaRepository receitaRepository;
    
    @Autowired
    private ComprovantePagamentoRepository comprovantePagamentoRepository;
    
    @Autowired
    private FinanceiroRepository financeiroRepository;

    /**
     * Lista todos os contratos
     */
    @Transactional(readOnly = true)
    public List<ContratoDTO> listarTodosContratos() {
        logger.debug("Listando todos os contratos");
        return contratoRepository.findAll()
                .stream()
                .map(contratoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca contrato por ID
     */
    @Transactional(readOnly = true)
    public Optional<ContratoDTO> buscarContratoPorId(Long id) {
        logger.debug("Buscando contrato por ID: {}", id);
        return contratoRepository.findById(id)
                .map(contratoMapper::toDto);
    }

    /**
     * Busca entidade Contrato por ID com relacionamentos carregados (para PDFs)
     */
    @Transactional(readOnly = true)
    public Optional<Contrato> buscarContratoEntityPorId(Long id) {
        logger.debug("Buscando entidade contrato por ID: {}", id);
        return contratoRepository.findById(id)
                .map(contrato -> {
                    // Forçar carregamento dos relacionamentos
                    if (contrato.getAluno() != null) {
                        contrato.getAluno().getNomeCompleto(); // Trigger lazy load
                        if (contrato.getAluno().getResponsavel() != null) {
                            contrato.getAluno().getResponsavel().getNomeCompleto(); // Trigger lazy load
                        }
                        if (contrato.getAluno().getEndereco() != null) {
                            contrato.getAluno().getEndereco().getLogradouro(); // Trigger lazy load
                        }
                        // Forçar carregamento das turmas do aluno
                        if (contrato.getAluno().getTurmas() != null) {
                            contrato.getAluno().getTurmas().size(); // Trigger lazy load
                        }
                    }
                    if (contrato.getTurma() != null) {
                        contrato.getTurma().getNomeTurma(); // Trigger lazy load
                    }
                    return contrato;
                });
    }

    /**
     * Cria contrato sem validações de duplicatas (para casos especiais)
     * ✅ NOVA REGRA: Bypass de todas as validações de negócio
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
        
        // Garantir template PDF válido (criação sem UI)
        if (contrato.getTemplatePdf() == null || contrato.getTemplatePdf().trim().isEmpty()) {
            contrato.setTemplatePdf("contrato-curso");
        }
        
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

        // Criar receita total no dashboard financeiro
        criarReceitaTotalNoDashboard(contratoSalvo);

        return contratoMapper.toDto(contratoSalvo);
    }

    /**
     * Lista contratos por aluno
     */
    @Transactional(readOnly = true)
    public List<ContratoDTO> listarContratosPorAluno(Long alunoId) {
        logger.debug("Listando contratos por aluno ID: {}", alunoId);
        return contratoRepository.findByAlunoIdOrderByDataCriacaoDesc(alunoId)
                .stream()
                .map(contratoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista contratos por turma
     */
    @Transactional(readOnly = true)
    public List<ContratoDTO> listarContratosPorTurma(Long turmaId) {
        logger.debug("Listando contratos por turma ID: {}", turmaId);
        return contratoRepository.findByTurmaIdOrderByDataCriacaoDesc(turmaId)
                .stream()
                .map(contratoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca contratos ativos
     */
    @Transactional(readOnly = true)
    public List<ContratoDTO> buscarContratosAtivos() {
        logger.debug("Buscando contratos ativos");
        return contratoRepository.findBySituacaoContratoOrderByDataCriacaoDesc("ATIVO")
                .stream()
                .map(contratoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Cria um novo contrato
     */
    @Transactional
    public ContratoDTO criarContrato(ContratoDTO contratoDTO) {
        logger.debug("Criando novo contrato para aluno ID: {} e turma ID: {}", 
                    contratoDTO.alunoId(), contratoDTO.turmaId());

        // Validações de negócio
        validarCriacaoContrato(contratoDTO);

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
        logger.info("Contrato criado com sucesso. ID: {}, Número: {}", 
                   contratoSalvo.getId(), contratoSalvo.getNumeroContrato());

        // Gerar parcelas automaticamente se houver valor de mensalidade e número de parcelas
        if (contratoSalvo.getValorMensalidade() != null 
                && contratoSalvo.getValorMensalidade().compareTo(BigDecimal.ZERO) > 0
                && contratoSalvo.getNumeroParcelas() != null 
                && contratoSalvo.getNumeroParcelas() > 0) {
            gerarParcelasAutomaticamente(contratoSalvo);
        }

        // Criar receita total no dashboard financeiro
        criarReceitaTotalNoDashboard(contratoSalvo);

        return contratoMapper.toDto(contratoSalvo);
    }

    /**
     * Atualiza um contrato existente
     */
    public ContratoDTO atualizarContrato(Long id, ContratoDTO contratoDTO) {
        logger.debug("Atualizando contrato ID: {}", id);

        Contrato contratoExistente = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + id));

        // Atualizar campos permitidos
        contratoExistente.setDataContrato(contratoDTO.dataContrato());
        contratoExistente.setDataInicioVigencia(contratoDTO.dataInicioVigencia());
        contratoExistente.setDataFimVigencia(contratoDTO.dataFimVigencia());
        contratoExistente.setValorMatricula(contratoDTO.valorMatricula());
        contratoExistente.setValorMensalidade(contratoDTO.valorMensalidade());
        contratoExistente.setNumeroParcelas(contratoDTO.numeroParcelas());
        contratoExistente.setDescontoValor(contratoDTO.descontoValor());
        contratoExistente.setDescontoPercentual(contratoDTO.descontoPercentual());
        contratoExistente.setObservacoes(contratoDTO.observacoes());
        contratoExistente.setSituacaoContrato(contratoDTO.situacaoContrato());
        contratoExistente.setTemplatePdf(contratoDTO.templatePdf());
        
        // Atualizar aluno e turma se fornecidos (permitir alteração)
        if (contratoDTO.alunoId() != null) {
            Aluno aluno = alunoService.buscarAlunoPorId(contratoDTO.alunoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + contratoDTO.alunoId()));
            contratoExistente.setAluno(aluno);
        }
        
        if (contratoDTO.turmaId() != null) {
            Turma turma = turmaService.buscarTurmaPorId(contratoDTO.turmaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + contratoDTO.turmaId()));
            contratoExistente.setTurma(turma);
        }

        // Recalcular valor total
        contratoExistente.setValorTotalContrato(calcularValorTotalContrato(contratoExistente));

        Contrato contratoAtualizado = contratoRepository.save(contratoExistente);
        logger.info("Contrato atualizado com sucesso. ID: {}", contratoAtualizado.getId());

        return contratoMapper.toDto(contratoAtualizado);
    }

    /**
     * Deleta um contrato (SEM validar pagamentos/parcelas)
     * ✅ NOVA REGRA: Não valida pagamentos/parcelas
     */
    public void deletarContrato(Long id) {
        logger.debug("Deletando contrato ID: {}", id);

        Contrato contrato = contratoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + id));

        // ✅ NOVA REGRA: Não validar pagamentos/parcelas
        // Removida validação de contratos cancelados
        // Removida validação de parcelas existentes
        
        // Deletar em ordem (respeitando dependências):
        // 1. Comprovantes vinculados às parcelas
        // 2. Pagamentos vinculados às receitas
        // 3. Receitas vinculadas ao contrato
        // 4. Parcelas vinculadas ao contrato
        // 5. Financeiro vinculado ao contrato
        // 6. Comprovantes vinculados diretamente ao contrato
        
        List<Parcela> parcelas = parcelaRepository.findByContratoId(id);
        
        // Deletar comprovantes das parcelas
        for (Parcela parcela : parcelas) {
            List<ComprovantePagamento> comprovantes = 
                comprovantePagamentoRepository.findByParcelaId(parcela.getId());
            if (!comprovantes.isEmpty()) {
                logger.info("Deletando {} comprovantes da parcela ID {}", comprovantes.size(), parcela.getId());
                comprovantePagamentoRepository.deleteAll(comprovantes);
            }
            
            // Deletar financeiro vinculado às parcelas (IMPORTANTE: antes de deletar as parcelas)
            List<Financeiro> financeirosParcela = 
                financeiroRepository.findByParcelaId(parcela.getId());
            if (!financeirosParcela.isEmpty()) {
                logger.info("Deletando {} registros financeiros da parcela ID {}", financeirosParcela.size(), parcela.getId());
                financeiroRepository.deleteAll(financeirosParcela);
            }
        }
        
        // Deletar receitas do contrato (e seus pagamentos)
        List<Receita> receitas = receitaRepository.findByContratoIdOrderByDataVencimentoAsc(id);
        for (Receita receita : receitas) {
            // Deletar pagamentos da receita
            List<Pagamento> pagamentos = 
                pagamentoRepository.findByReceitaIdOrderByDataPagamentoDesc(receita.getId());
            if (!pagamentos.isEmpty()) {
                logger.info("Deletando {} pagamentos da receita ID {}", pagamentos.size(), receita.getId());
                pagamentoRepository.deleteAll(pagamentos);
            }
        }
        if (!receitas.isEmpty()) {
            logger.info("Deletando {} receitas do contrato ID {}", receitas.size(), id);
            receitaRepository.deleteAll(receitas);
        }
        
        // Deletar financeiro vinculado diretamente ao contrato
        List<Financeiro> financeirosContrato = 
            financeiroRepository.findByContratoId(id);
        if (!financeirosContrato.isEmpty()) {
            logger.info("Deletando {} registros financeiros do contrato ID {}", financeirosContrato.size(), id);
            financeiroRepository.deleteAll(financeirosContrato);
        }
        
        // Deletar parcelas (agora que não há mais referências)
        if (!parcelas.isEmpty()) {
            logger.info("Deletando {} parcelas do contrato ID {}", parcelas.size(), id);
            parcelaRepository.deleteAll(parcelas);
        }
        
        // Deletar comprovantes vinculados diretamente ao contrato
        List<ComprovantePagamento> comprovantesContrato = 
            comprovantePagamentoRepository.findByContratoId(id);
        if (!comprovantesContrato.isEmpty()) {
            logger.info("Deletando {} comprovantes do contrato ID {}", comprovantesContrato.size(), id);
            comprovantePagamentoRepository.deleteAll(comprovantesContrato);
        }

        // Deletar contrato
        contratoRepository.delete(contrato);
        logger.info("Contrato deletado com sucesso. ID: {}", id);
    }

    /**
     * Busca contratos com filtros
     */
    @Transactional(readOnly = true)
    public List<ContratoDTO> buscarContratosComFiltros(Long alunoId, Long turmaId, String situacao, String numeroContrato) {
        logger.debug("Buscando contratos com filtros - Aluno: {}, Turma: {}, Situação: {}, Número: {}", 
                    alunoId, turmaId, situacao, numeroContrato);

        try {
            return contratoRepository.findContratosWithFilters(alunoId, turmaId, situacao, numeroContrato)
                    .stream()
                    .filter(contrato -> contrato.getAluno() != null && contrato.getAluno().getId() != null &&
                                       contrato.getTurma() != null && contrato.getTurma().getId() != null)
                    .map(contrato -> {
                        try {
                            return contratoMapper.toDto(contrato);
                        } catch (Exception e) {
                            logger.warn("Erro ao converter contrato ID {} para DTO: {}", contrato.getId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(dto -> dto != null && dto.alunoId() != null && dto.turmaId() != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao buscar contratos com filtros: ", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gera contrato baseado em aluno e turma (para criação rápida)
     */
    public ContratoDTO gerarContratoRapido(Long alunoId, Long turmaId) {
        logger.debug("Gerando contrato rápido para aluno ID: {} e turma ID: {}", alunoId, turmaId);

        // Buscar aluno e turma
        alunoService.buscarAlunoPorId(alunoId)
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + alunoId));

        Turma turma = turmaService.buscarTurmaPorId(turmaId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + turmaId));

        // Verificar se já existe contrato ativo
        if (contratoRepository.existsByAlunoIdAndTurmaIdAndSituacaoContrato(alunoId, turmaId, "ATIVO")) {
            throw new BusinessException("Já existe um contrato ativo para este aluno nesta turma");
        }

        // Criar DTO com dados básicos
        ContratoDTO contratoDTO = ContratoDTO.createNew(alunoId, turmaId,
                LocalDate.now(),
                turma.getInicioTurma() != null ? turma.getInicioTurma() : LocalDate.now(),
                turma.getTerminoTurma() != null ? turma.getTerminoTurma() : LocalDate.now().plusMonths(6));

        // Criação automática: definir um template padrão válido
        return criarContrato(new ContratoDTO(
                contratoDTO.id(),
                contratoDTO.alunoId(),
                contratoDTO.alunoNome(),
                contratoDTO.turmaId(),
                contratoDTO.turmaNome(),
                contratoDTO.numeroContrato(),
                contratoDTO.dataContrato(),
                contratoDTO.dataInicioVigencia(),
                contratoDTO.dataFimVigencia(),
                contratoDTO.valorMatricula(),
                contratoDTO.valorMensalidade(),
                contratoDTO.numeroParcelas(),
                contratoDTO.descontoValor(),
                contratoDTO.descontoPercentual(),
                contratoDTO.valorTotalContrato(),
                contratoDTO.observacoes(),
                contratoDTO.situacaoContrato(),
                "contrato-curso",
                contratoDTO.dataCriacao(),
                contratoDTO.dataAtualizacao()
        ));
    }

    /**
     * Validações para criação de contrato
     */
    private void validarCriacaoContrato(ContratoDTO contratoDTO) {
        // Tipo de contrato (template PDF) é obrigatório e limitado aos 4 templates válidos
        List<String> templatesValidos = List.of(
                "contrato-curso",
                "contrato-servicos-menor",
                "uso-imagem-adulto",
                "uso-imagem-menor"
        );
        if (contratoDTO.templatePdf() == null || contratoDTO.templatePdf().trim().isEmpty()) {
            throw new BusinessException("Tipo de contrato é obrigatório. Selecione um dos 4 modelos válidos.");
        }
        if (!templatesValidos.contains(contratoDTO.templatePdf().trim())) {
            throw new BusinessException("Tipo de contrato inválido: " + contratoDTO.templatePdf());
        }

        // Validar datas
        validarDatas(contratoDTO);
        
        // Validar se a turma existe e está ativa
        Turma turma = turmaService.buscarTurmaPorId(contratoDTO.turmaId())
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com ID: " + contratoDTO.turmaId()));
        
        // Validar se turma não está fechada (permitir criar contrato apenas para turmas ativas)
        if ("FECHADA".equalsIgnoreCase(turma.getSituacaoTurma())) {
            throw new BusinessException("Não é possível criar contrato para uma turma fechada. Reabra a turma primeiro.");
        }
        
        // Validar se não existe contrato ativo duplicado para mesmo aluno/turma
        if (contratoRepository.existsByAlunoIdAndTurmaIdAndSituacaoContrato(
                contratoDTO.alunoId(), contratoDTO.turmaId(), "ATIVO")) {
            throw new BusinessException("Já existe um contrato ATIVO para este aluno nesta turma. Cancele ou suspenda o contrato existente antes de criar um novo.");
        }
    }
    
    /**
     * Valida as datas do contrato
     */
    private void validarDatas(ContratoDTO contratoDTO) {
        // Validar data de contrato não é futura
        if (contratoDTO.dataContrato() != null && contratoDTO.dataContrato().isAfter(LocalDate.now())) {
            throw new BusinessException("Data do contrato não pode ser futura.");
        }
        
        // Validar data de início de vigência
        if (contratoDTO.dataInicioVigencia() == null) {
            throw new BusinessException("Data de início de vigência é obrigatória.");
        }
        
        // Validar data de fim de vigência
        if (contratoDTO.dataFimVigencia() == null) {
            throw new BusinessException("Data de fim de vigência é obrigatória.");
        }
        
        // Validar data de fim é posterior à data de início
        if (contratoDTO.dataFimVigencia().isBefore(contratoDTO.dataInicioVigencia())) {
            throw new BusinessException("Data de fim de vigência deve ser posterior à data de início de vigência.");
        }
        
        // Validar se data de início não é muito antiga (opcional - mais de 1 ano)
        if (contratoDTO.dataInicioVigencia().isBefore(LocalDate.now().minusYears(1))) {
            logger.warn("Data de início de vigência muito antiga: {}", contratoDTO.dataInicioVigencia());
            // Não bloqueia, apenas registra warning
        }
    }

    /**
     * Gera número único do contrato baseado no mês/ano atual
     * Formato: CTRYYYYMM####
     * Thread-safe: busca o maior número existente e incrementa sequencialmente
     */
    @Transactional
    private String gerarNumeroContrato() {
        LocalDate hoje = LocalDate.now();
        String ano = String.valueOf(hoje.getYear());
        String mes = String.format("%02d", hoje.getMonthValue());
        String prefixo = "CTR" + ano + mes;
        
        // Buscar números de contrato do mês atual que seguem o padrão (ordenados do maior para o menor)
        List<String> numerosExistentes = contratoRepository.findNumeroContratosByPrefixo(prefixo + "%");
        
        // Extrair o maior número sequencial do mês atual
        long proximoNumero = 1; // Começar do 1 se não houver contratos no mês
        
        if (!numerosExistentes.isEmpty()) {
            // Como a query retorna ordenado do maior para o menor, pegar o primeiro
            // e iterar apenas se necessário (caso o primeiro não seja válido)
            for (String numero : numerosExistentes) {
                try {
                    // Validar formato: deve ter pelo menos 11 caracteres (CTR + 4 ano + 2 mês + 4 sequencial)
                    if (numero != null && numero.length() >= 11 && numero.startsWith(prefixo)) {
                        // Extrair os últimos 4 dígitos (número sequencial)
                        String sequencialStr = numero.substring(numero.length() - 4);
                        long sequencial = Long.parseLong(sequencialStr);
                        proximoNumero = sequencial + 1;
                        break; // Encontrou o maior número válido, pode parar
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Número de contrato com formato inválido ignorado: {}", numero);
                    // Continuar para o próximo número na lista
                }
            }
        }
        
        // Tentar gerar número único (com retry para evitar race conditions)
        String numeroContrato;
        int tentativas = 0;
        int maxTentativas = 100; // Aumentado para permitir mais tentativas
        
        do {
            // Garantir que não ultrapasse o limite de 4 dígitos (9999)
            if (proximoNumero > 9999) {
                throw new BusinessException("Limite de contratos do mês atingido (9999). Não é possível gerar mais números.");
            }
            
            numeroContrato = String.format("CTR%s%s%04d", ano, mes, proximoNumero);
            
            // Verificar se número já existe (pode ter sido criado por outra thread)
            Optional<Contrato> contratoExistente = contratoRepository.findByNumeroContrato(numeroContrato);
            if (contratoExistente.isEmpty()) {
                break; // Número disponível
            }
            
            tentativas++;
            proximoNumero++; // Tentar próximo número
            
            if (tentativas % 10 == 0) {
                logger.warn("Número de contrato {} já existe. Tentando próximo número... (tentativa {}/{})", 
                           numeroContrato, tentativas, maxTentativas);
            }
            
        } while (tentativas < maxTentativas);
        
        if (tentativas >= maxTentativas) {
            throw new BusinessException("Não foi possível gerar número único de contrato após " + maxTentativas + " tentativas. Tente novamente.");
        }
        
        logger.debug("Número de contrato gerado: {} (tentativas: {})", numeroContrato, tentativas);
        return numeroContrato;
    }

    /**
     * Calcula valor total do contrato
     * Aplica descontos percentuais primeiro, depois descontos em valor
     */
    private BigDecimal calcularValorTotalContrato(Contrato contrato) {
        BigDecimal valorBase = BigDecimal.ZERO;
        
        // Somar valor da matrícula
        if (contrato.getValorMatricula() != null) {
            valorBase = valorBase.add(contrato.getValorMatricula());
        }
        
        // Somar valor das parcelas (mensalidade × número de parcelas)
        if (contrato.getValorMensalidade() != null 
                && contrato.getNumeroParcelas() != null 
                && contrato.getNumeroParcelas() > 0) {
            BigDecimal valorParcelas = contrato.getValorMensalidade()
                    .multiply(BigDecimal.valueOf(contrato.getNumeroParcelas()));
            valorBase = valorBase.add(valorParcelas);
        }
        
        // Aplicar desconto percentual primeiro (sobre o valor base)
        if (contrato.getDescontoPercentual() != null 
                && contrato.getDescontoPercentual().compareTo(BigDecimal.ZERO) > 0) {
            // Validar que desconto percentual não seja maior que 100%
            if (contrato.getDescontoPercentual().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BusinessException("Desconto percentual não pode ser maior que 100%");
            }
            
            BigDecimal descontoPercentual = valorBase
                    .multiply(contrato.getDescontoPercentual())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            valorBase = valorBase.subtract(descontoPercentual);
            
            logger.debug("Aplicado desconto percentual de {}%: R$ {}", 
                        contrato.getDescontoPercentual(), descontoPercentual);
        }
        
        // Aplicar desconto em valor depois (sobre o valor já com desconto percentual)
        if (contrato.getDescontoValor() != null 
                && contrato.getDescontoValor().compareTo(BigDecimal.ZERO) > 0) {
            valorBase = valorBase.subtract(contrato.getDescontoValor());
            
            logger.debug("Aplicado desconto em valor: R$ {}", contrato.getDescontoValor());
        }
        
        // Garantir que valor total não seja negativo
        BigDecimal valorTotal = valorBase.max(BigDecimal.ZERO);
        
        logger.debug("Valor total calculado: R$ {} (Matrícula: {}, Parcelas: {}, Desconto %: {}, Desconto R$: {})",
                    valorTotal,
                    contrato.getValorMatricula() != null ? contrato.getValorMatricula() : BigDecimal.ZERO,
                    contrato.getValorMensalidade() != null && contrato.getNumeroParcelas() != null 
                        ? contrato.getValorMensalidade().multiply(BigDecimal.valueOf(contrato.getNumeroParcelas()))
                        : BigDecimal.ZERO,
                    contrato.getDescontoPercentual() != null ? contrato.getDescontoPercentual() : BigDecimal.ZERO,
                    contrato.getDescontoValor() != null ? contrato.getDescontoValor() : BigDecimal.ZERO);
        
        return valorTotal;
    }

    /**
     * Gera parcelas automaticamente para um contrato
     * Sincroniza mensalidades com o contrato criado
     */
    private void gerarParcelasAutomaticamente(Contrato contrato) {
        try {
            // Verificar se já existem parcelas para este contrato
            List<Parcela> parcelasExistentes = parcelaRepository.findByContratoId(contrato.getId());
            if (!parcelasExistentes.isEmpty()) {
                logger.info("Contrato ID {} já possui {} parcelas. Não serão geradas novas parcelas.", 
                           contrato.getId(), parcelasExistentes.size());
                return;
            }

            // Calcular data de início para as parcelas (primeiro vencimento um mês após a data de início de vigência)
            // Se dataInicioVigencia não estiver definida, usar dataContrato como fallback
            LocalDate dataBase = contrato.getDataInicioVigencia() != null 
                    ? contrato.getDataInicioVigencia() 
                    : contrato.getDataContrato();
            LocalDate dataVencimento = dataBase.plusMonths(1);
            
            List<Parcela> parcelas = new ArrayList<>();
            
            for (int i = 1; i <= contrato.getNumeroParcelas(); i++) {
                Parcela parcela = new Parcela();
                parcela.setContrato(contrato);
                parcela.setNumeroParcela(i);
                
                // Primeira parcela: valor da matrícula + valor da mensalidade
                // Demais parcelas: apenas valor da mensalidade
                BigDecimal valorParcela;
                if (i == 1) {
                    // Primeira parcela inclui matrícula + mensalidade
                    valorParcela = contrato.getValorMensalidade();
                    if (contrato.getValorMatricula() != null) {
                        valorParcela = valorParcela.add(contrato.getValorMatricula());
                    }
                } else {
                    // Demais parcelas: apenas mensalidade
                    valorParcela = contrato.getValorMensalidade();
                }
                
                parcela.setValorParcela(valorParcela);
                parcela.setDataVencimento(dataVencimento);
                parcela.setStatusParcela(StatusParcela.PENDENTE);
                
                parcelas.add(parcela);
                
                // Próximo vencimento: um mês após o anterior
                dataVencimento = dataVencimento.plusMonths(1);
            }
            
            parcelaRepository.saveAll(parcelas);
            logger.info("Geradas {} parcelas automaticamente para contrato ID {}", 
                       parcelas.size(), contrato.getId());
                       
        } catch (Exception e) {
            logger.error("Erro ao gerar parcelas automaticamente para contrato ID {}: ", contrato.getId(), e);
            // Não lançar exceção para não impedir a criação do contrato
            // As parcelas podem ser geradas manualmente depois
        }
    }

    /**
     * Cria receita total do contrato no dashboard financeiro
     * Sincroniza o valor total do contrato com o sistema financeiro
     */
    private void criarReceitaTotalNoDashboard(Contrato contrato) {
        try {
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
                       
        } catch (Exception e) {
            logger.error("Erro ao criar receita total no dashboard para contrato ID {}: ", contrato.getId(), e);
            // Não lançar exceção para não impedir a criação do contrato
            // A receita pode ser criada manualmente depois
        }
    }

    /**
     * Migração: Atualiza datas de vencimento das parcelas dos contratos existentes
     * para usar dataInicioVigencia ao invés de dataContrato
     * 
     * Este método recalcula as datas das parcelas PENDENTES baseado em dataInicioVigencia
     * quando disponível, mantendo o intervalo mensal entre parcelas.
     * 
     * @return Número de contratos atualizados
     */
    @Transactional
    public int migrarParcelasParaDataInicioVigencia() {
        logger.info("Iniciando migração de parcelas para usar dataInicioVigencia");
        
        int contratosAtualizados = 0;
        int parcelasAtualizadas = 0;
        
        try {
            // Buscar todos os contratos que têm parcelas
            List<Contrato> contratos = contratoRepository.findAll();
            
            for (Contrato contrato : contratos) {
                // Buscar parcelas do contrato
                List<Parcela> parcelas = parcelaRepository.findByContratoIdOrderByDataVencimentoAsc(contrato.getId());
                
                if (parcelas.isEmpty()) {
                    continue; // Pular contratos sem parcelas
                }
                
                // Verificar se o contrato tem dataInicioVigencia e se é diferente de dataContrato
                LocalDate dataInicioVigencia = contrato.getDataInicioVigencia();
                LocalDate dataContrato = contrato.getDataContrato();
                
                // Se não tem dataInicioVigencia ou é igual a dataContrato, não precisa atualizar
                if (dataInicioVigencia == null || dataInicioVigencia.equals(dataContrato)) {
                    continue;
                }
                
                // Calcular nova data base (dataInicioVigencia + 1 mês para primeira parcela)
                LocalDate novaDataBase = dataInicioVigencia.plusMonths(1);
                
                // Atualizar apenas parcelas PENDENTES (não mexer nas pagas)
                boolean contratoFoiAtualizado = false;
                int parcelasAtualizadasContrato = 0;
                
                for (Parcela parcela : parcelas) {
                    // Apenas atualizar parcelas pendentes
                    if (parcela.getStatusParcela() == StatusParcela.PENDENTE) {
                        // Calcular nova data de vencimento baseada no número da parcela
                        LocalDate novaDataVencimento = novaDataBase.plusMonths(parcela.getNumeroParcela() - 1);
                        
                        // Só atualizar se a data for diferente
                        if (!parcela.getDataVencimento().equals(novaDataVencimento)) {
                            logger.debug("Atualizando parcela {} do contrato {}: {} -> {}", 
                                    parcela.getNumeroParcela(), contrato.getId(), 
                                    parcela.getDataVencimento(), novaDataVencimento);
                            
                            parcela.setDataVencimento(novaDataVencimento);
                            parcelaRepository.save(parcela);
                            parcelasAtualizadas++;
                            parcelasAtualizadasContrato++;
                            contratoFoiAtualizado = true;
                        }
                    }
                }
                
                if (contratoFoiAtualizado) {
                    contratosAtualizados++;
                    logger.info("Contrato ID {} atualizado: {} parcelas ajustadas", 
                            contrato.getId(), parcelasAtualizadasContrato);
                }
            }
            
            logger.info("Migração concluída: {} contratos atualizados, {} parcelas ajustadas", 
                    contratosAtualizados, parcelasAtualizadas);
            
            return contratosAtualizados;
            
        } catch (Exception e) {
            logger.error("Erro durante migração de parcelas: ", e);
            throw new BusinessException("Erro ao migrar parcelas: " + e.getMessage());
        }
    }

    /**
     * Migração: Atualiza valores das parcelas dos contratos existentes
     * para incluir matrícula na primeira parcela
     * 
     * Este método atualiza apenas parcelas PENDENTES:
     * - Primeira parcela: valorMatricula + valorMensalidade
     * - Demais parcelas: apenas valorMensalidade
     * 
     * Parcelas já pagas não são alteradas para preservar histórico.
     * 
     * @return Número de contratos atualizados
     */
    @Transactional
    public int migrarValoresParcelasParaIncluirMatricula() {
        logger.info("Iniciando migração de valores de parcelas para incluir matrícula na primeira parcela");
        
        int contratosAtualizados = 0;
        int parcelasAtualizadas = 0;
        
        try {
            // Buscar todos os contratos que têm parcelas
            List<Contrato> contratos = contratoRepository.findAll();
            
            for (Contrato contrato : contratos) {
                // Buscar parcelas do contrato ordenadas por número
                List<Parcela> parcelas = parcelaRepository.findByContratoIdOrderByDataVencimentoAsc(contrato.getId());
                
                if (parcelas.isEmpty()) {
                    continue; // Pular contratos sem parcelas
                }
                
                // Verificar se o contrato tem valores necessários
                if (contrato.getValorMensalidade() == null || contrato.getValorMensalidade().compareTo(BigDecimal.ZERO) <= 0) {
                    logger.debug("Contrato ID {} não tem valor de mensalidade válido. Pulando.", contrato.getId());
                    continue;
                }
                
                // Calcular valor esperado para primeira parcela
                BigDecimal valorEsperadoPrimeiraParcela = contrato.getValorMensalidade();
                if (contrato.getValorMatricula() != null && contrato.getValorMatricula().compareTo(BigDecimal.ZERO) > 0) {
                    valorEsperadoPrimeiraParcela = valorEsperadoPrimeiraParcela.add(contrato.getValorMatricula());
                }
                
                // Atualizar apenas parcelas PENDENTES (não mexer nas pagas)
                boolean contratoFoiAtualizado = false;
                int parcelasAtualizadasContrato = 0;
                
                for (Parcela parcela : parcelas) {
                    // Apenas atualizar parcelas pendentes
                    if (parcela.getStatusParcela() == StatusParcela.PENDENTE) {
                        BigDecimal novoValor;
                        
                        if (parcela.getNumeroParcela() == 1) {
                            // Primeira parcela: deve incluir matrícula + mensalidade
                            novoValor = valorEsperadoPrimeiraParcela;
                        } else {
                            // Demais parcelas: apenas mensalidade
                            novoValor = contrato.getValorMensalidade();
                        }
                        
                        // Só atualizar se o valor for diferente
                        if (!parcela.getValorParcela().equals(novoValor)) {
                            logger.debug("Atualizando parcela {} do contrato {}: R$ {} -> R$ {}", 
                                    parcela.getNumeroParcela(), contrato.getId(), 
                                    parcela.getValorParcela(), novoValor);
                            
                            parcela.setValorParcela(novoValor);
                            parcelaRepository.save(parcela);
                            parcelasAtualizadas++;
                            parcelasAtualizadasContrato++;
                            contratoFoiAtualizado = true;
                        }
                    }
                }
                
                if (contratoFoiAtualizado) {
                    contratosAtualizados++;
                    logger.info("Contrato ID {} atualizado: {} parcelas ajustadas", 
                            contrato.getId(), parcelasAtualizadasContrato);
                }
            }
            
            logger.info("Migração de valores concluída: {} contratos atualizados, {} parcelas ajustadas", 
                    contratosAtualizados, parcelasAtualizadas);
            
            return contratosAtualizados;
            
        } catch (Exception e) {
            logger.error("Erro durante migração de valores de parcelas: ", e);
            throw new BusinessException("Erro ao migrar valores de parcelas: " + e.getMessage());
        }
    }
}
