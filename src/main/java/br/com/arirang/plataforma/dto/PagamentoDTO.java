package br.com.arirang.plataforma.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PagamentoDTO(
        Long id,
        @NotNull(message = "Receita é obrigatória")
        Long receitaId,
        String receitaDescricao,
        Long alunoId,
        String alunoNome,
        @NotNull(message = "Valor pago é obrigatório")
        @DecimalMin(value = "0.0", inclusive = true, message = "Valor pago não pode ser negativo")
        BigDecimal valorPago,
        @NotNull(message = "Data do pagamento é obrigatória")
        LocalDate dataPagamento,
        @NotNull(message = "Forma de pagamento é obrigatória")
        String formaPagamento,
        String numeroTransacao,
        String observacoes,
        String comprovanteCaminho,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        String usuarioPagamento,
        BigDecimal valorRestante,
        Boolean isParcial,
        Boolean isIntegral,
        BigDecimal descontoPercentual,
        BigDecimal descontoValor
) {
    
    public static PagamentoDTO of(Long id, Long receitaId, String receitaDescricao, Long alunoId, String alunoNome,
                                 BigDecimal valorPago, LocalDate dataPagamento, String formaPagamento, String numeroTransacao,
                                 String observacoes, String comprovanteCaminho, LocalDateTime dataCriacao, LocalDateTime dataAtualizacao,
                                 String usuarioPagamento, BigDecimal valorRestante, Boolean isParcial, Boolean isIntegral,
                                 BigDecimal descontoPercentual, BigDecimal descontoValor) {
        return new PagamentoDTO(id, receitaId, receitaDescricao, alunoId, alunoNome, valorPago, dataPagamento, formaPagamento,
                               numeroTransacao, observacoes, comprovanteCaminho, dataCriacao, dataAtualizacao, usuarioPagamento,
                               valorRestante, isParcial, isIntegral, descontoPercentual, descontoValor);
    }
    
    public static PagamentoDTO createNew(Long receitaId, BigDecimal valorPago, LocalDate dataPagamento, String formaPagamento) {
        return new PagamentoDTO(null, receitaId, null, null, null, valorPago, dataPagamento, formaPagamento,
                               null, "", null, null, null, null, null, null, null, null, null);
    }
}
