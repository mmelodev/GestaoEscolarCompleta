package br.com.arirang.plataforma.dto;

import br.com.arirang.plataforma.entity.CategoriaFinanceira;
import br.com.arirang.plataforma.entity.TipoMovimentoFinanceiro;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FinanceiroDTO(
        Long id,
        
        @NotNull(message = "Tipo de movimento é obrigatório")
        TipoMovimentoFinanceiro tipoMovimento,
        
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "Valor deve ser maior que zero")
        BigDecimal valor,
        
        @NotNull(message = "Data do movimento é obrigatória")
        LocalDate dataMovimento,
        
        String descricao,
        
        CategoriaFinanceira categoria,
        
        Long contratoId,
        
        Long parcelaId,
        
        Long alunoId,
        
        LocalDateTime dataCriacao,
        
        LocalDateTime dataAtualizacao,
        
        String observacoes,
        
        String numeroDocumento,
        
        String referencia,
        
        boolean confirmado,
        
        LocalDateTime dataConfirmacao,
        
        String confirmadoPor
) {
    
    public FinanceiroDTO {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        
        if (dataMovimento == null) {
            dataMovimento = LocalDate.now();
        }
    }
}
