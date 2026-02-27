package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.entity.*;
import br.com.arirang.plataforma.repository.FinanceiroRepository;
import br.com.arirang.plataforma.repository.ContratoRepository;
import br.com.arirang.plataforma.repository.ParcelaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FinanceiroContratoSyncService {

    private static final Logger logger = LoggerFactory.getLogger(FinanceiroContratoSyncService.class);

    @Autowired
    private FinanceiroRepository financeiroRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private ParcelaRepository parcelaRepository;

    /**
     * Sincroniza um novo contrato com o sistema financeiro
     * Cria apenas a receita da parcela mais próxima (próximo vencimento)
     */
    @Transactional
    public void sincronizarContratoComFinanceiro(Long contratoId) {
        try {
            logger.info("Iniciando sincronização financeira para contrato ID: {}", contratoId);
            
            Optional<Contrato> contratoOpt = contratoRepository.findById(contratoId);
            if (contratoOpt.isEmpty()) {
                logger.warn("Contrato ID {} não encontrado para sincronização", contratoId);
                return;
            }

            Contrato contrato = contratoOpt.get();
            
            // Verificar se já existe movimento financeiro para este contrato
            List<Financeiro> movimentosExistentes = financeiroRepository.findByContratoId(contratoId);
            if (!movimentosExistentes.isEmpty()) {
                logger.info("Contrato ID {} já possui movimentos financeiros. Pulando sincronização.", contratoId);
                return;
            }

            // Buscar a parcela mais próxima (próximo vencimento)
            List<Parcela> parcelas = parcelaRepository.findByContratoId(contratoId);
            if (parcelas.isEmpty()) {
                logger.warn("Nenhuma parcela encontrada para contrato ID {}", contratoId);
                return;
            }
            
            // Ordenar por data de vencimento e pegar a primeira
            Parcela proximaParcela = parcelas.stream()
                    .sorted((p1, p2) -> p1.getDataVencimento().compareTo(p2.getDataVencimento()))
                    .findFirst()
                    .orElse(null);
            
            if (proximaParcela == null) {
                logger.warn("Nenhuma parcela válida encontrada para contrato ID {}", contratoId);
                return;
            }
            
            // Criar movimento financeiro para a parcela mais próxima
            Financeiro movimentoFinanceiro = new Financeiro();
            movimentoFinanceiro.setTipoMovimento(TipoMovimentoFinanceiro.RECEITA);
            movimentoFinanceiro.setValor(proximaParcela.getValorParcela());
            movimentoFinanceiro.setDataMovimento(proximaParcela.getDataVencimento());
            movimentoFinanceiro.setDescricao("Receita - Parcela " + proximaParcela.getNumeroParcela() + "/" + contrato.getNumeroParcelas());
            movimentoFinanceiro.setCategoria(CategoriaFinanceira.MENSALIDADE);
            movimentoFinanceiro.setContrato(contrato);
            movimentoFinanceiro.setParcela(proximaParcela);
            movimentoFinanceiro.setAluno(contrato.getAluno());
            movimentoFinanceiro.setReferencia("Parcela " + proximaParcela.getNumeroParcela() + "/" + contrato.getNumeroParcelas());
            movimentoFinanceiro.setNumeroDocumento("PARC-" + proximaParcela.getId());
            movimentoFinanceiro.setObservacoes("Receita automática gerada para próxima parcela do contrato");
            movimentoFinanceiro.setConfirmado(false);
            movimentoFinanceiro.setDataCriacao(LocalDateTime.now());

            financeiroRepository.save(movimentoFinanceiro);
            
            logger.info("Movimento financeiro criado com sucesso para contrato ID {} - Parcela ID {} - Valor: R$ {}", 
                       contratoId, proximaParcela.getId(), proximaParcela.getValorParcela());

        } catch (Exception e) {
            logger.error("Erro ao sincronizar contrato ID {} com sistema financeiro: ", contratoId, e);
            throw new RuntimeException("Erro na sincronização financeira: " + e.getMessage(), e);
        }
    }

    /**
     * Atualiza movimento financeiro quando parcela é paga
     */
    @Transactional
    public void confirmarReceitaParcela(Long parcelaId, String confirmadoPor) {
        try {
            logger.info("Confirmando receita para parcela ID: {}", parcelaId);
            
            List<Financeiro> movimentos = financeiroRepository.findByParcelaId(parcelaId);
            for (Financeiro movimento : movimentos) {
                if (movimento.getTipoMovimento() == TipoMovimentoFinanceiro.RECEITA && !movimento.isConfirmado()) {
                    movimento.confirmar(confirmadoPor);
                    movimento.setDataAtualizacao(LocalDateTime.now());
                    financeiroRepository.save(movimento);
                    
                    logger.info("Receita confirmada para parcela ID {} - Valor: R$ {}", 
                               parcelaId, movimento.getValor());
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao confirmar receita para parcela ID {}: ", parcelaId, e);
            throw new RuntimeException("Erro ao confirmar receita: " + e.getMessage(), e);
        }
    }

    /**
     * Remove movimento financeiro quando contrato é cancelado
     */
    @Transactional
    public void cancelarReceitasContrato(Long contratoId, String motivo) {
        try {
            logger.info("Cancelando receitas para contrato ID: {} - Motivo: {}", contratoId, motivo);
            
            List<Financeiro> movimentos = financeiroRepository.findByContratoId(contratoId);
            for (Financeiro movimento : movimentos) {
                if (movimento.getTipoMovimento() == TipoMovimentoFinanceiro.RECEITA && !movimento.isConfirmado()) {
                    movimento.setObservacoes("CANCELADO: " + motivo + " - " + movimento.getObservacoes());
                    movimento.setDataAtualizacao(LocalDateTime.now());
                    financeiroRepository.save(movimento);
                    
                    logger.info("Receita cancelada para contrato ID {} - Valor: R$ {}", 
                               contratoId, movimento.getValor());
                }
            }
        } catch (Exception e) {
            logger.error("Erro ao cancelar receitas para contrato ID {}: ", contratoId, e);
            throw new RuntimeException("Erro ao cancelar receitas: " + e.getMessage(), e);
        }
    }

    /**
     * Gera receita para próxima parcela quando uma é paga
     */
    @Transactional
    public void gerarProximaReceita(Long contratoId) {
        try {
            logger.info("Gerando receita para próxima parcela do contrato ID: {}", contratoId);
            
            // Buscar parcelas não pagas ordenadas por vencimento
            List<Parcela> parcelasNaoPagas = parcelaRepository.findByContratoId(contratoId)
                    .stream()
                    .filter(p -> p.getStatusParcela() == StatusParcela.PENDENTE)
                    .sorted((p1, p2) -> p1.getDataVencimento().compareTo(p2.getDataVencimento()))
                    .collect(java.util.stream.Collectors.toList());
            
            if (parcelasNaoPagas.isEmpty()) {
                logger.info("Todas as parcelas do contrato ID {} foram pagas", contratoId);
                return;
            }

            Parcela proximaParcela = parcelasNaoPagas.get(0);
            
            // Verificar se já existe movimento para esta parcela
            List<Financeiro> movimentosExistentes = financeiroRepository.findByParcelaId(proximaParcela.getId());
            if (!movimentosExistentes.isEmpty()) {
                logger.info("Já existe movimento financeiro para parcela ID {}", proximaParcela.getId());
                return;
            }

            // Criar novo movimento financeiro
            Optional<Contrato> contratoOpt = contratoRepository.findById(contratoId);
            if (contratoOpt.isEmpty()) {
                logger.warn("Contrato ID {} não encontrado", contratoId);
                return;
            }

            Contrato contrato = contratoOpt.get();
            
            Financeiro movimentoFinanceiro = new Financeiro();
            movimentoFinanceiro.setTipoMovimento(TipoMovimentoFinanceiro.RECEITA);
            movimentoFinanceiro.setValor(proximaParcela.getValorParcela());
            movimentoFinanceiro.setDataMovimento(proximaParcela.getDataVencimento());
            movimentoFinanceiro.setDescricao("Receita - Parcela " + proximaParcela.getNumeroParcela() + "/" + contrato.getNumeroParcelas());
            movimentoFinanceiro.setCategoria(CategoriaFinanceira.MENSALIDADE);
            movimentoFinanceiro.setContrato(contrato);
            movimentoFinanceiro.setParcela(proximaParcela);
            movimentoFinanceiro.setAluno(contrato.getAluno());
            movimentoFinanceiro.setReferencia("Parcela " + proximaParcela.getNumeroParcela() + "/" + contrato.getNumeroParcelas());
            movimentoFinanceiro.setNumeroDocumento("PARC-" + proximaParcela.getId());
            movimentoFinanceiro.setObservacoes("Receita automática gerada para próxima parcela");
            movimentoFinanceiro.setConfirmado(false);
            movimentoFinanceiro.setDataCriacao(LocalDateTime.now());

            financeiroRepository.save(movimentoFinanceiro);
            
            logger.info("Nova receita gerada para contrato ID {} - Parcela ID {} - Valor: R$ {}", 
                       contratoId, proximaParcela.getId(), proximaParcela.getValorParcela());

        } catch (Exception e) {
            logger.error("Erro ao gerar próxima receita para contrato ID {}: ", contratoId, e);
            throw new RuntimeException("Erro ao gerar próxima receita: " + e.getMessage(), e);
        }
    }

    /**
     * Sincroniza TODAS as parcelas de um contrato com o sistema financeiro
     * Cria movimentos financeiros para todas as parcelas pendentes
     */
    @Transactional
    public void sincronizarTodasParcelasDoContrato(Long contratoId) {
        try {
            logger.info("Iniciando sincronização de TODAS as parcelas para contrato ID: {}", contratoId);
            
            Optional<Contrato> contratoOpt = contratoRepository.findById(contratoId);
            if (contratoOpt.isEmpty()) {
                logger.warn("Contrato ID {} não encontrado para sincronização", contratoId);
                return;
            }

            Contrato contrato = contratoOpt.get();
            
            // Buscar todas as parcelas do contrato
            List<Parcela> parcelas = parcelaRepository.findByContratoIdOrderByDataVencimentoAsc(contratoId);
            if (parcelas.isEmpty()) {
                logger.warn("Nenhuma parcela encontrada para contrato ID {}", contratoId);
                return;
            }
            
            int parcelasSincronizadas = 0;
            int parcelasJaExistentes = 0;
            
            for (Parcela parcela : parcelas) {
                // Verificar se já existe movimento financeiro para esta parcela
                List<Financeiro> movimentosExistentes = financeiroRepository.findByParcelaId(parcela.getId());
                if (!movimentosExistentes.isEmpty()) {
                    parcelasJaExistentes++;
                    logger.debug("Parcela ID {} já possui movimento financeiro. Pulando.", parcela.getId());
                    continue;
                }
                
                // Criar movimento financeiro para esta parcela
                Financeiro movimentoFinanceiro = new Financeiro();
                movimentoFinanceiro.setTipoMovimento(TipoMovimentoFinanceiro.RECEITA);
                movimentoFinanceiro.setValor(parcela.getValorParcela());
                movimentoFinanceiro.setDataMovimento(parcela.getDataVencimento());
                movimentoFinanceiro.setDescricao("Receita - Parcela " + parcela.getNumeroParcela() + "/" + contrato.getNumeroParcelas());
                movimentoFinanceiro.setCategoria(CategoriaFinanceira.MENSALIDADE);
                movimentoFinanceiro.setContrato(contrato);
                movimentoFinanceiro.setParcela(parcela);
                movimentoFinanceiro.setAluno(contrato.getAluno());
                movimentoFinanceiro.setReferencia("Parcela " + parcela.getNumeroParcela() + "/" + contrato.getNumeroParcelas());
                movimentoFinanceiro.setNumeroDocumento("PARC-" + parcela.getId());
                movimentoFinanceiro.setObservacoes("Receita automática gerada para parcela do contrato");
                movimentoFinanceiro.setConfirmado(false);
                movimentoFinanceiro.setDataCriacao(LocalDateTime.now());

                financeiroRepository.save(movimentoFinanceiro);
                parcelasSincronizadas++;
                
                logger.debug("Movimento financeiro criado para parcela {} do contrato ID {}", 
                           parcela.getNumeroParcela(), contratoId);
            }
            
            logger.info("Sincronização concluída para contrato ID {}: {} parcelas sincronizadas, {} já existentes", 
                       contratoId, parcelasSincronizadas, parcelasJaExistentes);

        } catch (Exception e) {
            logger.error("Erro ao sincronizar todas as parcelas do contrato ID {}: ", contratoId, e);
            throw new RuntimeException("Erro na sincronização de parcelas: " + e.getMessage(), e);
        }
    }

    /**
     * Sincroniza todos os contratos ativos com o sistema financeiro
     * Agora sincroniza TODAS as parcelas de cada contrato
     */
    @Transactional
    public void sincronizarTodosContratosAtivos() {
        try {
            logger.info("Iniciando sincronização de todos os contratos ativos");
            
            List<Contrato> contratosAtivos = contratoRepository.findBySituacaoContratoOrderByDataCriacaoDesc(StatusContrato.ATIVO.name());
            logger.info("Encontrados {} contratos ativos para sincronização", contratosAtivos.size());
            
            int contratosSincronizados = 0;
            int contratosComErro = 0;
            
            for (Contrato contrato : contratosAtivos) {
                try {
                    // Sincronizar TODAS as parcelas do contrato
                    sincronizarTodasParcelasDoContrato(contrato.getId());
                    contratosSincronizados++;
                } catch (Exception e) {
                    contratosComErro++;
                    logger.error("Erro ao sincronizar contrato ID {}: ", contrato.getId(), e);
                    // Continua com os outros contratos mesmo se um falhar
                }
            }
            
            logger.info("Sincronização de contratos ativos concluída: {} sincronizados, {} com erro", 
                       contratosSincronizados, contratosComErro);
            
        } catch (Exception e) {
            logger.error("Erro na sincronização geral de contratos: ", e);
            throw new RuntimeException("Erro na sincronização geral: " + e.getMessage(), e);
        }
    }

    /**
     * Método de diagnóstico: verifica se as parcelas estão sendo criadas corretamente
     */
    public String diagnosticarContrato(Long contratoId) {
        try {
            Optional<Contrato> contratoOpt = contratoRepository.findById(contratoId);
            if (contratoOpt.isEmpty()) {
                return "❌ Contrato ID " + contratoId + " não encontrado";
            }

            Contrato contrato = contratoOpt.get();
            StringBuilder diagnostico = new StringBuilder();
            diagnostico.append("=== DIAGNÓSTICO DO CONTRATO ===\n");
            diagnostico.append("ID: ").append(contrato.getId()).append("\n");
            diagnostico.append("Número: ").append(contrato.getNumeroContrato()).append("\n");
            diagnostico.append("Situação: ").append(contrato.getSituacaoContrato()).append("\n");
            diagnostico.append("Aluno: ").append(contrato.getAluno() != null ? contrato.getAluno().getNomeCompleto() : "NULL").append("\n");
            diagnostico.append("Valor Mensalidade: ").append(contrato.getValorMensalidade()).append("\n");
            diagnostico.append("Número de Parcelas: ").append(contrato.getNumeroParcelas()).append("\n\n");

            // Verificar parcelas
            List<Parcela> parcelas = parcelaRepository.findByContratoIdOrderByDataVencimentoAsc(contratoId);
            diagnostico.append("=== PARCELAS ===\n");
            diagnostico.append("Total de parcelas encontradas: ").append(parcelas.size()).append("\n");
            
            if (parcelas.isEmpty()) {
                diagnostico.append("⚠️ NENHUMA PARCELA ENCONTRADA! As parcelas podem não ter sido criadas.\n");
            } else {
                for (Parcela parcela : parcelas) {
                    diagnostico.append(String.format("  Parcela %d: Valor R$ %s, Vencimento: %s, Status: %s\n",
                            parcela.getNumeroParcela(),
                            parcela.getValorParcela(),
                            parcela.getDataVencimento(),
                            parcela.getStatusParcela()));
                }
            }
            
            // Verificar movimentos financeiros
            List<Financeiro> movimentos = financeiroRepository.findByContratoId(contratoId);
            diagnostico.append("\n=== MOVIMENTOS FINANCEIROS ===\n");
            diagnostico.append("Total de movimentos: ").append(movimentos.size()).append("\n");
            
            if (movimentos.isEmpty()) {
                diagnostico.append("⚠️ NENHUM MOVIMENTO FINANCEIRO ENCONTRADO!\n");
            } else {
                for (Financeiro movimento : movimentos) {
                    diagnostico.append(String.format("  Movimento: %s, Valor: R$ %s, Parcela: %s\n",
                            movimento.getDescricao(),
                            movimento.getValor(),
                            movimento.getParcela() != null ? movimento.getParcela().getNumeroParcela() : "N/A"));
                }
            }
            
            return diagnostico.toString();
            
        } catch (Exception e) {
            logger.error("Erro ao diagnosticar contrato ID {}: ", contratoId, e);
            return "❌ Erro ao diagnosticar: " + e.getMessage();
        }
    }
}
