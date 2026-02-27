package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.ContratoDTO;
import br.com.arirang.plataforma.dto.ReceitaDTO;
import br.com.arirang.plataforma.entity.Aluno;
import br.com.arirang.plataforma.entity.Contrato;
import br.com.arirang.plataforma.entity.Receita;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.mapper.ReceitaMapper;
import br.com.arirang.plataforma.repository.ReceitaRepository;
import br.com.arirang.plataforma.repository.ContratoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReceitaService {

    private static final Logger logger = LoggerFactory.getLogger(ReceitaService.class);

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ContratoService contratoService;

    @Autowired
    private AlunoService alunoService;

    @Autowired
    private ReceitaMapper receitaMapper;

    /**
     * Lista todas as receitas (excluindo alunos deletados)
     */
    @Transactional(readOnly = true)
    public List<ReceitaDTO> listarTodasReceitas() {
        logger.debug("Listando todas as receitas");
        return receitaRepository.findAll()
                .stream()
                .filter(receita -> receita.getAluno() != null && receita.getAluno().getId() != null)
                .map(receitaMapper::toDto)
                .filter(dto -> dto != null && dto.alunoId() != null) // Filtra DTOs nulos ou com aluno deletado
                .collect(Collectors.toList());
    }

    /**
     * Busca receita por ID
     */
    @Transactional(readOnly = true)
    public Optional<ReceitaDTO> buscarReceitaPorId(Long id) {
        logger.debug("Buscando receita por ID: {}", id);
        return receitaRepository.findById(id)
                .map(receitaMapper::toDto);
    }

    /**
     * Lista receitas por aluno
     */
    @Transactional(readOnly = true)
    public List<ReceitaDTO> listarReceitasPorAluno(Long alunoId) {
        logger.debug("Listando receitas por aluno ID: {}", alunoId);
        return receitaRepository.findByAlunoIdOrderByDataVencimentoDesc(alunoId)
                .stream()
                .map(receitaMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista receitas por contrato
     */
    @Transactional(readOnly = true)
    public List<ReceitaDTO> listarReceitasPorContrato(Long contratoId) {
        logger.debug("Listando receitas por contrato ID: {}", contratoId);
        return receitaRepository.findByContratoIdOrderByDataVencimentoAsc(contratoId)
                .stream()
                .map(receitaMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista receitas por situação
     */
    @Transactional(readOnly = true)
    public List<ReceitaDTO> listarReceitasPorSituacao(String situacao) {
        logger.debug("Listando receitas por situação: {}", situacao);
        return receitaRepository.findBySituacaoOrderByDataVencimentoAsc(situacao)
                .stream()
                .map(receitaMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista receitas vencidas (excluindo alunos deletados)
     */
    @Transactional(readOnly = true)
    public List<ReceitaDTO> listarReceitasVencidas() {
        logger.debug("Listando receitas vencidas");
        return receitaRepository.findReceitasVencidas(LocalDate.now())
                .stream()
                .filter(receita -> receita.getAluno() != null && receita.getAluno().getId() != null)
                .map(receitaMapper::toDto)
                .filter(dto -> dto != null && dto.alunoId() != null) // Filtra DTOs nulos ou com aluno deletado
                .collect(Collectors.toList());
    }

    /**
     * Busca receitas com filtros
     */
    @Transactional(readOnly = true)
    public List<ReceitaDTO> buscarReceitasComFiltros(Long alunoId, Long contratoId, String situacao, 
                                                     String tipoReceita, LocalDate dataInicio, LocalDate dataFim) {
        logger.debug("Buscando receitas com filtros");
        return receitaRepository.findReceitasWithFilters(alunoId, contratoId, situacao, tipoReceita, dataInicio, dataFim)
                .stream()
                .map(receitaMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Cria nova receita
     */
    public ReceitaDTO criarReceita(ReceitaDTO receitaDTO) {
        return criarReceita(receitaDTO, false);
    }
    
    /**
     * Cria nova receita (com opção de permitir data passada para receitas retroativas)
     * @param receitaDTO DTO da receita
     * @param permitirDataPassada Se true, permite criar receitas com data de vencimento no passado
     */
    public ReceitaDTO criarReceita(ReceitaDTO receitaDTO, boolean permitirDataPassada) {
        logger.debug("Criando nova receita para contrato ID: {} (permitirDataPassada: {})", receitaDTO.contratoId(), permitirDataPassada);

        // Validações de negócio
        validarCriacaoReceita(receitaDTO, permitirDataPassada);

        // Buscar contrato
        if (receitaDTO.contratoId() == null) {
            throw new BusinessException("Contrato é obrigatório para criar receita");
        }
        
        Contrato contrato = contratoRepository.findById(receitaDTO.contratoId())
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + receitaDTO.contratoId()));

        // Buscar aluno
        Aluno aluno = alunoService.buscarAlunoPorId(receitaDTO.alunoId())
                .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado com ID: " + receitaDTO.alunoId()));

        // Criar entidade
        Receita receita = receitaMapper.toEntity(receitaDTO);
        receita.setContrato(contrato); // IMPORTANTE: Associar o contrato
        receita.setAluno(aluno);
        
        // Calcular valor final se não informado
        if (receita.getValorFinal() == null || receita.getValorFinal().compareTo(BigDecimal.ZERO) == 0) {
            receita.setValorFinal(calcularValorFinal(receita));
        }

        // Salvar receita
        Receita receitaSalva = receitaRepository.save(receita);
        logger.info("Receita criada com sucesso. ID: {}, Contrato ID: {}", receitaSalva.getId(), contrato.getId());

        return receitaMapper.toDto(receitaSalva);
    }

    /**
     * Gera receitas automaticamente baseadas no contrato
     */
    public List<ReceitaDTO> gerarReceitasDoContrato(Long contratoId) {
        logger.debug("Gerando receitas para contrato ID: {}", contratoId);

        // Buscar contrato
        ContratoDTO contratoDTO = contratoService.buscarContratoPorId(contratoId)
                .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado com ID: " + contratoId));

        // Verificar se já existem receitas para este contrato
        if (!receitaRepository.findByContratoIdOrderByDataVencimentoAsc(contratoId).isEmpty()) {
            throw new BusinessException("Já existem receitas geradas para este contrato");
        }

        // Gerar receitas de mensalidade se houver
        // NOTA: A primeira mensalidade já inclui o valor da matrícula
        // Não criar receita de matrícula separada
        if (contratoDTO.valorMensalidade() != null && contratoDTO.valorMensalidade().compareTo(BigDecimal.ZERO) > 0 
            && contratoDTO.numeroParcelas() != null && contratoDTO.numeroParcelas() > 0) {
            criarReceitasMensalidade(contratoDTO);
        }

        // Retornar todas as receitas criadas
        return listarReceitasPorContrato(contratoId);
    }

    /**
     * Atualiza receita
     */
    public ReceitaDTO atualizarReceita(Long id, ReceitaDTO receitaDTO) {
        logger.debug("Atualizando receita ID: {}", id);

        Receita receitaExistente = receitaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receita não encontrada com ID: " + id));

        // Atualizar campos permitidos
        receitaExistente.setDescricao(receitaDTO.descricao());
        receitaExistente.setValorDesconto(receitaDTO.valorDesconto());
        receitaExistente.setObservacoes(receitaDTO.observacoes());
        receitaExistente.setDataVencimento(receitaDTO.dataVencimento());

        // Recalcular valor final
        receitaExistente.setValorFinal(calcularValorFinal(receitaExistente));

        Receita receitaAtualizada = receitaRepository.save(receitaExistente);
        logger.info("Receita atualizada com sucesso. ID: {}", receitaAtualizada.getId());

        return receitaMapper.toDto(receitaAtualizada);
    }

    /**
     * Deleta receita
     */
    public void deletarReceita(Long id) {
        logger.debug("Deletando receita ID: {}", id);

        Receita receita = receitaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receita não encontrada com ID: " + id));

        // Verificar se pode ser deletada
        if ("PAGO".equals(receita.getSituacao())) {
            throw new BusinessException("Receitas pagas não podem ser deletadas");
        }

        receitaRepository.delete(receita);
        logger.info("Receita deletada com sucesso. ID: {}", id);
    }

    /**
     * Calcula estatísticas financeiras
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> calcularEstatisticasFinanceiras() {
        java.util.Map<String, Object> estatisticas = new java.util.HashMap<>();
        
        // Valores por situação
        estatisticas.put("totalPendente", receitaRepository.sumReceitasBySituacao("PENDENTE").orElse(0.0));
        estatisticas.put("totalPago", receitaRepository.sumReceitasBySituacao("PAGO").orElse(0.0));
        estatisticas.put("totalVencido", receitaRepository.sumReceitasBySituacao("VENCIDO").orElse(0.0));
        
        // Contadores por situação
        estatisticas.put("qtdPendente", receitaRepository.countReceitasBySituacao("PENDENTE"));
        estatisticas.put("qtdPago", receitaRepository.countReceitasBySituacao("PAGO"));
        estatisticas.put("qtdVencido", receitaRepository.countReceitasBySituacao("VENCIDO"));
        
        return estatisticas;
    }

    /**
     * Validações para criação de receita
     */
    private void validarCriacaoReceita(ReceitaDTO receitaDTO) {
        validarCriacaoReceita(receitaDTO, false);
    }
    
    /**
     * Validações para criação de receita
     * @param receitaDTO DTO da receita
     * @param permitirDataPassada Se true, permite criar receitas com data de vencimento no passado (útil para receitas retroativas de parcelas vencidas)
     */
    private void validarCriacaoReceita(ReceitaDTO receitaDTO, boolean permitirDataPassada) {
        // Validar se contrato existe e está ativo
        // Validar se aluno está vinculado ao contrato
        // Validar datas (apenas se não for receita retroativa)
        if (!permitirDataPassada && receitaDTO.dataVencimento() != null && receitaDTO.dataVencimento().isBefore(LocalDate.now())) {
            throw new BusinessException("Data de vencimento não pode ser anterior à data atual");
        }
    }

    /**
     * Cria receita de matrícula
     */
    private void criarReceitaMatricula(ContratoDTO contratoDTO) {
        ReceitaDTO receitaMatricula = ReceitaDTO.createNew(
                contratoDTO.id(),
                contratoDTO.alunoId(),
                "MATRICULA",
                contratoDTO.valorMatricula(),
                contratoDTO.dataInicioVigencia(),
                0,
                contratoDTO.numeroParcelas()
        );
        
        criarReceita(receitaMatricula);
    }

    /**
     * Cria receitas de mensalidade
     * A primeira mensalidade inclui o valor da matrícula
     */
    private void criarReceitasMensalidade(ContratoDTO contratoDTO) {
        // Usar dataInicioVigencia se disponível, caso contrário usar dataContrato
        LocalDate dataBase = contratoDTO.dataInicioVigencia() != null 
                ? contratoDTO.dataInicioVigencia() 
                : contratoDTO.dataContrato();
        
        for (int i = 1; i <= contratoDTO.numeroParcelas(); i++) {
            LocalDate dataVencimento = dataBase.plusMonths(i);
            
            // Primeira mensalidade: valor da mensalidade + valor da matrícula
            // Demais mensalidades: apenas valor da mensalidade
            BigDecimal valorReceita = contratoDTO.valorMensalidade();
            if (i == 1 && contratoDTO.valorMatricula() != null) {
                valorReceita = valorReceita.add(contratoDTO.valorMatricula());
            }
            
            ReceitaDTO receitaMensalidade = ReceitaDTO.createNew(
                    contratoDTO.id(),
                    contratoDTO.alunoId(),
                    i == 1 ? "MENSALIDADE + MATRÍCULA" : "MENSALIDADE",
                    valorReceita,
                    dataVencimento,
                    i,
                    contratoDTO.numeroParcelas()
            );
            
            criarReceita(receitaMensalidade);
        }
    }

    /**
     * Calcula valor final da receita
     */
    private BigDecimal calcularValorFinal(Receita receita) {
        BigDecimal valorFinal = receita.getValorOriginal();
        
        // Subtrair desconto
        if (receita.getValorDesconto() != null) {
            valorFinal = valorFinal.subtract(receita.getValorDesconto());
        }
        
        // Adicionar juros
        if (receita.getValorJuros() != null) {
            valorFinal = valorFinal.add(receita.getValorJuros());
        }
        
        return valorFinal.max(BigDecimal.ZERO);
    }
}
