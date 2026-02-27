package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.FinanceiroDTO;
import br.com.arirang.plataforma.entity.*;
import br.com.arirang.plataforma.exception.BusinessException;
import br.com.arirang.plataforma.exception.ResourceNotFoundException;
import br.com.arirang.plataforma.repository.FinanceiroRepository;
import br.com.arirang.plataforma.repository.ContratoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FinanceiroService {

    private static final Logger logger = LoggerFactory.getLogger(FinanceiroService.class);

    @Autowired
    private FinanceiroRepository financeiroRepository;

    @Autowired
    private ContratoRepository contratoRepository;

    @Autowired
    private AlunoService alunoService;

    @Transactional
    public Financeiro criarMovimentoFinanceiro(FinanceiroDTO financeiroDTO) {
        try {
            logger.info("Criando movimento financeiro: {} - R$ {}", 
                       financeiroDTO.tipoMovimento(), financeiroDTO.valor());

            Financeiro financeiro = new Financeiro();
            financeiro.setTipoMovimento(financeiroDTO.tipoMovimento());
            financeiro.setValor(financeiroDTO.valor());
            financeiro.setDataMovimento(financeiroDTO.dataMovimento() != null ? 
                                      financeiroDTO.dataMovimento() : LocalDate.now());
            financeiro.setDescricao(financeiroDTO.descricao());
            financeiro.setCategoria(financeiroDTO.categoria());
            financeiro.setObservacoes(financeiroDTO.observacoes());
            financeiro.setNumeroDocumento(financeiroDTO.numeroDocumento());
            financeiro.setReferencia(financeiroDTO.referencia());

            // Vincular contrato se fornecido
            if (financeiroDTO.contratoId() != null) {
                // Buscar entidade Contrato do repository (não o DTO)
                Contrato contrato = contratoRepository.findById(financeiroDTO.contratoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Contrato não encontrado"));
                financeiro.setContrato(contrato);
                financeiro.setAluno(contrato.getAluno());
            }

            // Vincular parcela se fornecida
            if (financeiroDTO.parcelaId() != null) {
                // Implementar busca de parcela quando necessário
                // Parcela parcela = parcelaService.buscarParcelaPorId(financeiroDTO.parcelaId());
                // financeiro.setParcela(parcela);
            }

            // Vincular aluno se fornecido
            if (financeiroDTO.alunoId() != null) {
                Aluno aluno = alunoService.buscarAlunoPorId(financeiroDTO.alunoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));
                financeiro.setAluno(aluno);
            }

            financeiro = financeiroRepository.save(financeiro);

            logger.info("Movimento financeiro criado com sucesso. ID: {}", financeiro.getId());
            return financeiro;

        } catch (ResourceNotFoundException e) {
            logger.error("Recurso não encontrado ao criar movimento financeiro: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar movimento financeiro: ", e);
            throw new BusinessException("Erro ao criar movimento financeiro: " + e.getMessage());
        }
    }

    @Transactional
    public Financeiro atualizarMovimentoFinanceiro(Long id, FinanceiroDTO financeiroDTO) {
        try {
            logger.info("Atualizando movimento financeiro ID: {}", id);

            Financeiro financeiro = financeiroRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Movimento financeiro não encontrado com ID: " + id));

            // Validar se pode ser atualizado
            if (financeiro.isConfirmado()) {
                throw new BusinessException("Não é possível atualizar um movimento financeiro confirmado");
            }

            // Atualizar campos permitidos
            financeiro.setTipoMovimento(financeiroDTO.tipoMovimento());
            financeiro.setValor(financeiroDTO.valor());
            financeiro.setDataMovimento(financeiroDTO.dataMovimento());
            financeiro.setDescricao(financeiroDTO.descricao());
            financeiro.setCategoria(financeiroDTO.categoria());
            financeiro.setObservacoes(financeiroDTO.observacoes());
            financeiro.setNumeroDocumento(financeiroDTO.numeroDocumento());
            financeiro.setReferencia(financeiroDTO.referencia());
            financeiro.setDataAtualizacao(LocalDateTime.now());

            financeiro = financeiroRepository.save(financeiro);

            logger.info("Movimento financeiro atualizado com sucesso. ID: {}", financeiro.getId());
            return financeiro;

        } catch (ResourceNotFoundException | BusinessException e) {
            logger.error("Erro de negócio ao atualizar movimento financeiro: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao atualizar movimento financeiro: ", e);
            throw new BusinessException("Erro ao atualizar movimento financeiro: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Financeiro> buscarTodosMovimentos() {
        return financeiroRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Financeiro> buscarMovimentoFinanceiroPorId(Long id) {
        return financeiroRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Financeiro> buscarMovimentosPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return financeiroRepository.findByDataMovimentoBetween(dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    public List<Financeiro> buscarReceitasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return financeiroRepository.findByTipoMovimentoAndDataMovimentoBetween(
                TipoMovimentoFinanceiro.RECEITA, dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    public List<Financeiro> buscarDespesasPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        return financeiroRepository.findByTipoMovimentoAndDataMovimentoBetween(
                TipoMovimentoFinanceiro.DESPESA, dataInicio, dataFim);
    }

    @Transactional(readOnly = true)
    public List<Financeiro> buscarMovimentosPorAluno(Long alunoId) {
        return financeiroRepository.findByAlunoId(alunoId);
    }

    @Transactional(readOnly = true)
    public List<Financeiro> buscarMovimentosPorContrato(Long contratoId) {
        return financeiroRepository.findByContratoId(contratoId);
    }

    @Transactional(readOnly = true)
    public List<Financeiro> buscarMovimentosPorCategoria(CategoriaFinanceira categoria) {
        return financeiroRepository.findByCategoria(categoria);
    }

    @Transactional(readOnly = true)
    public List<Financeiro> buscarMovimentosPendentesConfirmacao() {
        return financeiroRepository.findByConfirmadoFalse();
    }

    @Transactional
    public void confirmarMovimentoFinanceiro(Long id, String confirmadoPor) {
        try {
            logger.info("Confirmando movimento financeiro ID: {}", id);

            Financeiro financeiro = financeiroRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Movimento financeiro não encontrado com ID: " + id));

            if (financeiro.isConfirmado()) {
                throw new BusinessException("Movimento financeiro já está confirmado");
            }

            financeiro.confirmar(confirmadoPor);
            financeiroRepository.save(financeiro);

            logger.info("Movimento financeiro confirmado com sucesso. ID: {}", financeiro.getId());

        } catch (ResourceNotFoundException | BusinessException e) {
            logger.error("Erro de negócio ao confirmar movimento financeiro: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao confirmar movimento financeiro: ", e);
            throw new BusinessException("Erro ao confirmar movimento financeiro: " + e.getMessage());
        }
    }

    @Transactional
    public void cancelarMovimentoFinanceiro(Long id, String motivo) {
        try {
            logger.info("Cancelando movimento financeiro ID: {}", id);

            Financeiro financeiro = financeiroRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Movimento financeiro não encontrado com ID: " + id));

            if (financeiro.isConfirmado()) {
                throw new BusinessException("Não é possível cancelar um movimento financeiro confirmado");
            }

            financeiroRepository.delete(financeiro);

            logger.info("Movimento financeiro cancelado com sucesso. ID: {}", id);

        } catch (ResourceNotFoundException | BusinessException e) {
            logger.error("Erro de negócio ao cancelar movimento financeiro: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao cancelar movimento financeiro: ", e);
            throw new BusinessException("Erro ao cancelar movimento financeiro: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularReceitaTotalPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        List<Financeiro> receitas = buscarReceitasPorPeriodo(dataInicio, dataFim);
        return receitas.stream()
                .map(Financeiro::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularDespesaTotalPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        List<Financeiro> despesas = buscarDespesasPorPeriodo(dataInicio, dataFim);
        return despesas.stream()
                .map(Financeiro::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        BigDecimal receitas = calcularReceitaTotalPorPeriodo(dataInicio, dataFim);
        BigDecimal despesas = calcularDespesaTotalPorPeriodo(dataInicio, dataFim);
        return receitas.subtract(despesas);
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularReceitaTotalPorAluno(Long alunoId) {
        List<Financeiro> movimentos = buscarMovimentosPorAluno(alunoId);
        return movimentos.stream()
                .filter(Financeiro::isReceita)
                .map(Financeiro::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Método para converter Financeiro para DTO
    public FinanceiroDTO convertToDTO(Financeiro financeiro) {
        return new FinanceiroDTO(
                financeiro.getId(),
                financeiro.getTipoMovimento(),
                financeiro.getValor(),
                financeiro.getDataMovimento(),
                financeiro.getDescricao(),
                financeiro.getCategoria(),
                financeiro.getContrato() != null ? financeiro.getContrato().getId() : null,
                financeiro.getParcela() != null ? financeiro.getParcela().getId() : null,
                financeiro.getAluno() != null ? financeiro.getAluno().getId() : null,
                financeiro.getDataCriacao(),
                financeiro.getDataAtualizacao(),
                financeiro.getObservacoes(),
                financeiro.getNumeroDocumento(),
                financeiro.getReferencia(),
                financeiro.isConfirmado(),
                financeiro.getDataConfirmacao(),
                financeiro.getConfirmadoPor()
        );
    }

    @Transactional
    public Financeiro confirmarMovimento(Long id) {
        try {
            logger.info("Confirmando movimento financeiro ID: {}", id);
            
            Financeiro movimento = financeiroRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Movimento financeiro não encontrado com ID: " + id));
            
            if (movimento.isConfirmado()) {
                throw new BusinessException("Movimento já está confirmado");
            }
            
            movimento.setConfirmado(true);
            movimento.setDataConfirmacao(LocalDateTime.now());
            movimento.setConfirmadoPor("Sistema"); // Pode ser melhorado para usar usuário logado
            
            Financeiro movimentoConfirmado = financeiroRepository.save(movimento);
            logger.info("Movimento financeiro ID {} confirmado com sucesso", id);
            
            return movimentoConfirmado;
            
        } catch (ResourceNotFoundException | BusinessException e) {
            logger.error("Erro de negócio ao confirmar movimento ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao confirmar movimento ID {}: ", id, e);
            throw new BusinessException("Erro ao confirmar movimento: " + e.getMessage());
        }
    }

    @Transactional
    public void cancelarMovimento(Long id) {
        try {
            logger.info("Cancelando movimento financeiro ID: {}", id);
            
            Financeiro movimento = financeiroRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Movimento financeiro não encontrado com ID: " + id));
            
            if (movimento.isConfirmado()) {
                throw new BusinessException("Não é possível cancelar um movimento já confirmado");
            }
            
            financeiroRepository.delete(movimento);
            logger.info("Movimento financeiro ID {} cancelado com sucesso", id);
            
        } catch (ResourceNotFoundException | BusinessException e) {
            logger.error("Erro de negócio ao cancelar movimento ID {}: {}", id, e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erro inesperado ao cancelar movimento ID {}: ", id, e);
            throw new BusinessException("Erro ao cancelar movimento: " + e.getMessage());
        }
    }
}
