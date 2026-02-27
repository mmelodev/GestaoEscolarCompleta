package br.com.arirang.plataforma.service;

import br.com.arirang.plataforma.dto.MensalidadeDTO;
import br.com.arirang.plataforma.entity.Parcela;
import br.com.arirang.plataforma.entity.StatusParcela;
import br.com.arirang.plataforma.repository.ParcelaRepository;
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
@Transactional(readOnly = true)
public class MensalidadeService {

    private static final Logger logger = LoggerFactory.getLogger(MensalidadeService.class);

    @Autowired
    private ParcelaRepository parcelaRepository;

    /**
     * Lista todas as mensalidades (parcelas) excluindo alunos deletados
     */
    public List<MensalidadeDTO> listarTodasMensalidades() {
        try {
            List<Parcela> parcelas = parcelaRepository.findAllWithAlunoAndContrato();
            return parcelas.stream()
                    .map(this::convertToDTO)
                    .filter(dto -> dto != null) // Remove DTOs nulos (alunos deletados)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar todas as mensalidades: ", e);
            throw new RuntimeException("Erro ao listar mensalidades: " + e.getMessage());
        }
    }

    /**
     * Lista mensalidades por status
     */
    public List<MensalidadeDTO> listarMensalidadesPorStatus(StatusParcela status) {
        try {
            List<Parcela> parcelas = parcelaRepository.findByStatusParcelaWithAlunoAndContrato(status);
            return parcelas.stream()
                    .map(this::convertToDTO)
                    .filter(dto -> dto != null) // Remove DTOs nulos (alunos deletados)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar mensalidades por status {}: ", status, e);
            throw new RuntimeException("Erro ao listar mensalidades: " + e.getMessage());
        }
    }

    /**
     * Lista mensalidades vencidas
     */
    public List<MensalidadeDTO> listarMensalidadesVencidas() {
        try {
            List<Parcela> parcelas = parcelaRepository.findParcelasVencidasWithAlunoAndContrato(LocalDate.now());
            return parcelas.stream()
                    .map(this::convertToDTO)
                    .filter(dto -> dto != null) // Remove DTOs nulos (alunos deletados)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar mensalidades vencidas: ", e);
            throw new RuntimeException("Erro ao listar mensalidades vencidas: " + e.getMessage());
        }
    }

    /**
     * Lista mensalidades por período de vencimento
     */
    public List<MensalidadeDTO> listarMensalidadesPorPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        try {
            List<Parcela> parcelas = parcelaRepository.findByPeriodoVencimentoWithAlunoAndContrato(dataInicio, dataFim);
            return parcelas.stream()
                    .map(this::convertToDTO)
                    .filter(dto -> dto != null) // Remove DTOs nulos (alunos deletados)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar mensalidades por período: ", e);
            throw new RuntimeException("Erro ao listar mensalidades: " + e.getMessage());
        }
    }

    /**
     * Lista mensalidades por aluno
     */
    public List<MensalidadeDTO> listarMensalidadesPorAluno(Long alunoId) {
        try {
            List<Parcela> parcelas = parcelaRepository.findAllWithAlunoAndContrato();
            return parcelas.stream()
                    .filter(p -> p.getContrato() != null 
                            && p.getContrato().getAluno() != null 
                            && p.getContrato().getAluno().getId() != null
                            && p.getContrato().getAluno().getId().equals(alunoId))
                    .map(this::convertToDTO)
                    .filter(dto -> dto != null) // Remove DTOs nulos (alunos deletados)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar mensalidades por aluno ID {}: ", alunoId, e);
            throw new RuntimeException("Erro ao listar mensalidades: " + e.getMessage());
        }
    }

    /**
     * Lista mensalidades por contrato
     */
    public List<MensalidadeDTO> listarMensalidadesPorContrato(Long contratoId) {
        try {
            List<Parcela> parcelas = parcelaRepository.findByContratoIdOrderByDataVencimentoAsc(contratoId);
            return parcelas.stream()
                    .map(this::convertToDTO)
                    .filter(dto -> dto != null) // Remove DTOs nulos (alunos deletados)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Erro ao listar mensalidades por contrato ID {}: ", contratoId, e);
            throw new RuntimeException("Erro ao listar mensalidades: " + e.getMessage());
        }
    }

    /**
     * Calcula estatísticas de mensalidades
     */
    public java.util.Map<String, Object> calcularEstatisticasMensalidades() {
        try {
            List<MensalidadeDTO> todas = listarTodasMensalidades();
            List<MensalidadeDTO> pendentes = listarMensalidadesPorStatus(StatusParcela.PENDENTE);
            List<MensalidadeDTO> pagas = listarMensalidadesPorStatus(StatusParcela.PAGA);
            List<MensalidadeDTO> vencidas = listarMensalidadesVencidas();
            
            // Para pendentes, usar valorParcela (sem juros/multa, pois ainda não venceu)
            BigDecimal totalPendente = pendentes.stream()
                    .map(m -> m.valorParcela() != null ? m.valorParcela() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Para pagas, usar valorPago
            BigDecimal totalPago = pagas.stream()
                    .map(m -> m.valorPago() != null ? m.valorPago() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Para vencidas, usar valorComJurosEMulta (inclui juros e multa)
            BigDecimal totalVencido = vencidas.stream()
                    .map(m -> {
                        // Se tem valorComJurosEMulta calculado, usar ele, senão usar valorParcela
                        if (m.valorComJurosEMulta() != null && m.valorComJurosEMulta().compareTo(BigDecimal.ZERO) > 0) {
                            return m.valorComJurosEMulta();
                        }
                        return m.valorParcela() != null ? m.valorParcela() : BigDecimal.ZERO;
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return java.util.Map.of(
                    "totalMensalidades", todas.size(),
                    "totalPendentes", pendentes.size(),
                    "totalPagas", pagas.size(),
                    "totalVencidas", vencidas.size(),
                    "valorTotalPendente", totalPendente,
                    "valorTotalPago", totalPago,
                    "valorTotalVencido", totalVencido
            );
        } catch (Exception e) {
            logger.error("Erro ao calcular estatísticas de mensalidades: ", e);
            throw new RuntimeException("Erro ao calcular estatísticas: " + e.getMessage());
        }
    }

    /**
     * Lista alunos com parcelas próximas a vencer (vencidas, hoje ou nos próximos 5 dias)
     * Retorna apenas uma parcela por aluno (a mais próxima)
     */
    public List<MensalidadeDTO> listarAlunosComPagamentosProximos() {
        try {
            LocalDate hoje = LocalDate.now();
            LocalDate dataFim = hoje.plusDays(5);
            
            List<StatusParcela> statuses = java.util.Arrays.asList(StatusParcela.PENDENTE, StatusParcela.EM_ATRASO);
            
            // Primeiro, tentar buscar com a query específica
            List<Parcela> parcelas = parcelaRepository.findParcelasProximasAVencer(hoje, dataFim, statuses);
            logger.info("Total de parcelas encontradas (vencidas + próximos 5 dias): {}", parcelas.size());
            
            // Se não encontrar nada, buscar todas as parcelas pendentes/em atraso (sem limite de data)
            if (parcelas.isEmpty()) {
                logger.info("Nenhuma parcela encontrada no período. Buscando todas as parcelas pendentes/em atraso...");
                parcelas = parcelaRepository.findByStatusParcelaWithAlunoAndContrato(StatusParcela.PENDENTE);
                parcelas.addAll(parcelaRepository.findByStatusParcelaWithAlunoAndContrato(StatusParcela.EM_ATRASO));
                logger.info("Total de parcelas pendentes/em atraso encontradas: {}", parcelas.size());
            }
            
            // Agrupar por aluno e pegar apenas a primeira parcela (mais próxima) de cada aluno
            List<MensalidadeDTO> resultado = parcelas.stream()
                    .filter(p -> {
                        boolean valido = p.getContrato() != null 
                                && p.getContrato().getAluno() != null 
                                && p.getContrato().getAluno().getId() != null;
                        if (!valido) {
                            logger.debug("Parcela {} filtrada: contrato ou aluno nulo", p.getId());
                        }
                        return valido;
                    })
                    .collect(Collectors.groupingBy(
                            p -> p.getContrato().getAluno().getId(),
                            Collectors.minBy((p1, p2) -> p1.getDataVencimento().compareTo(p2.getDataVencimento()))
                    ))
                    .values()
                    .stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(this::convertToDTO)
                    .filter(dto -> dto != null)
                    .sorted((m1, m2) -> m1.dataVencimento().compareTo(m2.dataVencimento()))
                    .collect(Collectors.toList());
            
            logger.info("Total de alunos com pagamentos próximos: {}", resultado.size());
            return resultado;
        } catch (Exception e) {
            logger.error("Erro ao listar alunos com pagamentos próximos: ", e);
            throw new RuntimeException("Erro ao listar alunos com pagamentos próximos: " + e.getMessage());
        }
    }

    /**
     * Converte Parcela para MensalidadeDTO
     * Filtra alunos deletados (alunos com ID null não aparecem)
     */
    private MensalidadeDTO convertToDTO(Parcela parcela) {
        if (parcela == null || parcela.getContrato() == null || parcela.getContrato().getAluno() == null) {
            return null;
        }
        
        // Garantir que o aluno não foi deletado (verificação adicional)
        if (parcela.getContrato().getAluno().getId() == null) {
            return null;
        }
        
        return new MensalidadeDTO(
                parcela.getId(),
                parcela.getContrato().getId(),
                parcela.getContrato().getNumeroContrato(),
                parcela.getContrato().getAluno().getId(),
                parcela.getContrato().getAluno().getNomeCompleto(),
                parcela.getContrato().getAluno().getEmail(),
                parcela.getContrato().getAluno().getTelefone(),
                parcela.getContrato().getTurma() != null ? parcela.getContrato().getTurma().getId() : null,
                parcela.getContrato().getTurma() != null ? parcela.getContrato().getTurma().getNomeTurma() : null,
                parcela.getNumeroParcela(),
                parcela.getValorParcela(),
                parcela.getDataVencimento(),
                parcela.getDataPagamento(),
                parcela.getValorPago(),
                parcela.getJurosAplicados(),
                parcela.getMultaAplicada(),
                parcela.getDescontoAplicado(),
                parcela.getStatusParcela(),
                parcela.getObservacoes(),
                null, // Será calculado no construtor
                false, // Será calculado no construtor
                false // Será calculado no construtor
        );
    }
}

