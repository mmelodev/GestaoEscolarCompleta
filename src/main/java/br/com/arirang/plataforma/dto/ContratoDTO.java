package br.com.arirang.plataforma.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ContratoDTO(
        Long id,
        @NotNull(message = "Aluno é obrigatório")
        Long alunoId,
        String alunoNome,
        @NotNull(message = "Turma é obrigatória")
        Long turmaId,
        String turmaNome,
        String numeroContrato,
        @NotNull(message = "Data do contrato é obrigatória")
        LocalDate dataContrato,
        @NotNull(message = "Data de início é obrigatória")
        LocalDate dataInicioVigencia,
        @NotNull(message = "Data de fim é obrigatória")
        LocalDate dataFimVigencia,
        @DecimalMin(value = "0.0", message = "Valor da matrícula deve ser positivo")
        BigDecimal valorMatricula,
        @DecimalMin(value = "0.0", message = "Valor da mensalidade deve ser positivo")
        BigDecimal valorMensalidade,
        Integer numeroParcelas,
        @DecimalMin(value = "0.0", message = "Desconto deve ser positivo")
        BigDecimal descontoValor,
        @DecimalMin(value = "0.0", message = "Desconto percentual deve ser positivo")
        BigDecimal descontoPercentual,
        @DecimalMin(value = "0.0", message = "Valor total deve ser positivo")
        BigDecimal valorTotalContrato,
        String observacoes,
        String situacaoContrato,
        String templatePdf,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao
) {
    
    public static ContratoDTO of(Long id, Long alunoId, String alunoNome, Long turmaId, String turmaNome,
                                String numeroContrato, LocalDate dataContrato, LocalDate dataInicioVigencia,
                                LocalDate dataFimVigencia, BigDecimal valorMatricula, BigDecimal valorMensalidade,
                                Integer numeroParcelas, BigDecimal descontoValor, BigDecimal descontoPercentual,
                                BigDecimal valorTotalContrato, String observacoes, String situacaoContrato,
                                String templatePdf, LocalDateTime dataCriacao, LocalDateTime dataAtualizacao) {
        return new ContratoDTO(id, alunoId, alunoNome, turmaId, turmaNome, numeroContrato, dataContrato,
                              dataInicioVigencia, dataFimVigencia, valorMatricula, valorMensalidade, numeroParcelas,
                              descontoValor, descontoPercentual, valorTotalContrato, observacoes, situacaoContrato,
                              templatePdf, dataCriacao, dataAtualizacao);
    }
    
    public static ContratoDTO createNew(Long alunoId, Long turmaId, LocalDate dataContrato,
                                       LocalDate dataInicioVigencia, LocalDate dataFimVigencia) {
        return new ContratoDTO(null, alunoId, null, turmaId, null, null, dataContrato,
                              dataInicioVigencia, dataFimVigencia, BigDecimal.ZERO, BigDecimal.ZERO, 0,
                              BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "", "ATIVO",
                              null, null, null);
    }
}
