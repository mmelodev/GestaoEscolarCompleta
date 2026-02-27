package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.PagamentoDTO;
import br.com.arirang.plataforma.dto.ReceitaDTO;
import br.com.arirang.plataforma.entity.Pagamento;
import br.com.arirang.plataforma.entity.Receita;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.mapper.PagamentoMapper;
import br.com.arirang.plataforma.mapper.ReceitaMapper;
import br.com.arirang.plataforma.repository.PagamentoRepository;
import br.com.arirang.plataforma.repository.ReceitaRepository;
import br.com.arirang.plataforma.repository.ParcelaRepository;
import br.com.arirang.plataforma.entity.Parcela;
import br.com.arirang.plataforma.entity.StatusParcela;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PagamentoService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoService.class);

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private PagamentoMapper pagamentoMapper;

    @Autowired
    private ReceitaMapper receitaMapper;

    @Autowired
    private ParcelaRepository parcelaRepository;

    @Autowired
    private PagamentoRegistroCoreService pagamentoRegistroCoreService;

    @Autowired
    private PagamentoSyncService pagamentoSyncService;

    /**
     * Lista todos os pagamentos
     */
    @Transactional(readOnly = true)
    public List<PagamentoDTO> listarTodosPagamentos() {
        logger.debug("Listando todos os pagamentos");
        return pagamentoRepository.findAll()
                .stream()
                .map(pagamentoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca pagamento por ID
     */
    @Transactional(readOnly = true)
    public Optional<PagamentoDTO> buscarPagamentoPorId(Long id) {
        logger.debug("Buscando pagamento por ID: {}", id);
        return pagamentoRepository.findById(id)
                .map(pagamentoMapper::toDto);
    }
    
    /**
     * Busca pagamento por ID com todas as relações carregadas (receita, contrato, aluno, turma)
     */
    @Transactional(readOnly = true)
    public Optional<Pagamento> buscarPagamentoComRelacoes(Long id) {
        logger.debug("Buscando pagamento por ID com todas as relações: {}", id);
        return pagamentoRepository.findByIdWithRelations(id);
    }

    /**
     * Lista pagamentos por receita
     */
    @Transactional(readOnly = true)
    public List<PagamentoDTO> listarPagamentosPorReceita(Long receitaId) {
        logger.debug("Listando pagamentos por receita ID: {}", receitaId);
        return pagamentoRepository.findByReceitaIdOrderByDataPagamentoDesc(receitaId)
                .stream()
                .map(pagamentoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista pagamentos por aluno
     */
    @Transactional(readOnly = true)
    public List<PagamentoDTO> listarPagamentosPorAluno(Long alunoId) {
        logger.debug("Listando pagamentos por aluno ID: {}", alunoId);
        return pagamentoRepository.findByAlunoIdOrderByDataPagamentoDesc(alunoId)
                .stream()
                .map(pagamentoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Lista pagamentos por período
     */
    @Transactional(readOnly = true)
    public List<PagamentoDTO> listarPagamentosPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        logger.debug("Listando pagamentos por período: {} a {}", dataInicio, dataFim);
        return pagamentoRepository.findPagamentosPorPeriodo(dataInicio, dataFim)
                .stream()
                .map(pagamentoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Busca pagamentos com filtros
     */
    @Transactional(readOnly = true)
    public List<PagamentoDTO> buscarPagamentosComFiltros(Long alunoId, String formaPagamento, 
                                                         LocalDate dataInicio, LocalDate dataFim) {
        logger.debug("Buscando pagamentos com filtros");
        return pagamentoRepository.findPagamentosWithFilters(alunoId, formaPagamento, dataInicio, dataFim)
                .stream()
                .map(pagamentoMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Registra um pagamento. Orquestra core (tx própria) + sincronização (nova tx).
     * Sem transação própria para evitar rollback-only quando a sync falha.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PagamentoDTO registrarPagamento(PagamentoDTO pagamentoDTO) {
        validarPagamento(pagamentoDTO);
        PagamentoDTO result = pagamentoRegistroCoreService.registrarPagamentoCore(pagamentoDTO);
        Long receitaId = pagamentoDTO.receitaId();
        Long pagamentoId = result.id();
        try {
            pagamentoSyncService.sincronizarPosPagamentoEmNovaTransacao(receitaId, pagamentoId);
        } catch (Exception e) {
            logger.error("Erro na sincronização pós-pagamento (parcela/dashboard). Pagamento registrado com sucesso.", e);
        }
        return result;
    }

    /**
     * Atualiza pagamento
     */
    public PagamentoDTO atualizarPagamento(Long id, PagamentoDTO pagamentoDTO) {
        logger.debug("Atualizando pagamento ID: {}", id);

        Pagamento pagamentoExistente = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado com ID: " + id));

        // Atualizar campos permitidos
        pagamentoExistente.setObservacoes(pagamentoDTO.observacoes());
        pagamentoExistente.setNumeroTransacao(pagamentoDTO.numeroTransacao());
        pagamentoExistente.setComprovanteCaminho(pagamentoDTO.comprovanteCaminho());

        Pagamento pagamentoAtualizado = pagamentoRepository.save(pagamentoExistente);
        logger.info("Pagamento atualizado com sucesso. ID: {}", pagamentoAtualizado.getId());

        return pagamentoMapper.toDto(pagamentoAtualizado);
    }

    /**
     * Deleta pagamento
     */
    public void deletarPagamento(Long id) {
        logger.debug("Deletando pagamento ID: {}", id);

        Pagamento pagamento = pagamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado com ID: " + id));

        // Reverter situação da receita
        Receita receita = pagamento.getReceita();
        receita.setSituacao("PENDENTE");
        receita.setDataPagamento(null);
        receitaRepository.save(receita);

        // Deletar pagamento
        pagamentoRepository.delete(pagamento);
        logger.info("Pagamento deletado com sucesso. ID: {}", id);
    }

    /**
     * Calcula estatísticas de pagamentos
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> calcularEstatisticasPagamentos() {
        java.util.Map<String, Object> estatisticas = new java.util.HashMap<>();
        
        // Totais por forma de pagamento
        estatisticas.put("totalDinheiro", pagamentoRepository.sumPagamentosPorPeriodo(LocalDate.now().withDayOfMonth(1), LocalDate.now()));
        estatisticas.put("totalPix", 0.0); // TODO: Implementar soma por forma de pagamento
        estatisticas.put("totalCartao", 0.0);
        
        // Contadores
        estatisticas.put("qtdDinheiro", pagamentoRepository.countPagamentosByFormaPagamento("DINHEIRO"));
        estatisticas.put("qtdPix", pagamentoRepository.countPagamentosByFormaPagamento("PIX"));
        estatisticas.put("qtdCartao", pagamentoRepository.countPagamentosByFormaPagamento("CARTAO_DEBITO") + 
                                     pagamentoRepository.countPagamentosByFormaPagamento("CARTAO_CREDITO"));
        
        return estatisticas;
    }

    /**
     * Gera carnê de pagamentos para um aluno
     */
    @Transactional(readOnly = true)
    public List<ReceitaDTO> gerarCarneAluno(Long alunoId) {
        logger.debug("Gerando carnê para aluno ID: {}", alunoId);
        
        return receitaRepository.findByAlunoIdOrderByDataVencimentoDesc(alunoId)
                .stream()
                .filter(receita -> "PENDENTE".equals(receita.getSituacao()) || "VENCIDO".equals(receita.getSituacao()))
                .map(receita -> receitaMapper.toDto(receita))
                .filter(dto -> dto != null)
                .collect(Collectors.toList());
    }

    /**
     * Validações para pagamento
     */
    private void validarPagamento(PagamentoDTO pagamentoDTO) {
        if (pagamentoDTO.valorPago().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Valor do pagamento não pode ser negativo");
        }

        if (pagamentoDTO.dataPagamento().isAfter(LocalDate.now())) {
            throw new BusinessException("Data do pagamento não pode ser futura");
        }
    }

    /**
     * Sincroniza o status de todas as parcelas baseado nos pagamentos existentes
     * Útil para corrigir parcelas que estão como pendentes mas já foram pagas
     */
    @Transactional
    public void sincronizarStatusParcelas() {
        logger.info("Iniciando sincronização de status das parcelas...");
        int parcelasAtualizadas = 0;
        
        try {
            // Buscar todas as receitas que têm pagamentos
            List<Receita> receitasComPagamentos = receitaRepository.findAll()
                    .stream()
                    .filter(r -> r.getContrato() != null && r.getNumeroParcela() != null)
                    .filter(r -> !pagamentoRepository.findByReceitaIdOrderByDataPagamentoDesc(r.getId()).isEmpty())
                    .collect(Collectors.toList());
            
            for (Receita receita : receitasComPagamentos) {
                try {
                    // Buscar parcela relacionada
                    List<Parcela> parcelas = parcelaRepository.findByContratoId(receita.getContrato().getId());
                    Optional<Parcela> parcelaOpt = parcelas.stream()
                            .filter(p -> p.getNumeroParcela().equals(receita.getNumeroParcela()))
                            .findFirst();
                    
                    if (parcelaOpt.isPresent()) {
                        Parcela parcela = parcelaOpt.get();
                        
                        // Se a parcela já está como PAGA, pular
                        if (parcela.getStatusParcela() == StatusParcela.PAGA) {
                            continue;
                        }
                        
                        // Calcular total pago na receita
                        List<Pagamento> pagamentos = pagamentoRepository.findByReceitaIdOrderByDataPagamentoDesc(receita.getId());
                        BigDecimal totalPago = pagamentos.stream()
                                .map(Pagamento::getValorPago)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                        // Calcular valor total da parcela
                        BigDecimal valorTotalParcela = parcela.getValorComJurosEMulta();
                        if (valorTotalParcela.compareTo(BigDecimal.ZERO) == 0) {
                            valorTotalParcela = parcela.getValorParcela();
                        }
                        
                        // Se o total pago é maior ou igual ao valor da parcela, marcar como PAGA
                        if (totalPago.compareTo(valorTotalParcela) >= 0) {
                            parcela.setStatusParcela(StatusParcela.PAGA);
                            parcela.setValorPago(totalPago);
                            if (!pagamentos.isEmpty()) {
                                parcela.setDataPagamento(pagamentos.get(0).getDataPagamento());
                            }
                            parcelaRepository.save(parcela);
                            parcelasAtualizadas++;
                            logger.info("Parcela ID {} sincronizada: PENDENTE -> PAGA (Valor pago: R$ {})", 
                                       parcela.getId(), totalPago);
                        } else if (totalPago.compareTo(BigDecimal.ZERO) > 0) {
                            // Atualizar valor pago mesmo que parcial
                            parcela.setValorPago(totalPago);
                            if (!pagamentos.isEmpty()) {
                                parcela.setDataPagamento(pagamentos.get(0).getDataPagamento());
                            }
                            parcelaRepository.save(parcela);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Erro ao sincronizar parcela para receita ID {}: ", receita.getId(), e);
                }
            }
            
            logger.info("Sincronização concluída. {} parcelas atualizadas.", parcelasAtualizadas);
        } catch (Exception e) {
            logger.error("Erro ao sincronizar status das parcelas: ", e);
        }
    }
}
