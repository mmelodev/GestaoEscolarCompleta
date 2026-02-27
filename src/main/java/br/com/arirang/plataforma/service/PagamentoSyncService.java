package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.entity.Financeiro;
import br.com.arirang.plataforma.entity.Pagamento;
import br.com.arirang.plataforma.entity.Parcela;
import br.com.arirang.plataforma.entity.Receita;
import br.com.arirang.plataforma.entity.StatusParcela;
import br.com.arirang.plataforma.entity.TipoMovimentoFinanceiro;
import br.com.arirang.plataforma.entity.CategoriaFinanceira;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.repository.FinanceiroRepository;
import br.com.arirang.plataforma.repository.PagamentoRepository;
import br.com.arirang.plataforma.repository.ParcelaRepository;
import br.com.arirang.plataforma.repository.ReceitaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Executa sincronização pós-pagamento (parcela + dashboard) em nova transação.
 * Usado por PagamentoService para evitar self-invocation e ciclo de dependência.
 */
@Service
public class PagamentoSyncService {

    private static final Logger logger = LoggerFactory.getLogger(PagamentoSyncService.class);

    @Autowired
    private ReceitaRepository receitaRepository;

    @Autowired
    private PagamentoRepository pagamentoRepository;

    @Autowired
    private ParcelaRepository parcelaRepository;

    @Autowired
    private FinanceiroRepository financeiroRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sincronizarPosPagamentoEmNovaTransacao(Long receitaId, Long pagamentoId) {
        Receita receita = receitaRepository.findById(receitaId)
                .orElseThrow(() -> new ResourceNotFoundException("Receita não encontrada: " + receitaId));
        Pagamento pagamento = pagamentoRepository.findById(pagamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pagamento não encontrado: " + pagamentoId));
        atualizarParcelaRelacionada(receita, pagamento);
        sincronizarPagamentoComDashboard(pagamento, receita);
    }

    private void atualizarParcelaRelacionada(Receita receita, Pagamento pagamentoRegistrado) {
        try {
            if (receita.getContrato() == null || receita.getNumeroParcela() == null) {
                logger.warn("Receita ID {} não tem contrato ou número de parcela.", receita.getId());
                return;
            }
            List<Parcela> parcelas = parcelaRepository.findByContratoId(receita.getContrato().getId());
            Optional<Parcela> parcelaOpt = parcelas.stream()
                    .filter(p -> p.getNumeroParcela().equals(receita.getNumeroParcela()))
                    .findFirst();

            if (parcelaOpt.isEmpty()) {
                logger.warn("Parcela não encontrada para receita ID {} - Contrato: {}, Número Parcela: {}",
                        receita.getId(), receita.getContrato().getId(), receita.getNumeroParcela());
                return;
            }

            Parcela parcela = parcelaOpt.get();
            List<Pagamento> pagamentosReceita = pagamentoRepository.findByReceitaIdOrderByDataPagamentoDesc(receita.getId());
            BigDecimal totalPagoReceita = pagamentosReceita.stream()
                    .map(Pagamento::getValorPago)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            parcela.setValorPago(totalPagoReceita);
            if (!pagamentosReceita.isEmpty()) {
                parcela.setDataPagamento(pagamentosReceita.get(0).getDataPagamento());
            } else {
                parcela.setDataPagamento(LocalDate.now());
            }

            BigDecimal valorTotalParcela = parcela.getValorComJurosEMulta();
            if (valorTotalParcela == null || valorTotalParcela.compareTo(BigDecimal.ZERO) == 0) {
                valorTotalParcela = parcela.getValorParcela() != null ? parcela.getValorParcela() : BigDecimal.ZERO;
            }

            boolean integralComDesconto100 = false;
            if (pagamentoRegistrado != null
                    && pagamentoRegistrado.getValorPago() != null
                    && pagamentoRegistrado.getValorPago().compareTo(BigDecimal.ZERO) == 0) {
                var pct = pagamentoRegistrado.getDescontoPercentual();
                var vDesc = pagamentoRegistrado.getDescontoValor();
                if (pct != null && pct.compareTo(BigDecimal.valueOf(100)) >= 0) {
                    integralComDesconto100 = true;
                } else if (vDesc != null && valorTotalParcela.compareTo(BigDecimal.ZERO) > 0
                        && vDesc.compareTo(valorTotalParcela) >= 0) {
                    integralComDesconto100 = true;
                }
            }

            if (totalPagoReceita.compareTo(valorTotalParcela) >= 0 || integralComDesconto100) {
                parcela.setStatusParcela(StatusParcela.PAGA);
                parcela.marcarComoPaga(totalPagoReceita);
                logger.info("Parcela ID {} marcada como PAGA. Valor pago: R$ {}, Valor total: R$ {} (desconto 100%: {})",
                        parcela.getId(), totalPagoReceita, valorTotalParcela, integralComDesconto100);
            } else if (totalPagoReceita.compareTo(BigDecimal.ZERO) > 0) {
                if (parcela.getStatusParcela() != StatusParcela.PAGA) {
                    if (parcela.isVencida() && parcela.getStatusParcela() != StatusParcela.EM_ATRASO) {
                        parcela.setStatusParcela(StatusParcela.EM_ATRASO);
                    }
                    logger.info("Parcela ID {} parcialmente paga. Valor pago: R$ {}, Valor total: R$ {}",
                            parcela.getId(), totalPagoReceita, valorTotalParcela);
                }
            }

            parcelaRepository.save(parcela);
            logger.info("Parcela ID {} sincronizada após pagamento. Status: {}, Valor pago: R$ {}",
                    parcela.getId(), parcela.getStatusParcela(), totalPagoReceita);
        } catch (Exception e) {
            logger.error("Erro ao atualizar parcela relacionada para receita ID {}: ", receita.getId(), e);
        }
    }

    private void sincronizarPagamentoComDashboard(Pagamento pagamento, Receita receita) {
        try {
            BigDecimal valor = pagamento.getValorPago();
            if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
                logger.info("Pagamento ID {} com valor zero (100% desconto). Sincronização com dashboard omitida.", pagamento.getId());
                return;
            }

            List<Financeiro> movimentosExistentes = financeiroRepository.findAll()
                    .stream()
                    .filter(f -> f.getNumeroDocumento() != null &&
                            f.getNumeroDocumento().equals("PAG-" + pagamento.getId()))
                    .toList();

            if (!movimentosExistentes.isEmpty()) {
                logger.info("Pagamento ID {} já possui movimento financeiro no dashboard", pagamento.getId());
                return;
            }

            Financeiro movimentoFinanceiro = new Financeiro();
            movimentoFinanceiro.setTipoMovimento(TipoMovimentoFinanceiro.RECEITA);
            movimentoFinanceiro.setValor(pagamento.getValorPago());
            movimentoFinanceiro.setDataMovimento(pagamento.getDataPagamento());
            movimentoFinanceiro.setDescricao("Pagamento recebido - " +
                    (receita.getDescricao() != null ? receita.getDescricao() : "Receita ID " + receita.getId()));
            movimentoFinanceiro.setCategoria(CategoriaFinanceira.MENSALIDADE);

            if (receita.getContrato() != null) {
                movimentoFinanceiro.setContrato(receita.getContrato());
                movimentoFinanceiro.setAluno(receita.getContrato().getAluno());
            } else if (receita.getAluno() != null) {
                movimentoFinanceiro.setAluno(receita.getAluno());
            }

            movimentoFinanceiro.setReferencia("PAGAMENTO-" + pagamento.getId());
            movimentoFinanceiro.setNumeroDocumento("PAG-" + pagamento.getId());
            movimentoFinanceiro.setObservacoes("Pagamento registrado - Forma: " + pagamento.getFormaPagamento() +
                    (pagamento.getObservacoes() != null ? " - " + pagamento.getObservacoes() : ""));
            movimentoFinanceiro.setConfirmado(true);
            movimentoFinanceiro.setDataCriacao(LocalDateTime.now());
            movimentoFinanceiro.setDataConfirmacao(LocalDateTime.now());
            movimentoFinanceiro.setConfirmadoPor(pagamento.getUsuarioPagamento() != null ?
                    pagamento.getUsuarioPagamento() : "SISTEMA");

            financeiroRepository.save(movimentoFinanceiro);
            logger.info("Pagamento ID {} sincronizado com dashboard financeiro - Valor: R$ {}",
                    pagamento.getId(), pagamento.getValorPago());
        } catch (Exception e) {
            logger.error("Erro ao sincronizar pagamento ID {} com dashboard financeiro: ", pagamento.getId(), e);
        }
    }
}
