package br.com.arirang.plataforma.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReceitaDTO(
        Long id,
        @NotNull(message = "Contrato é obrigatório")
        Long contratoId,
        String contratoNumero,
        @NotNull(message = "Aluno é obrigatório")
        Long alunoId,
        String alunoNome,
        @NotNull(message = "Tipo da receita é obrigatório")
        String tipoReceita,
        String descricao,
        @NotNull(message = "Valor original é obrigatório")
        @DecimalMin(value = "0.0", message = "Valor deve ser positivo")
        BigDecimal valorOriginal,
        @DecimalMin(value = "0.0", message = "Desconto deve ser positivo")
        BigDecimal valorDesconto,
        @DecimalMin(value = "0.0", message = "Juros devem ser positivos")
        BigDecimal valorJuros,
        @NotNull(message = "Valor final é obrigatório")
        @DecimalMin(value = "0.0", message = "Valor final deve ser positivo")
        BigDecimal valorFinal,
        @NotNull(message = "Data de vencimento é obrigatória")
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        String situacao,
        Integer numeroParcela,
        Integer totalParcelas,
        String observacoes,
        LocalDateTime dataCriacao,
        LocalDateTime dataAtualizacao,
        String usuarioCriacao,
        Long diasAtraso,
        BigDecimal valorRestante
) {
    
    public static ReceitaDTO of(Long id, Long contratoId, String contratoNumero, Long alunoId, String alunoNome,
                               String tipoReceita, String descricao, BigDecimal valorOriginal, BigDecimal valorDesconto,
                               BigDecimal valorJuros, BigDecimal valorFinal, LocalDate dataVencimento, LocalDate dataPagamento,
                               String situacao, Integer numeroParcela, Integer totalParcelas, String observacoes,
                               LocalDateTime dataCriacao, LocalDateTime dataAtualizacao, String usuarioCriacao,
                               Long diasAtraso, BigDecimal valorRestante) {
        return new ReceitaDTO(id, contratoId, contratoNumero, alunoId, alunoNome, tipoReceita, descricao, valorOriginal,
                             valorDesconto, valorJuros, valorFinal, dataVencimento, dataPagamento, situacao, numeroParcela,
                             totalParcelas, observacoes, dataCriacao, dataAtualizacao, usuarioCriacao, diasAtraso, valorRestante);
    }
    
    public static ReceitaDTO createNew(Long contratoId, Long alunoId, String tipoReceita, BigDecimal valorOriginal,
                                      LocalDate dataVencimento, Integer numeroParcela, Integer totalParcelas) {
        return new ReceitaDTO(null, contratoId, null, alunoId, null, tipoReceita, "", valorOriginal,
                             BigDecimal.ZERO, BigDecimal.ZERO, valorOriginal, dataVencimento, null, "PENDENTE",
                             numeroParcela, totalParcelas, "", null, null, null, 0L, valorOriginal);
    }
}
